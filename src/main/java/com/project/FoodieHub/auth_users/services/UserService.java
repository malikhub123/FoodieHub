package com.project.FoodieHub.auth_users.services;

import com.project.FoodieHub.auth_users.dtos.UserDTO;
import com.project.FoodieHub.auth_users.entity.User;
import com.project.FoodieHub.response.Response;

import java.util.List;

public interface UserService {


    User getCurrentLoggedInUser();

    Response<List<UserDTO>> getAllUsers();

    Response<UserDTO> getOwnAccountDetails();

    Response<?> updateOwnAccount(UserDTO userDTO);

    Response<?> deactivateOwnAccount();

}
