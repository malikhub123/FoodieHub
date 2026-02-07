package com.project.FoodieHub.auth_users.services;

import com.project.FoodieHub.auth_users.dtos.UserDTO;
import com.project.FoodieHub.auth_users.entity.User;
import com.project.FoodieHub.auth_users.repository.UserRepository;
import com.project.FoodieHub.aws.AWSS3Service;
import com.project.FoodieHub.email_notification.dtos.NotificationDTO;
import com.project.FoodieHub.email_notification.services.NotificationService;
import com.project.FoodieHub.exceptions.BadRequestException;
import com.project.FoodieHub.exceptions.NotFoundException;
import com.project.FoodieHub.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AWSS3Service awss3Service;


    @Override
    public User getCurrentLoggedInUser() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("user not found"));

    }

    @Override
    public Response<List<UserDTO>> getAllUsers() {

        log.info("INSIDE getAllUsers()");

        List<User> users =
                userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> dtos = users.stream().map(user -> {

            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setAddress(user.getAddress());
            dto.setProfileUrl(user.getProfileUrl());
            dto.setActive(user.isActive());

            // ❌ DO NOT include roles, orders, payments

            return dto;
        }).toList();

        return Response.<List<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All users retrieved successfully")
                .data(dtos)
                .build();
    }


    @Override
    public Response<UserDTO> getOwnAccountDetails() {

        log.info("INSIDE getOwnAccountDetails()");

        User user = getCurrentLoggedInUser();

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setProfileUrl(user.getProfileUrl());
        dto.setActive(user.isActive());

        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(dto)
                .build();
    }


    @Override
    public Response<?> updateOwnAccount(UserDTO userDTO) {

        log.info("INSIDE updateOwnAccount()");

        // Fetch the currently logged-in user
        User user = getCurrentLoggedInUser();

        String profileUrl = user.getProfileUrl();
        MultipartFile imageFile = userDTO.getImageFile();


        log.info("EXISTIN Profile URL IS: " + profileUrl);

        // Check if a new imageFile was provided
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete the old image from S3 if it exists
            if (profileUrl != null && !profileUrl.isEmpty()) {
                String keyName = profileUrl.substring(profileUrl.lastIndexOf("/") + 1);
                awss3Service.deleteFile("profile/" + keyName);

                log.info("Deleted old profile image from s3");
            }
            //upload new image
            String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            URL newImageUrl = awss3Service.uploadFile("profile/" + imageName, imageFile);

            user.setProfileUrl(newImageUrl.toString());
        }


        // Update user details
        if (userDTO.getName() != null) {
            user.setName(userDTO.getName());
        }

        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }

        if (userDTO.getAddress() != null) {
            user.setAddress(userDTO.getAddress());
        }

        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            // Check if the new email is already taken
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Save the updated user
        userRepository.save(user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account updated successfully")
                .build();

    }

    @Override
    public Response<?> deactivateOwnAccount() {

        log.info("INSIDE deactivateOwnAccount()");

        User user = getCurrentLoggedInUser();

        // Deactivate the user
        user.setActive(false);
        userRepository.save(user);

        //SEND EMAIL AFTER DEACTIVATION

        // Send email notification
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Account Deactivated")
                .body("Your account has been deactivated. If this was a mistake, please contact support.")
                .build();
        notificationService.sendEmail(notificationDTO);

        // Return a success response
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account deactivated successfully")
                .build();

    }
}
