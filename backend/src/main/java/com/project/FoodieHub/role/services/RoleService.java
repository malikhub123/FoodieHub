package com.project.FoodieHub.role.services;

import com.project.FoodieHub.response.Response;
import com.project.FoodieHub.role.dtos.RoleDTO;

import java.util.List;

public interface RoleService {


    Response<RoleDTO> createRole(RoleDTO roleDTO);

    Response<RoleDTO> updateRole(RoleDTO roleDTO);

    Response<List<RoleDTO>> getAllRoles();

    Response<?> deleteRole(Long id);
}
