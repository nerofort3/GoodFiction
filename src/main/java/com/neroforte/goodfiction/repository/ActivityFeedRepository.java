package com.neroforte.goodfiction.repository;

import com.neroforte.goodfiction.entity.ActivityFeedEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityFeedRepository extends JpaRepository<ActivityFeedEntity, Long> {

    @Query("SELECT a FROM ActivityFeedEntity a JOIN FETCH a.user u JOIN FETCH a.book b " +
            "WHERE u.isProfilePublic = true ORDER BY a.timestamp DESC")
    List<ActivityFeedEntity> findPublicCommunityFeed(Pageable pageable);
}
