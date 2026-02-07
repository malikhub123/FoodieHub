package com.project.FoodieHub.auth_users.services;

import com.project.FoodieHub.auth_users.dtos.LoginRequest;
import com.project.FoodieHub.auth_users.dtos.LoginResponse;
import com.project.FoodieHub.auth_users.dtos.RegistrationRequest;
import com.project.FoodieHub.response.Response;

public interface AuthService {
    Response<?> register(RegistrationRequest registrationRequest);
    Response<LoginResponse> login(LoginRequest loginRequest);
}

