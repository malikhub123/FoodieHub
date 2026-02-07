package com.project.FoodieHub.review.services;

import com.project.FoodieHub.auth_users.entity.User;
import com.project.FoodieHub.auth_users.services.UserService;
import com.project.FoodieHub.enums.OrderStatus;
import com.project.FoodieHub.exceptions.BadRequestException;
import com.project.FoodieHub.exceptions.NotFoundException;
import com.project.FoodieHub.menu.entity.Menu;
import com.project.FoodieHub.menu.repository.MenuRepository;
import com.project.FoodieHub.order.entity.Order;
import com.project.FoodieHub.order.repository.OrderItemRepository;
import com.project.FoodieHub.order.repository.OrderRepository;
import com.project.FoodieHub.response.Response;
import com.project.FoodieHub.review.dtos.ReviewDTO;
import com.project.FoodieHub.review.entity.Review;
import com.project.FoodieHub.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Response<ReviewDTO> createReview(ReviewDTO reviewDTO) {

        log.info("Inside createReview()");

        User user = userService.getCurrentLoggedInUser();

        if (reviewDTO.getOrderId() == null || reviewDTO.getMenuId() == null) {
            throw new BadRequestException("Order ID and Menu Item ID are required");
        }

        Menu menu = menuRepository.findById(reviewDTO.getMenuId())
                .orElseThrow(() -> new NotFoundException("Menu item not found"));

        Order order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This order doesn't belong to you");
        }

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("You can only review delivered items");
        }

        boolean itemInOrder = orderItemRepository.existsByOrderIdAndMenuId(
                reviewDTO.getOrderId(),
                reviewDTO.getMenuId()
        );

        if (!itemInOrder) {
            throw new BadRequestException("Menu item not part of this order");
        }

        if (reviewRepository.existsByUserIdAndMenuIdAndOrderId(
                user.getId(),
                reviewDTO.getMenuId(),
                reviewDTO.getOrderId()
        )) {
            throw new BadRequestException("You've already reviewed this item");
        }

        Review review = Review.builder()
                .user(user)
                .menu(menu)
                .orderId(reviewDTO.getOrderId())
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // ✅ MANUAL MAPPING (SAFE)
        ReviewDTO dto = new ReviewDTO();
        dto.setId(savedReview.getId());
        dto.setMenuId(menu.getId());
        dto.setOrderId(savedReview.getOrderId());
        dto.setRating(savedReview.getRating());
        dto.setComment(savedReview.getComment());
        dto.setCreatedAt(savedReview.getCreatedAt());
        dto.setUserName(user.getName());
        dto.setMenuName(menu.getName());

        return Response.<ReviewDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Review added successfully")
                .data(dto)
                .build();
    }

    @Override
    public Response<List<ReviewDTO>> getReviewsForMenu(Long menuId) {

        log.info("Inside getReviewsForMenu()");

        List<Review> reviews =
                reviewRepository.findByMenuIdOrderByIdDesc(menuId);

        List<ReviewDTO> dtos = reviews.stream().map(review -> {

            ReviewDTO dto = new ReviewDTO();
            dto.setId(review.getId());
            dto.setMenuId(menuId);
            dto.setOrderId(review.getOrderId());
            dto.setRating(review.getRating());
            dto.setComment(review.getComment());
            dto.setCreatedAt(review.getCreatedAt());

            if (review.getUser() != null) {
                dto.setUserName(review.getUser().getName());
            }

            if (review.getMenu() != null) {
                dto.setMenuName(review.getMenu().getName());
            }

            return dto;
        }).toList();

        return Response.<List<ReviewDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Reviews retrieved successfully")
                .data(dtos)
                .build();
    }

    @Override
    public Response<Double> getAverageRating(Long menuId) {

        Double avg = reviewRepository.calculateAverageRatingByMenuId(menuId);

        return Response.<Double>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Average rating retrieved successfully")
                .data(avg != null ? avg : 0.0)
                .build();
    }
}


