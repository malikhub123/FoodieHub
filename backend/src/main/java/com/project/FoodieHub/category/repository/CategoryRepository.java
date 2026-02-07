package com.project.FoodieHub.category.repository;


import com.project.FoodieHub.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}

