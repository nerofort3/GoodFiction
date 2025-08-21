package com.neroforte.goodfiction.controller;


import com.neroforte.goodfiction.DTO.UserBookListItemResponse;
import com.neroforte.goodfiction.UserDetailsImpl;
import com.neroforte.goodfiction.service.UserBookListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shelves")
@ApiController
public class UserBookListController {

    private final UserBookListService userBookListService;

    //

    @GetMapping("byStatus")
    @PreAuthorize("isAuthenticated()")
    public List<UserBookListItemResponse> findAllBooksByStatus(
            @RequestParam(required = false, defaultValue = "FINISHED") String status,
            @RequestParam(required = false, defaultValue = "25") int limit,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userBookListService.findBookListItemsByStatus(status, limit, userId);
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public List<UserBookListItemResponse> findAllBooks(
            @RequestParam(required = false, defaultValue = "25") int limit,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userBookListService.findAllBooks(limit, userId);
    }


    @PostMapping("/{bookId}")
    @PreAuthorize("isAuthenticated()")
    public UserBookListItemResponse addBookToShelf(
            @RequestParam(required = false, defaultValue = "FINISHED") String status,
            @RequestParam(required = false, defaultValue = "0") int rating,
            @PathVariable long bookId,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userBookListService.addBookToShelf(status, bookId, userId, rating);
    }

    @PostMapping("/title/{title}")
    @PreAuthorize("isAuthenticated()")
    public UserBookListItemResponse addBookToShelfByTitle(
            @RequestParam(required = false, defaultValue = "FINISHED") String status,
            @RequestParam(required = false, defaultValue = "0") int rating,
            @PathVariable String title,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userBookListService.addBookToShelfByTitle(status, userId, title, rating);
    }


    @PatchMapping("/{bookId}/update")
    @PreAuthorize("isAuthenticated()")
    public UserBookListItemResponse updateBook(
            @RequestParam(required = false) Optional<String> status,
            @RequestParam(required = false) Optional<Integer> rating,
            @RequestParam(required = false) Optional<Double> finishedPercentage,
            @PathVariable long bookId,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userBookListService.updateBook(status, finishedPercentage, rating, userId, bookId);

    }

    @PatchMapping("/{bookId}/review")
    @PreAuthorize("isAuthenticated()")
    public UserBookListItemResponse updateBookReview(
            @RequestParam(required = false) String review,
            @PathVariable long bookId,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userBookListService.updateReview(review, userId, bookId);
    }


    @DeleteMapping("/{bookId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteBook(
            @PathVariable long bookId,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        userBookListService.deleteBook(userId, bookId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Book on shelf deleted successfully");
    }


}
