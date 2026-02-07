package com.project.FoodieHub.payment.services;

import com.project.FoodieHub.payment.dtos.PaymentDTO;
import com.project.FoodieHub.response.Response;

import java.util.List;

public interface PaymentService {

    Response<?> initializePayment(PaymentDTO paymentDTO);
    void updatePaymentForOrder(PaymentDTO paymentDTO);
    Response<List<PaymentDTO>> getAllPayments();
    Response<PaymentDTO> getPaymentById(Long paymentId);

}
