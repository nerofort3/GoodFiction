package com.neroforte.goodfiction.controller;


import com.neroforte.goodfiction.DTO.UserBookListItemResponse;
import com.neroforte.goodfiction.config.UserDetailsImpl;
import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.exception.PrivateProfileException;
import com.neroforte.goodfiction.service.UserBookListService;
import com.neroforte.goodfiction.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shelves")
@ApiController
public class UserBookListController {

    private final UserBookListService userBookListService;
    private final UserService userService;

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

    @GetMapping("/byGoogleId")
    @PreAuthorize("isAuthenticated()")
    public UserBookListItemResponse findBookByGoogleId(
            @RequestParam(required = false) String googleId,
            @RequestParam(required = false) Long targetUserId,
            Authentication authentication
    ) {
        Long currentUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        if (targetUserId != null && !Objects.equals(targetUserId, currentUserId)) {
            UserEntity targetUser = userService.getUserEntityById(targetUserId);
            if (!targetUser.isProfilePublic()) {
                throw new PrivateProfileException("The user's profile is private!");
            }
        }
        Long userId = targetUserId != null ? targetUserId : currentUserId;
        return userBookListService.findBookListItemsByGoogleBookId(googleId, userId);
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


    @PostMapping("/{googleBookId}")
    @PreAuthorize("isAuthenticated()")
    public UserBookListItemResponse addBookToShelf(
            @RequestParam(required = false, defaultValue = "FINISHED") String status,
            @RequestParam(required = false, defaultValue = "0") int rating,
            @PathVariable String googleBookId,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userBookListService.addBookToShelf(status, googleBookId, userId, rating);
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


    @PatchMapping("/{googleBookId}/update")
    @PreAuthorize("isAuthenticated()")
    public UserBookListItemResponse updateBook(
            @RequestParam(required = false) Optional<String> status,
            @RequestParam(required = false) Optional<String> review,
            @RequestParam(required = false) Optional<Integer> rating,
            @RequestParam(required = false) Optional<Double> finishedPercentage,
            @PathVariable String googleBookId,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userBookListService.updateBook(status, finishedPercentage, rating, userId, googleBookId, review);

    }

    @GetMapping("/user/{targetUserId}")
    public ResponseEntity<List<UserBookListItemResponse>> getUserShelf(
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "100") int limit,
            Authentication authentication
    ) {
        Long currentUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(userBookListService.findAllUsersBooks(limit, currentUserId, targetUserId));
    }

    @PatchMapping("/{googleBookId}/review")
    @PreAuthorize("isAuthenticated()")
    public UserBookListItemResponse updateBookReview(
            @RequestParam(required = false) String review,
            @PathVariable String googleBookId,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        return userBookListService.updateReview(review, userId, googleBookId);
    }


    @DeleteMapping("/{googleBookId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteBook(
            @PathVariable String googleBookId,
            Authentication authentication
    ) {
        Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        userBookListService.deleteBook(userId, googleBookId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Book on shelf deleted successfully");
    }


}
