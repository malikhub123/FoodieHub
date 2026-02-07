package com.project.FoodieHub.cart.services;

import com.project.FoodieHub.cart.dtos.CartDTO;
import com.project.FoodieHub.response.Response;

public interface CartService {

    Response<?> addItemToCart(CartDTO cartDTO);
    Response<?> incrementItem(Long menuId);
    Response<?> decrementItem(Long menuId);
    Response<?> removeItem(Long cartItemId);
    Response<CartDTO> getShoppingCart();
    Response<?> clearShoppingCart();
}
