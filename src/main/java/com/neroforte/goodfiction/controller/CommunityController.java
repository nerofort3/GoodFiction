package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.DTO.ActivityFeedResponse;
import com.neroforte.goodfiction.mapper.ActivityFeedMapper;
import com.neroforte.goodfiction.service.ActivityFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final ActivityFeedService activityFeedService;
    private final ActivityFeedMapper activityMapper;

    @GetMapping("/feed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityFeedResponse>> getFeed(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(activityFeedService.getCommunityFeed(limit).stream()
                .map(activityMapper::toDto)
                .collect(Collectors.toList()));
    }
}