package com.project.FoodieHub.order.services;

import com.project.FoodieHub.auth_users.dtos.UserDTO;
import com.project.FoodieHub.auth_users.entity.User;
import com.project.FoodieHub.auth_users.repository.UserRepository;
import com.project.FoodieHub.auth_users.services.UserService;
import com.project.FoodieHub.cart.entity.Cart;
import com.project.FoodieHub.cart.entity.CartItem;
import com.project.FoodieHub.cart.repository.CartRepository;
import com.project.FoodieHub.cart.services.CartService;
import com.project.FoodieHub.email_notification.dtos.NotificationDTO;
import com.project.FoodieHub.email_notification.services.NotificationService;
import com.project.FoodieHub.enums.OrderStatus;
import com.project.FoodieHub.enums.PaymentStatus;
import com.project.FoodieHub.exceptions.BadRequestException;
import com.project.FoodieHub.exceptions.NotFoundException;
import com.project.FoodieHub.menu.dtos.MenuDTO;
import com.project.FoodieHub.order.dtos.OrderDTO;
import com.project.FoodieHub.order.dtos.OrderItemDTO;
import com.project.FoodieHub.order.entity.Order;
import com.project.FoodieHub.order.entity.OrderItem;
import com.project.FoodieHub.order.repository.OrderItemRepository;
import com.project.FoodieHub.order.repository.OrderRepository;
import com.project.FoodieHub.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final TemplateEngine templateEngine;
    private final CartService cartService;
    private final CartRepository cartRepository;

    @Value("${base.payment.link}")
    private String basePaymentLink;

    /* ===================== UTIL ===================== */

    private User getCurrentLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /* ===================== PLACE ORDER ===================== */

    @Override
    @Transactional
    public Response<?> placeOrderFromCart() {

        User customer = userService.getCurrentLoggedInUser();

        if (customer.getAddress() == null) {
            throw new NotFoundException("Delivery address not found");
        }

        Cart cart = cartRepository.findByUser_Id(customer.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem item = OrderItem.builder()
                    .menu(cartItem.getMenu())
                    .quantity(cartItem.getQuantity())
                    .pricePerUnit(cartItem.getPricePerUnit())
                    .subtotal(cartItem.getSubtotal())
                    .build();
            orderItems.add(item);
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        Order order = Order.builder()
                .user(customer)
                .orderItems(orderItems)
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.INITIALIZED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        orderItems.forEach(i -> i.setOrder(savedOrder));
        orderItemRepository.saveAll(orderItems);

        cartService.clearShoppingCart();

        OrderDTO orderDTO = mapOrderToDTO(savedOrder);
        sendOrderConfirmationEmail(customer, orderDTO);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order placed successfully. Payment link sent to email.")
                .build();
    }

    /* ===================== GET ORDER ===================== */

    @Override
    @Transactional(readOnly = true)
    public Response<OrderDTO> getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        return Response.<OrderDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .data(mapOrderToDTO(order))
                .message("Order retrieved successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<OrderDTO>> getOrdersOfUser() {

        User user = getCurrentLoggedInUser();
        List<Order> orders = orderRepository.findOrdersWithItemsByUser(user);

        List<OrderDTO> dtos = orders.stream()
                .map(this::mapOrderToDTO)
                .toList();

        return Response.<List<OrderDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(dtos)
                .message("My orders retrieved successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<Page<OrderDTO>> getAllOrders(OrderStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Order> orders = (status == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByOrderStatus(status, pageable);

        Page<OrderDTO> dtoPage = orders.map(this::mapOrderToDTO);

        return Response.<Page<OrderDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(dtoPage)
                .message("Orders retrieved successfully")
                .build();
    }

    /* ===================== ORDER ITEM ===================== */

    @Override
    @Transactional(readOnly = true)
    public Response<OrderItemDTO> getOrderItemById(Long orderItemId) {

        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order item not found"));

        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setPricePerUnit(item.getPricePerUnit());
        dto.setSubtotal(item.getSubtotal());

        MenuDTO menuDTO = new MenuDTO();
        menuDTO.setId(item.getMenu().getId());
        menuDTO.setName(item.getMenu().getName());
        menuDTO.setPrice(item.getMenu().getPrice());
        menuDTO.setImageUrl(item.getMenu().getImageUrl());

        dto.setMenu(menuDTO);
        dto.setMenuId(item.getMenu().getId());

        return Response.<OrderItemDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .data(dto)
                .message("Order item retrieved")
                .build();
    }

    /* ===================== UPDATE ===================== */

    @Override
    public Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO) {

        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        order.setOrderStatus(orderDTO.getOrderStatus());
        orderRepository.save(order);

        return Response.<OrderDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order status updated")
                .build();
    }

    @Override
    public Response<Long> countUniqueCustomers() {
        return Response.<Long>builder()
                .statusCode(HttpStatus.OK.value())
                .data(orderRepository.countDistinctUsers())
                .message("Unique customers count")
                .build();
    }

    /* ===================== MAPPER ===================== */

    private OrderDTO mapOrderToDTO(Order order) {

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentStatus(order.getPaymentStatus());

        UserDTO userDTO = new UserDTO();
        userDTO.setId(order.getUser().getId());
        userDTO.setName(order.getUser().getName());
        userDTO.setEmail(order.getUser().getEmail());
        userDTO.setAddress(order.getUser().getAddress());
        dto.setUser(userDTO);

        List<OrderItemDTO> items = new ArrayList<>();

        for (OrderItem item : order.getOrderItems()) {
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

            items.add(itemDTO);
        }

        dto.setOrderItems(items);
        return dto;
    }

    /* ===================== EMAIL ===================== */

    private void sendOrderConfirmationEmail(User customer, OrderDTO orderDTO) {

        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName", customer.getName());
        context.setVariable("orderId", orderDTO.getId());
        context.setVariable("orderDate", orderDTO.getOrderDate());
        context.setVariable("totalAmount", orderDTO.getTotalAmount());
        context.setVariable("deliveryAddress", orderDTO.getUser().getAddress());

        String paymentLink = basePaymentLink + orderDTO.getId() + "&amount=" + orderDTO.getTotalAmount();
        context.setVariable("paymentLink", paymentLink);

        String body = templateEngine.process("order-confirmation", context);

        notificationService.sendEmail(NotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject("Order Confirmation #" + orderDTO.getId())
                .body(body)
                .isHtml(true)
                .build());
    }
}
