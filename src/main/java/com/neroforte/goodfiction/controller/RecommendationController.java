package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.DTO.RecommendationResponse;
import com.neroforte.goodfiction.config.UserDetailsImpl;
import com.neroforte.goodfiction.service.AiRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final AiRecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<RecommendationResponse> getRecommendations(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new RuntimeException("User is not authenticated or principal is wrong type.");
        }

        Long userId = userDetails.getId();

        /* * ALTERNATIVE APPROACH (If you don't have a custom UserDetailsImpl with getId()):
         * String username = authentication.getName();
         * Long userId = userService.getUserByUsername(username).getId();
         */

        return ResponseEntity.ok(recommendationService.getRecommendations(userId));
    }
}