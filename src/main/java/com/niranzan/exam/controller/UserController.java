package com.niranzan.exam.controller;

import com.niranzan.exam.dto.PasswordChangeRequest;
import com.niranzan.exam.dto.ProfileUpdateRequest;
import com.niranzan.exam.entity.User;
import com.niranzan.exam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(
            summary = "Get current user profile",
            description = "Retrieves the profile of the currently authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = User.class)))
    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUserProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Update current user profile",
            description = "Updates the profile information of the currently authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = User.class)))
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody ProfileUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User updatedUser = userService.updateProfile(
                user.getId(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getMobileNumber()
        );
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(
            summary = "Change password",
            description = "Changes the password for the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Current password is incorrect")
    })
    @PostMapping("/profile/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody PasswordChangeRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean success = userService.changePassword(
                user.getId(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );
        
        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Current password is incorrect");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
            summary = "Get all organizers",
            description = "Retrieves a list of all users with ORGANIZER role. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "List of organizers retrieved successfully")
    @GetMapping("/organizers")
    public ResponseEntity<List<User>> getAllOrganizers() {
        return ResponseEntity.ok(userService.getUsersByRole(User.Role.ORGANIZER));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a specific user by ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(schema = @Schema(implementation = User.class)))
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Update user status",
            description = "Updates the status (ACTIVE/INACTIVE) of a user. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "User status updated successfully")
    @PutMapping("/{id}/status")
    public ResponseEntity<User> updateUserStatus(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @RequestParam User.UserStatus status) {
        userService.updateUserStatus(id, status);
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Delete user",
            description = "Deletes a user by ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

