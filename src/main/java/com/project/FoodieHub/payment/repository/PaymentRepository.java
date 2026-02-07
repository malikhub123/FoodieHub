package com.project.FoodieHub.payment.repository;

import com.project.FoodieHub.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}