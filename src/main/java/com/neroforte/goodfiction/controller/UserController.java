package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.DTO.UserRegisterRequest;
import com.neroforte.goodfiction.DTO.UserUpdateRequest;
import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.DTO.Password;
import com.neroforte.goodfiction.DTO.UserResponse;
import com.neroforte.goodfiction.serivce.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    public List<UserResponse> findAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse findUserById(@PathVariable Long id) {
        return userService.getUserById(id).get();
    }

    @PostMapping("create_user")
    public UserResponse createUser(@RequestBody @Valid UserRegisterRequest user) {
        return userService.saveUser(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest user) {
        return userService.updateUser(id, user);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @Valid @RequestBody Password password) {
        userService.updatePassword(id, password);
        return ResponseEntity.status(HttpStatus.OK).body("password updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted successfully");
    }

    @DeleteMapping("/u/{username}")
    public ResponseEntity<?> deleteUserByUsername(@PathVariable String username) {
        userService.deleteUserByUsername(username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted successfully");
    }

}
