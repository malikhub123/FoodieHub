package com.project.FoodieHub.menu.services;

import com.project.FoodieHub.aws.AWSS3Service;
import com.project.FoodieHub.category.entity.Category;
import com.project.FoodieHub.category.repository.CategoryRepository;
import com.project.FoodieHub.exceptions.BadRequestException;
import com.project.FoodieHub.exceptions.NotFoundException;
import com.project.FoodieHub.menu.dtos.MenuDTO;
import com.project.FoodieHub.menu.entity.Menu;
import com.project.FoodieHub.menu.repository.MenuRepository;
import com.project.FoodieHub.response.Response;
import com.project.FoodieHub.review.dtos.ReviewDTO;
import com.project.FoodieHub.review.entity.Review;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService{


    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final AWSS3Service awss3Service;


    @Override
    public Response<MenuDTO> createMenu(MenuDTO menuDTO) {

        log.info("Inside createMenu()");

        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with ID: " + menuDTO.getCategoryId()));

        MultipartFile imageFile = menuDTO.getImageFile();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BadRequestException("Menu Image is needed");
        }

        String imageName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        URL s3Url = awss3Service.uploadFile("menus/" + imageName, imageFile);

        Menu menu = Menu.builder()
                .name(menuDTO.getName())
                .description(menuDTO.getDescription())
                .price(menuDTO.getPrice())
                .imageUrl(s3Url.toString())
                .category(category)
                .build();

        Menu savedMenu = menuRepository.save(menu);

        // ✅ MANUAL DTO
        MenuDTO responseDTO = new MenuDTO();
        responseDTO.setId(savedMenu.getId());
        responseDTO.setName(savedMenu.getName());
        responseDTO.setDescription(savedMenu.getDescription());
        responseDTO.setPrice(savedMenu.getPrice());
        responseDTO.setImageUrl(savedMenu.getImageUrl());
        responseDTO.setCategoryId(category.getId());

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu created successfully")
                .data(responseDTO)
                .build();
    }


    @Override
    public Response<MenuDTO> updateMenu(MenuDTO menuDTO) {

        log.info("Inside updateMenu()");

        Menu existingMenu = menuRepository.findById(menuDTO.getId())
                .orElseThrow(() -> new NotFoundException("Menu not found"));

        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        String imageUrl = existingMenu.getImageUrl();
        MultipartFile imageFile = menuDTO.getImageFile();

        if (imageFile != null && !imageFile.isEmpty()) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String keyName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                awss3Service.deleteFile("menus/" + keyName);
            }

            String imageName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            imageUrl = awss3Service.uploadFile("menus/" + imageName, imageFile).toString();
        }

        if (menuDTO.getName() != null) existingMenu.setName(menuDTO.getName());
        if (menuDTO.getDescription() != null) existingMenu.setDescription(menuDTO.getDescription());
        if (menuDTO.getPrice() != null) existingMenu.setPrice(menuDTO.getPrice());

        existingMenu.setImageUrl(imageUrl);
        existingMenu.setCategory(category);

        Menu updatedMenu = menuRepository.save(existingMenu);

        // ✅ MANUAL DTO
        MenuDTO responseDTO = new MenuDTO();
        responseDTO.setId(updatedMenu.getId());
        responseDTO.setName(updatedMenu.getName());
        responseDTO.setDescription(updatedMenu.getDescription());
        responseDTO.setPrice(updatedMenu.getPrice());
        responseDTO.setImageUrl(updatedMenu.getImageUrl());
        responseDTO.setCategoryId(category.getId());

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu updated successfully")
                .data(responseDTO)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public Response<MenuDTO> getMenuById(Long id) {

        log.info("Inside getMenuById()");

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found"));

        MenuDTO menuDTO = new MenuDTO();

        // basic fields
        menuDTO.setId(menu.getId());
        menuDTO.setName(menu.getName());
        menuDTO.setDescription(menu.getDescription());
        menuDTO.setPrice(menu.getPrice());
        menuDTO.setImageUrl(menu.getImageUrl());

        // category
        if (menu.getCategory() != null) {
            menuDTO.setCategoryId(menu.getCategory().getId());
        }


        if (menu.getReviews() != null) {

            List<ReviewDTO> reviewDTOS = new ArrayList<>();

            for (Review review : menu.getReviews()) {

                ReviewDTO reviewDTO = new ReviewDTO();
                reviewDTO.setId(review.getId());
                reviewDTO.setRating(review.getRating());
                reviewDTO.setComment(review.getComment());
                reviewDTO.setCreatedAt(review.getCreatedAt());

                reviewDTO.setMenuId(menu.getId());
                reviewDTO.setMenuName(menu.getName());

                if (review.getUser() != null) {
                    reviewDTO.setUserName(review.getUser().getName());
                }

                if (review.getOrderId() != null) {
                    reviewDTO.setOrderId(review.getOrderId());
                }

                reviewDTOS.add(reviewDTO);
            }

            reviewDTOS.sort(Comparator.comparing(ReviewDTO::getId).reversed());
            menuDTO.setReviews(reviewDTOS);
        }



        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu retrieved successfully")
                .data(menuDTO)
                .build();
    }


    @Override
    public Response<?> deleteMenu(Long id) {

        log.info("Inside deleteMenu()");

        Menu menuToDelete = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu  not found with ID: " + id));

        // Delete the image from S3 if it exists
        String imageUrl = menuToDelete.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String keyName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            awss3Service.deleteFile("menus/" + keyName);
            log.info("Deleted image from S3: menus/" + keyName);
        }

        menuRepository.deleteById(id);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu  deleted successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<MenuDTO>> getMenus(Long categoryId, String search) {

        log.info("Inside getMenus()");

        Specification<Menu> spec = buildSpecification(categoryId, search);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        List<Menu> menuList = menuRepository.findAll(spec, sort);

        List<MenuDTO> menuDTOS = new ArrayList<>();

        for (Menu menu : menuList) {

            MenuDTO dto = new MenuDTO();

            // basic fields
            dto.setId(menu.getId());
            dto.setName(menu.getName());
            dto.setDescription(menu.getDescription());
            dto.setPrice(menu.getPrice());
            dto.setImageUrl(menu.getImageUrl());

            // category
            if (menu.getCategory() != null) {
                dto.setCategoryId(menu.getCategory().getId());
            }

            // 🔥 REVIEWS — FULLY MANUAL (NO ModelMapper)
            if (menu.getReviews() != null) {

                List<ReviewDTO> reviewDTOS = new ArrayList<>();

                for (Review review : menu.getReviews()) {

                    ReviewDTO reviewDTO = new ReviewDTO();

                    reviewDTO.setId(review.getId());
                    reviewDTO.setRating(review.getRating());
                    reviewDTO.setComment(review.getComment());
                    reviewDTO.setCreatedAt(review.getCreatedAt());

                    // menu info
                    reviewDTO.setMenuId(menu.getId());
                    reviewDTO.setMenuName(menu.getName());

                    // user name
                    if (review.getUser() != null) {
                        reviewDTO.setUserName(review.getUser().getName());
                    }

                    // orderId ONLY if Review entity has it
                    if (review.getOrderId() != null) {
                        reviewDTO.setOrderId(review.getOrderId());
                    }

                    reviewDTOS.add(reviewDTO);
                }

                dto.setReviews(reviewDTOS);
            }

            menuDTOS.add(dto);
        }

        return Response.<List<MenuDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menus retrieved")
                .data(menuDTOS)
                .build();
    }




    private Specification<Menu> buildSpecification(Long categoryId, String search) {
        return (root, query, cb) -> {
            // List to accumulate all WHERE conditions
            List<Predicate> predicates = new ArrayList<>();

            // Add category filter if categoryId is provided
            if (categoryId != null) {
                // Creates condition: category.id = providedCategoryId
                predicates.add(cb.equal(
                        root.get("category").get("id"), // Navigate to category->id
                        categoryId                      // Match provided category ID
                ));
            }

            // Add search term filter if search text is provided
            if (search != null && !search.isBlank()) {
                // Prepare search term with wildcards for partial matching
                // Converts to lowercase for case-insensitive search
                String searchTerm = "%" + search.toLowerCase() + "%";

                // Creates OR condition for:
                // (name LIKE %term% OR description LIKE %term%)
                predicates.add(cb.or(
                        cb.like(
                                cb.lower(root.get("name")), // Convert name to lowercase
                                searchTerm                 // Match against search term
                        ),
                        cb.like(
                                cb.lower(root.get("description")), // Convert description to lowercase
                                searchTerm                        // Match against search term
                        )
                ));
            }

            // Combine all conditions with AND logic
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}









