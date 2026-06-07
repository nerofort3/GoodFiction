package com.neroforte.goodfiction.controller;

import tools.jackson.databind.ObjectMapper;
import com.neroforte.goodfiction.DTO.UserRegisterRequest;
import com.neroforte.goodfiction.DTO.UserResponse;
import com.neroforte.goodfiction.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.neroforte.goodfiction.config.SecurityConfig;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    /**
     * Example 1: Test public endpoint POST /api/v1/users (Unauthenticated)
     * 
     * Since this endpoint is permitAll in SecurityConfig.java, anyone can call it.
     */
    @Test
    void createUser_whenValidRequest_shouldReturnCreatedUser() throws Exception {
        // Arrange (Given)
        UserRegisterRequest request = new UserRegisterRequest("new_user", "new@example.com", "Secure@123Password");
        UserResponse response = new UserResponse(1L, "new_user", "new@example.com", Instant.now(), true, false);

        when(userService.saveUser(any(UserRegisterRequest.class))).thenReturn(response);

        // Act (When) & Assert (Then)
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())) // CSRF protection is disabled in SecurityConfig, but good practice to include in POST tests
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("new_user"))
                .andExpect(jsonPath("$.email").value("new@example.com"));

        verify(userService, times(1)).saveUser(any(UserRegisterRequest.class));
    }

    /**
     * Example 2: Test secured endpoint GET /api/v1/users (Unauthenticated)
     * 
     * Since it is not in permitAll, calling it without authentication should return 401.
     */
    @Test
    void findAllUsers_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Example 3: Test secured endpoint GET /api/v1/users (Authenticated with default user)
     */
    @Test
    @WithMockUser
    void findAllUsers_whenAuthenticated_shouldReturnUsersList() throws Exception {
        // Arrange
        UserResponse response = new UserResponse(1L, "user1", "user1@example.com", Instant.now(), true, false);
        when(userService.getAllUsers(10)).thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"));
    }

    /**
     * Example 4: Test method security authorization (Forbidden scenario)
     * 
     * DELETE /api/v1/users/{id} requires ROLE_ADMIN. Calling it as ROLE_USER should return 403 Forbidden.
     */
    @Test
    @WithMockUser(username = "regularUser", authorities = {"ROLE_USER"}) // Has ROLE_USER, but not ROLE_ADMIN
    void deleteUser_whenNotAdmin_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    /**
     * Example 5: Test method security authorization (Allowed scenario)
     * 
     * DELETE /api/v1/users/{id} requires ROLE_ADMIN. Calling it as ROLE_ADMIN should succeed.
     */
    @Test
    @WithMockUser(username = "adminUser", authorities = {"ROLE_ADMIN"}) // Has ROLE_ADMIN
    void deleteUser_whenAdmin_shouldDeleteAndReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string("User deleted successfully"));

        verify(userService, times(1)).deleteUser(1L);
    }
}
