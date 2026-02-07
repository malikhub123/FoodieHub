package com.project.FoodieHub.order.repository;

import com.project.FoodieHub.auth_users.entity.User;
import com.project.FoodieHub.enums.OrderStatus;
import com.project.FoodieHub.order.entity.Order;
import jakarta.persistence.Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    List<Order> findByUserOrderByOrderDateDesc(User user);

    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.menu
        WHERE o.user = :user
        ORDER BY o.orderDate DESC
        """)
    List<Order> findOrdersWithItemsByUser(@Param("user") User user);


    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o")
    long countDistinctUsers();
}
