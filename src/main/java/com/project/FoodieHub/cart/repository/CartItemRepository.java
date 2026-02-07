package com.project.FoodieHub.cart.repository;


import com.project.FoodieHub.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
