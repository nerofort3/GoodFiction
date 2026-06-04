package com.neroforte.goodfiction.DTO;

import com.neroforte.goodfiction.entity.ActivityType;

import java.time.Instant;

public record ActivityFeedResponse (
        String googleId,
        Long bookListItemId,
        Long bookId,
        Long userId,
        ActivityType activityType,
        Instant timestamp
) {
}
