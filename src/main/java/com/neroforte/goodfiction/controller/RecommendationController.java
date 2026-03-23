package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.DTO.CustomPromptRequest;
import com.neroforte.goodfiction.DTO.RecommendationResponse;
import com.neroforte.goodfiction.config.UserDetailsImpl;
import com.neroforte.goodfiction.service.AiRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

        return ResponseEntity.ok(recommendationService.getRecommendations(userId));
    }

    @PostMapping("/custom")
    public ResponseEntity<RecommendationResponse> getCustomRecommendations(
            Authentication authentication,
            @RequestBody CustomPromptRequest request) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        return ResponseEntity.ok(recommendationService.getCustomRecommendations(userId, request.prompt()));
    }
}