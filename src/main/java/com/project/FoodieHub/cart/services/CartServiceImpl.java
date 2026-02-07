package com.project.FoodieHub.cart.services;

import com.project.FoodieHub.auth_users.entity.User;
import com.project.FoodieHub.auth_users.services.UserService;
import com.project.FoodieHub.cart.dtos.CartDTO;
import com.project.FoodieHub.cart.dtos.CartItemDTO;
import com.project.FoodieHub.cart.entity.Cart;
import com.project.FoodieHub.cart.entity.CartItem;
import com.project.FoodieHub.cart.repository.CartItemRepository;
import com.project.FoodieHub.cart.repository.CartRepository;
import com.project.FoodieHub.exceptions.NotFoundException;
import com.project.FoodieHub.menu.dtos.MenuDTO;
import com.project.FoodieHub.menu.entity.Menu;
import com.project.FoodieHub.menu.repository.MenuRepository;
import com.project.FoodieHub.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {


    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final MenuRepository menuRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;


    @Override
    public Response<?> addItemToCart(CartDTO cartDTO) {
        log.info("Inside addItemToCart()");

        Long menuId = cartDTO.getMenuId();
        int quantity = cartDTO.getQuantity();

        User user = userService.getCurrentLoggedInUser();

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("Menu Item Not Found"));

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCartItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });


        // Check if the item is already in the cart
        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getMenu().getId().equals(menuId))
                .findFirst();


        //if present, increment item
        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setSubtotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            cartItemRepository.save(cartItem);
        } else {
            //if nor present, and add it
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .menu(menu)
                    .quantity(quantity)
                    .pricePerUnit(menu.getPrice())
                    .subtotal(menu.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();

            cart.getCartItems().add(newCartItem);

            cartItemRepository.save(newCartItem);

        }

        //cartRepository.save(cart);// not, it will auto save and persists in the cart table

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item added to cart successfully")
                .build();
    }


    @Override
    public Response<?> incrementItem(Long menuId) {
        log.info("Inside incrementItem()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart Not Found"));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst().orElseThrow(() -> new NotFoundException("Menu not found in cart"));

        int newQuantity = cartItem.getQuantity() + 1;  //INCREMENT THE ITEM

        cartItem.setQuantity(newQuantity);

        cartItem.setSubtotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity)));

        cartItemRepository.save(cartItem);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item incremented successfully")
                .build();


    }

    @Override
    public Response<?> decrementItem(Long menuId) {
        log.info("Inside decrementItem()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart Not Found"));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst().orElseThrow(() -> new NotFoundException("Menu not found in cart"));

        int newQuantity = cartItem.getQuantity() - 1; //DECREMENT THE ITEM

        if (newQuantity > 0) {
            cartItem.setQuantity(newQuantity);
            cartItem.setSubtotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity)));
            cartItemRepository.save(cartItem);
        } else {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        }

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item decremented successfully")
                .build();
    }

    @Override
    public Response<?> removeItem(Long cartItemId) {
        log.info("Inside removeItem()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (!cart.getCartItems().contains(cartItem)) {
            throw new NotFoundException("Cart item does not belong to this user's cart");
        }
        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);


        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item removed from cart successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<CartDTO> getShoppingCart() {

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user"));

        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<CartItemDTO> itemDTOs = new ArrayList<>();

        for (CartItem item : cart.getCartItems()) {

            CartItemDTO itemDTO = new CartItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPricePerUnit(item.getPricePerUnit());
            itemDTO.setSubtotal(item.getSubtotal());

            Menu menu = item.getMenu();
            if (menu != null) {
                MenuDTO menuDTO = new MenuDTO();
                menuDTO.setId(menu.getId());
                menuDTO.setName(menu.getName());
                menuDTO.setPrice(menu.getPrice());
                menuDTO.setImageUrl(menu.getImageUrl());
                itemDTO.setMenu(menuDTO);
            }

            totalAmount = totalAmount.add(item.getSubtotal());
            itemDTOs.add(itemDTO);
        }

        cartDTO.setCartItems(itemDTOs);
        cartDTO.setTotalAmount(totalAmount);

        return Response.<CartDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Shopping cart retrieved successfully")
                .data(cartDTO)
                .build();
    }




    @Override
    public Response<?> clearShoppingCart() {
        log.info("Inside clearShoppingCart()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user"));

        //Delete cart items from the database first
        cartItemRepository.deleteAll(cart.getCartItems());

        //Clear the cart's items collection
        cart.getCartItems().clear();

        //update the database
        cartRepository.save(cart);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Shopping cart cleared successfully")
                .build();
    }
}








