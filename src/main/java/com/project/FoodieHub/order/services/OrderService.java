package com.project.FoodieHub.order.services;


import com.project.FoodieHub.enums.OrderStatus;
import com.project.FoodieHub.order.dtos.OrderDTO;
import com.project.FoodieHub.order.dtos.OrderItemDTO;
import com.project.FoodieHub.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Response<?> placeOrderFromCart();
    Response<OrderDTO> getOrderById(Long id);
    Response<Page<OrderDTO>> getAllOrders(OrderStatus orderStatus, int page, int size);
    Response<List<OrderDTO>> getOrdersOfUser();
    Response<OrderItemDTO> getOrderItemById(Long orderItemId);
    Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO);
    Response<Long> countUniqueCustomers();
}
