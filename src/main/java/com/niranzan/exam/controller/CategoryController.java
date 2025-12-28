package com.niranzan.exam.controller;

import com.niranzan.exam.dto.CategoryRequest;
import com.niranzan.exam.entity.Category;
import com.niranzan.exam.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Question category management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(
            summary = "Get all categories for current user",
            description = "Returns common categories and user's own categories. Admin sees all categories."
    )
    @ApiResponse(responseCode = "200", description = "List of categories retrieved successfully")
    @GetMapping
    public ResponseEntity<List<Category>> getCategoriesForCurrentUser() {
        return ResponseEntity.ok(categoryService.getCategoriesForCurrentUser());
    }

    @Operation(
            summary = "Get all categories (Admin only)",
            description = "Retrieves all categories. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "List of all categories retrieved successfully")
    @GetMapping("/all")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(
            summary = "Get common categories",
            description = "Retrieves all common categories created by admin"
    )
    @ApiResponse(responseCode = "200", description = "List of common categories retrieved successfully")
    @GetMapping("/common")
    public ResponseEntity<List<Category>> getCommonCategories() {
        return ResponseEntity.ok(categoryService.getCommonCategories());
    }

    @Operation(
            summary = "Get categories for organizer",
            description = "Retrieves common categories and categories created by a specific organizer. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "List of categories retrieved successfully")
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<Category>> getCategoriesForOrganizer(
            @Parameter(description = "Organizer ID", required = true) @PathVariable Long organizerId) {
        return ResponseEntity.ok(categoryService.getCategoriesForOrganizer(organizerId));
    }

    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a specific category by ID"
    )
    @ApiResponse(responseCode = "200", description = "Category found",
            content = @Content(schema = @Schema(implementation = Category.class)))
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create a new category",
            description = "Creates a new category. Admin can create common categories. Organizers can create their own categories."
    )
    @ApiResponse(responseCode = "200", description = "Category created successfully",
            content = @Content(schema = @Schema(implementation = Category.class)))
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @Operation(
            summary = "Update a category",
            description = "Updates a category. Only creator or admin can update."
    )
    @ApiResponse(responseCode = "200", description = "Category updated successfully",
            content = @Content(schema = @Schema(implementation = Category.class)))
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(
            summary = "Delete a category",
            description = "Deletes a category. Only creator or admin can delete."
    )
    @ApiResponse(responseCode = "204", description = "Category deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

