package com.project.FoodieHub.payment.services;

import com.project.FoodieHub.auth_users.dtos.UserDTO;
import com.project.FoodieHub.email_notification.dtos.NotificationDTO;
import com.project.FoodieHub.email_notification.services.NotificationService;
import com.project.FoodieHub.enums.OrderStatus;
import com.project.FoodieHub.enums.PaymentGateway;
import com.project.FoodieHub.enums.PaymentStatus;
import com.project.FoodieHub.exceptions.BadRequestException;
import com.project.FoodieHub.exceptions.NotFoundException;
import com.project.FoodieHub.menu.dtos.MenuDTO;
import com.project.FoodieHub.order.dtos.OrderDTO;
import com.project.FoodieHub.order.dtos.OrderItemDTO;
import com.project.FoodieHub.order.entity.Order;
import com.project.FoodieHub.order.entity.OrderItem;
import com.project.FoodieHub.order.repository.OrderRepository;
import com.project.FoodieHub.payment.dtos.PaymentDTO;
import com.project.FoodieHub.payment.entity.Payment;
import com.project.FoodieHub.payment.repository.PaymentRepository;
import com.project.FoodieHub.response.Response;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final TemplateEngine templateEngine;


    @Value("${stripe.api.secret.key}")
    private String secreteKey;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;


    @Override
    public Response<?> initializePayment(PaymentDTO paymentRequest) {

        log.info("Inside initializePayment()");
        Stripe.apiKey = secreteKey;

        Long orderId = paymentRequest.getOrderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));


        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment Already Made For This Order");
        }

        log.info("Payment Not Made For This order Yet ...moving");
        log.info("Payment Request Amount IS: {}", paymentRequest.getAmount());

        if (order.getTotalAmount() == null || paymentRequest.getAmount() == null) {
            log.info("Amount is likely null");
            throw new BadRequestException("Amount you are passing in is null");
        }

        log.info("Amount is not null ... moving forward ...");

        if (order.getTotalAmount().compareTo(paymentRequest.getAmount()) != 0) {
            log.info("Payment Amount Does Not Tally. Please Contact Out Customer Support Agent");
            throw new BadRequestException("Payment Amount Does Not Tally. Please Contact Out Customer Support Agent");
        }

        log.info("Payment amount tally...moving");

        //create payment intent i.e create unique transaction id for that payment
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency("usd")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .putMetadata("orderId", String.valueOf(orderId))
                    .build();


            PaymentIntent intent = PaymentIntent.create(params);
            String uniqueTransactionId = intent.getClientSecret();

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("success")
                    .data(uniqueTransactionId)
                    .build();

        } catch (Exception e) {
            log.info("ERROR in PaymentIntentCreateParams" + e.getMessage());
            throw new RuntimeException("Error Creating payment unique transaction id");
        }
    }


    @Override
    public void updatePaymentForOrder(PaymentDTO paymentDTO) {

        log.info("inside updatePaymentForOrder()");

        Long orderId = paymentDTO.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));

        //  Build payment entity to save
        Payment payment = new Payment();
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setAmount(paymentDTO.getAmount());
        payment.setTransactionId(paymentDTO.getTransactionId());
        payment.setPaymentStatus(paymentDTO.isSuccess() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setOrder(order);
        payment.setUser(order.getUser());

        if (!paymentDTO.isSuccess()) {
            payment.setFailureReason(paymentDTO.getFailureReason());
        }

        paymentRepository.save(payment);

        // Prepare email context. Context should be. imported from thymeleaf
        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName", order.getUser().getName());
        context.setVariable("orderId", order.getId());
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("amount", "$" + paymentDTO.getAmount());

        if (paymentDTO.isSuccess()) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);


            log.info("PAYMENT IS SUCCESSFUL ABOUT TO SEND EMAIL");

            // Add success-specific variables
            context.setVariable("transactionId", paymentDTO.getTransactionId());
            context.setVariable("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
            context.setVariable("frontendBaseUrl", this.frontendBaseUrl);

            String emailBody = templateEngine.process("payment-success", context);

            log.info("HAVE GOTTEN TEMPLATE");

            notificationService.sendEmail(NotificationDTO.builder()
                    .recipient(order.getUser().getEmail())
                    .subject("Payment Successful - Order #" + order.getId())
                    .body(emailBody)
                    .isHtml(true)
                    .build());
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);


            log.info("PAYMENT IS FAILED ABOUT TO SEND EMAIL");
            // Add failure-specific variables
            context.setVariable("failureReason", paymentDTO.getFailureReason());

            String emailBody = templateEngine.process("payment-failed", context);

            notificationService.sendEmail(NotificationDTO.builder()
                    .recipient(order.getUser().getEmail())
                    .subject("Payment Failed - Order #" + order.getId())
                    .body(emailBody)
                    .isHtml(true)
                    .build());
        }
    }


    @Override
    public Response<List<PaymentDTO>> getAllPayments() {

        log.info("inside getAllPayments()");

        List<Payment> paymentList =
                paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<PaymentDTO> paymentDTOS = new ArrayList<>();

        for (Payment payment : paymentList) {

            PaymentDTO dto = new PaymentDTO();

            dto.setId(payment.getId());
            dto.setAmount(payment.getAmount());
            dto.setTransactionId(payment.getTransactionId());
            dto.setPaymentGateway(payment.getPaymentGateway());
            dto.setPaymentStatus(payment.getPaymentStatus());
            dto.setPaymentDate(payment.getPaymentDate());
            dto.setFailureReason(payment.getFailureReason());

            // ❌ DO NOT attach Order or User for list view
            // This keeps dashboard & admin table SAFE

            paymentDTOS.add(dto);
        }

        return Response.<List<PaymentDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Payments retrieved successfully")
                .data(paymentDTOS)
                .build();
    }




    @Override
    public Response<PaymentDTO> getPaymentById(Long paymentId) {

        log.info("inside getPaymentById()");

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentGateway(payment.getPaymentGateway());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setFailureReason(payment.getFailureReason());

        // 🔹 USER (SAFE)
        if (payment.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(payment.getUser().getId());
            userDTO.setName(payment.getUser().getName());
            userDTO.setEmail(payment.getUser().getEmail());
            dto.setUser(userDTO);
        }

        // 🔹 ORDER (SAFE)
        if (payment.getOrder() != null) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setId(payment.getOrder().getId());
            orderDTO.setOrderDate(payment.getOrder().getOrderDate());
            orderDTO.setTotalAmount(payment.getOrder().getTotalAmount());
            orderDTO.setOrderStatus(payment.getOrder().getOrderStatus());
            orderDTO.setPaymentStatus(payment.getOrder().getPaymentStatus());

            // 🔹 ORDER ITEMS (SAFE)
            List<OrderItemDTO> itemDTOs = new ArrayList<>();

            for (OrderItem item : payment.getOrder().getOrderItems()) {
                OrderItemDTO itemDTO = new OrderItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setPricePerUnit(item.getPricePerUnit());
                itemDTO.setSubtotal(item.getSubtotal());

                MenuDTO menuDTO = new MenuDTO();
                menuDTO.setId(item.getMenu().getId());
                menuDTO.setName(item.getMenu().getName());
                menuDTO.setPrice(item.getMenu().getPrice());
                menuDTO.setImageUrl(item.getMenu().getImageUrl());

                itemDTO.setMenu(menuDTO);
                itemDTO.setMenuId(menuDTO.getId());

                itemDTOs.add(itemDTO);
            }

            orderDTO.setOrderItems(itemDTOs);
            dto.setOrder(orderDTO);
        }

        return Response.<PaymentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Payment retrieved successfully by id")
                .data(dto)
                .build();
    }
}


