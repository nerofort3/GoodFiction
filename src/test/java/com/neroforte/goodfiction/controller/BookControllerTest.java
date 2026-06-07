package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.DTO.BookResponse;
import com.neroforte.goodfiction.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.neroforte.goodfiction.config.SecurityConfig;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Modern Spring Boot (3.4+) uses @MockitoBean instead of @MockBean to register mocks in the context
    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    /**
     * Practice Task 1: Test GET /api/books/search (Unauthenticated)
     * 
     * Requirements:
     * - Perform a GET request to "/api/books/search" with query param "query=Stormlight".
     * - Assert that the HTTP response is 401 Unauthorized (since no security user is mocked).
     */
    @Test
    void searchBooks_whenUnauthenticated_shouldReturn401() throws Exception {
        // TODO: Implement this test
        // 1. Act: call
        mockMvc.perform(get("/api/books/search").param("query", "Stormlight"))
                .andExpect(status().isUnauthorized());
        // 2. Assert: verify status isUnauthorized()
    }

    /**
     * Practice Task 2: Test GET /api/books/search (Authenticated with Mock User)
     * 
     * Requirements:
     * - Annotate the method with @WithMockUser.
     * - Arrange: stub bookService.searchAndPersist("Stormlight") to return a list with one BookResponse.
     * - Act: perform the request.
     * - Assert: verify HTTP status is 200 OK, content type is application/json, and JSON contains the expected title.
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void searchBooks_whenAuthenticated_shouldReturnBooks() throws Exception {
        // TODO: Implement this test
        // Hint:
        BookResponse response = new BookResponse("AAAAAA", "The Way of Kings", "", "Brandon Sanderson", null, null, null, null, 0.0, null);
        when(bookService.searchAndPersist("Stormlight")).thenReturn(List.of(response));
        //
        mockMvc.perform(get("/api/books/search").param("query", "Stormlight"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Way of Kings"));
    }
}
