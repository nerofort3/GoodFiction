package com.neroforte.goodfiction.repository;

import com.neroforte.goodfiction.BaseIntegrationTest;
import com.neroforte.goodfiction.entity.ActivityFeedEntity;
import com.neroforte.goodfiction.entity.ActivityType;
import com.neroforte.goodfiction.entity.BookEntity;
import com.neroforte.goodfiction.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityFeedRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private ActivityFeedRepository activityFeedRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        // Clear repositories before each test to ensure a clean database state
        activityFeedRepository.deleteAll();
        userRepository.deleteAll();
        bookRepository.deleteAll();
    }

    /**
     * Practice Example: Test findPublicCommunityFeed(Pageable pageable)
     */
    @Test
    void findPublicCommunityFeed_shouldOnlyReturnPublicUserActivities() {
        // 1. Arrange:
        // Create a common book
        BookEntity book = BookEntity.builder()
                .googleId("book-123")
                .title("Sample Book")
                .author("Author Name")
                .build();
        book = bookRepository.save(book);

        // Create a public user
        UserEntity publicUser = UserEntity.builder()
                .username("public_user")
                .email("public@example.com")
                .password("password123")
                .isProfilePublic(true)
                .build();
        publicUser = userRepository.save(publicUser);

        // Create a private user
        UserEntity privateUser = UserEntity.builder()
                .username("private_user")
                .email("private@example.com")
                .password("password123")
                .isProfilePublic(false)
                .build();
        privateUser = userRepository.save(privateUser);

        // Create activity for the public user
        ActivityFeedEntity publicActivity = ActivityFeedEntity.builder()
                .user(publicUser)
                .book(book)
                .activityType(ActivityType.STARTED_READING)
                .timestamp(Instant.now())
                .build();
        activityFeedRepository.save(publicActivity);

        // Create activity for the private user
        ActivityFeedEntity privateActivity = ActivityFeedEntity.builder()
                .user(privateUser)
                .book(book)
                .activityType(ActivityType.FINISHED_READING)
                .timestamp(Instant.now())
                .build();
        activityFeedRepository.save(privateActivity);

        // 2. Act:
        List<ActivityFeedEntity> result = activityFeedRepository.findPublicCommunityFeed(PageRequest.of(0, 10));

        // 3. Assert:
        assertEquals(1, result.size());
        ActivityFeedEntity returnedActivity = result.get(0);
        assertEquals("public_user", returnedActivity.getUser().getUsername());
        assertTrue(returnedActivity.getUser().isProfilePublic());
        assertEquals("Sample Book", returnedActivity.getBook().getTitle());
    }

    /**
     * Practice Task: Test Sorting Order
     * 
     * Requirements:
     * - Create and save a BookEntity and a public UserEntity.
     * - Create and save two ActivityFeedEntity instances for this user and book with different timestamps:
     *   - Activity A: created 10 minutes ago (e.g. Instant.now().minusSeconds(600))
     *   - Activity B: created just now (e.g. Instant.now())
     * - Call activityFeedRepository.findPublicCommunityFeed(PageRequest.of(0, 10)).
     * - Assert that both activities are returned, and the newest one (Activity B) appears FIRST in the list.
     */
    @Test
    void findPublicCommunityFeed_shouldReturnActivitiesSortedByTimestampDesc() {
        BookEntity book = BookEntity.builder()
                .googleId("book-123")
                .title("Sample Book")
                .author("Author Name")
                .build();
        book = bookRepository.save(book);

        // Create a public user
        UserEntity publicUser = UserEntity.builder()
                .username("public_user")
                .email("public@example.com")
                .password("password123")
                .isProfilePublic(true)
                .build();
        publicUser = userRepository.save(publicUser);

        ActivityFeedEntity publicActivity = ActivityFeedEntity.builder()
                .user(publicUser)
                .book(book)
                .activityType(ActivityType.STARTED_READING)
                .timestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                .build();
        activityFeedRepository.save(publicActivity);

        ActivityFeedEntity previousPublicActivity = ActivityFeedEntity.builder()
                .user(publicUser)
                .book(book)
                .activityType(ActivityType.ADDED_TO_WANT_TO_READ)
                .timestamp(Instant.now().minusSeconds(600).truncatedTo(ChronoUnit.MILLIS))
                .build();
        activityFeedRepository.save(previousPublicActivity);

        //act:

        List<ActivityFeedEntity> result = activityFeedRepository.findPublicCommunityFeed(PageRequest.of(0,10));

        //Assert:

        assertEquals(2,result.size());
        assertEquals(publicActivity.getActivityType(), result.getFirst().getActivityType());
        assertEquals(publicActivity.getId(), result.getFirst().getId());
        assertEquals(publicActivity.getBook().getId(), result.getFirst().getBook().getId());

    }
}
