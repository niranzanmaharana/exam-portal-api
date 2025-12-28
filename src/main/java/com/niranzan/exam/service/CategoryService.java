package com.niranzan.exam.service;

import com.niranzan.exam.dto.CategoryRequest;
import com.niranzan.exam.entity.Category;
import com.niranzan.exam.entity.User;
import com.niranzan.exam.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getCommonCategories() {
        return categoryRepository.findByIsCommonTrue();
    }

    public List<Category> getCategoriesForOrganizer(Long organizerId) {
        User organizer = userService.getUserById(organizerId)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));
        
        // Return common categories + categories created by this organizer
        return categoryRepository.findByIsCommonTrueOrCreatedBy(organizer.getId());
    }

    public List<Category> getCategoriesForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (currentUser.getRole() == User.Role.ADMIN) {
            // Admin can see all categories
            List<Category> allCategories = categoryRepository.findAll();
            // Eagerly load createdBy to avoid lazy loading issues
            allCategories.forEach(category -> {
                if (category.getCreatedBy() != null) {
                    category.getCreatedBy().getId(); // Trigger lazy loading
                }
            });
            return allCategories;
        } else if (currentUser.getRole() == User.Role.ORGANIZER) {
            // Organizers see common categories + their own
            List<Category> categories = categoryRepository.findByIsCommonTrueOrCreatedBy(currentUser.getId());
            // Eagerly load createdBy to avoid lazy loading issues
            categories.forEach(category -> {
                if (category.getCreatedBy() != null) {
                    category.getCreatedBy().getId(); // Trigger lazy loading
                }
            });
            return categories;
        }
        
        return List.of();
    }

    public Category createCategory(CategoryRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category with this name already exists");
        }
        
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setCreatedBy(currentUser);
        
        // Admin-created categories are automatically common, organizer-created are private
        category.setIsCommon(currentUser.getRole() == User.Role.ADMIN);
        
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Only creator or admin can update
        if (!category.getCreatedBy().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("You don't have permission to update this category");
        }
        
        // Check if name is being changed and if it already exists
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category with this name already exists");
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        // Only admin can change isCommon status, and only if they're updating their own category
        if (currentUser.getRole() == User.Role.ADMIN && request.getIsCommon() != null) {
            category.setIsCommon(request.getIsCommon());
        }
        // If organizer updates, keep the original isCommon status (should be false)
        
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Only creator or admin can delete
        if (!category.getCreatedBy().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("You don't have permission to delete this category");
        }
        
        categoryRepository.deleteById(id);
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
}

