package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.DTO.UserRegisterRequest;
import com.neroforte.goodfiction.DTO.UserUpdateRequest;
import com.neroforte.goodfiction.DTO.Password;
import com.neroforte.goodfiction.DTO.UserResponse;
import com.neroforte.goodfiction.config.UserDetailsImpl;
import com.neroforte.goodfiction.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@ApiController
public class UserController {

    private final UserService userService;


    @GetMapping()
    public List<UserResponse> findAllUsers(@RequestParam(required = false , defaultValue = "10") int limit)  {
        return userService.getAllUsers(limit);
    }

    @Tag(name = "get user", description = "get a user from DB")
    @GetMapping("/{userId}")
    public UserResponse findUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @Tag(name = "get user", description = "get a user from DB")
    @Tag(name = "your account", description = "perform various operations with your account")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse findUserById(Authentication authentication ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userService.getUserById(userId);
    }

    @Tag(name = "your account", description = "perform various operations with your account")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateUser(Authentication authentication , @Valid @RequestBody UserUpdateRequest user) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userService.updateUser(userId, user);
    }

    @Tag(name = "your account", description = "perform various operations with your account")
    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePassword(Authentication authentication, @Valid @RequestBody Password password) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        userService.updatePassword(userId, password);
        return ResponseEntity.status(HttpStatus.OK).body("password updated successfully");
    }


    @PostMapping()
    public UserResponse createUser(@RequestBody @Valid UserRegisterRequest user) {
        return userService.saveUser(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest user) {
        return userService.updateUser(id, user);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @Valid @RequestBody Password password) {
        userService.updatePassword(id, password);
        return ResponseEntity.status(HttpStatus.OK).body("password updated successfully");
    }

    @PatchMapping("/{id}/password-admin")
    public ResponseEntity<?> updatePasswordAdmin(@PathVariable Long id, @Valid @RequestBody Password password) {
        userService.updatePasswordAdmin(id, password);
        return ResponseEntity.ok("");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted successfully");
    }

    @DeleteMapping("/username/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUserByUsername(@PathVariable String username) {
        userService.deleteUserByUsername(username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted successfully");
    }

}
