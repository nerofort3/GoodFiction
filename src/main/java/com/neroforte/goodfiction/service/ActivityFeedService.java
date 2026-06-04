package com.neroforte.goodfiction.service;

import com.neroforte.goodfiction.entity.ActivityFeedEntity;
import com.neroforte.goodfiction.entity.ActivityType;
import com.neroforte.goodfiction.entity.BookEntity;
import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.repository.ActivityFeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityFeedService {

    private final ActivityFeedRepository activityFeedRepository;

    public void logActivity(UserEntity user, BookEntity book, ActivityType type, String content) {
        ActivityFeedEntity activity = ActivityFeedEntity.builder()
                .user(user)
                .book(book)
                .activityType(type)
                .timestamp(Instant.now())
                .build();

        activityFeedRepository.save(activity);
    }

    public List<ActivityFeedEntity> getCommunityFeed(int limit) {
        return activityFeedRepository.findPublicCommunityFeed(PageRequest.of(0, limit));
    }
}