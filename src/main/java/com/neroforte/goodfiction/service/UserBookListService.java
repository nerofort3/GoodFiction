package com.neroforte.goodfiction.service;

import com.neroforte.goodfiction.entity.*;
import com.neroforte.goodfiction.DTO.UserBookListItemResponse;
import com.neroforte.goodfiction.exception.AlreadyExistsException;
import com.neroforte.goodfiction.exception.NotFoundException;
import com.neroforte.goodfiction.exception.PrivateProfileException;
import com.neroforte.goodfiction.mapper.UserBookListMapper;
import com.neroforte.goodfiction.repository.UserBookListRepository;
import com.neroforte.goodfiction.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBookListService {

    private final BookService bookService;
    private final ActivityFeedService feedService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserBookListRepository userBookListRepository;
    private final UserBookListMapper userBookListMapper;

    public List<UserBookListItemResponse> findBookListItemsByStatus(String status, int limit, long userId) {
        UserEntity user = userService.getUserEntityById(userId);

        return user.getBookListItems().stream()
                .filter(item -> item.getBookStatus() == BookStatus.valueOf(status.toUpperCase()))
                .limit(limit)
                .map(userBookListMapper::userBookListToUserBookListItemResponse)
                .collect(Collectors.toList());
    }

    public UserBookListItemResponse findBookListItemsByGoogleBookId(String googleBookId, long userId) {
        UserEntity user = userService.getUserEntityById(userId);

        return user.getBookListItems().stream()
                .filter(item -> Objects.equals(item.getBook().getGoogleId(), googleBookId))
                .map(userBookListMapper::userBookListToUserBookListItemResponse)
                .findFirst().orElseThrow(() -> new RuntimeException("Such book was not found on the shelf"));
    }

    @Transactional
    public UserBookListItemResponse addBookToShelf(String status, String googleId, long userId, int rating) {

        BookEntity book = bookService.findOrCreateBook(googleId);
        Optional<UserBookListItem> found = userBookListRepository.findByUserIdAndBookId(userId, book.getId());

        if (found.isPresent()) {
            throw new AlreadyExistsException("Such book already exists on your shelves");
        }

        UserEntity user = userService.getUserEntityById(userId);
        BookStatus bookStatus = BookStatus.valueOf(status.toUpperCase());
        ActivityType activityType = null;

        switch (bookStatus) {
            case FINISHED -> activityType = ActivityType.FINISHED_READING;
            case WANT_TO_READ -> activityType = ActivityType.ADDED_TO_WANT_TO_READ;
            case CURRENTLY_READING -> activityType = ActivityType.STARTED_READING;
            default -> log.warn("Could not add determine feed status");
        }

        UserBookListItem userBookListItem = UserBookListItem.builder()
                .book(book)
                .user(user)
                .bookStatus(bookStatus)
                .userRating(rating)
                .build();

        if (activityType != null) {
            feedService.logActivity(user, book, activityType, null);
        }
        userBookListRepository.save(userBookListItem);
        return userBookListMapper.userBookListToUserBookListItemResponse(userBookListItem);
    }

    /**
     * TODO: Adding by Title is dangerous because titles aren't unique (e.g., "The Gathering Storm").
     * I adapted this to use the new BookService, taking the first search result.
     * However, I highly recommend your frontend passes the 'googleId' using the method above instead.
     */
    @Transactional
    public UserBookListItemResponse addBookToShelfByTitle(String status, long userId, String title, int rating) {

        List<BookEntity> searchResults = bookService.searchByTitle(title);
        if (searchResults.isEmpty()) {
            throw new NotFoundException("Book with such title was not found on Google Books: " + title);
        }

        String googleId = searchResults.get(0).getGoogleId();

        return addBookToShelf(status, googleId, userId, rating);
    }

    public List<UserBookListItemResponse> findAllBooks(int limit, Long userId) {
        UserEntity user = userService.getUserEntityById(userId);

        return user.getBookListItems().stream()
                .limit(limit)
                .map(userBookListMapper::userBookListToUserBookListItemResponse)
                .collect(Collectors.toList());
    }

    public List<UserBookListItemResponse> findAllUsersBooks(int limit, Long currentUserId, Long targetUserId) {
        UserEntity currentUser = userService.getUserEntityById(currentUserId);
        UserEntity targetUser = userService.getUserEntityById(targetUserId);

        if (!Objects.equals(currentUserId, targetUserId) && !targetUser.isProfilePublic()) {
            throw new PrivateProfileException("The user's profile is private!");
        }

        return targetUser.getBookListItems().stream()
                .limit(limit)
                .map(userBookListMapper::userBookListToUserBookListItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserBookListItemResponse updateBook(Optional<String> status,
                                               Optional<Double> finishedPercentage,
                                               Optional<Integer> rating,
                                               long userId,
                                               String googleId,
                                               Optional<String> review) {

        UserEntity user = userService.getUserEntityById(userId);

        Optional<UserBookListItem> updatedItem = user.getBookListItems().stream()
                .filter(item -> item.getBook().getGoogleId().equals(googleId))
                .findFirst();

        if (updatedItem.isPresent()) {
            UserBookListItem item = updatedItem.get();
            status.ifPresent(string -> item.setBookStatus(BookStatus.valueOf(string.toUpperCase())));

            item.setFinishedPercentage(finishedPercentage.orElse(0.0));
            item.setReview(review.orElse(""));

            if (item.getBookStatus().equals(BookStatus.FINISHED)) {
                item.setFinishedPercentage(100.0);
            }

            rating.ifPresent(item::setUserRating);
            userBookListRepository.save(item); // Save the item explicitly
            feedService.logActivity(user, item.getBook(), ActivityType.UPDATED_BOOK, item.getReview());
            return userBookListMapper.userBookListToUserBookListItemResponse(item);
        } else {
            throw new NotFoundException("Book not found in user's list");
        }
    }

    @Transactional
    public UserBookListItemResponse updateReview(String review, long userId, String googleId) {
        UserEntity user = userService.getUserEntityById(userId);

        Optional<UserBookListItem> foundBook = user.getBookListItems()
                .stream()
                .filter(item -> item.getBook().getGoogleId().equals(googleId))
                .findFirst();

        if (foundBook.isPresent()) {
            UserBookListItem item = foundBook.get();
            item.setReview(review);
            userBookListRepository.save(item);
            feedService.logActivity(user, item.getBook(), ActivityType.WROTE_REVIEW, item.getReview());
            return userBookListMapper.userBookListToUserBookListItemResponse(item);
        } else {
            throw new NotFoundException("Book not found in user's list");
        }
    }

    @Transactional
    public void deleteBook(long userId, String googleId) {
        UserEntity user = userService.getUserEntityById(userId);

        UserBookListItem bookListItem = user.getBookListItems().stream()
                .filter(item -> item.getBook().getGoogleId().equals(googleId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Book not found in user's list"));

        user.getBookListItems().remove(bookListItem);
        userRepository.save(user);
    }
}