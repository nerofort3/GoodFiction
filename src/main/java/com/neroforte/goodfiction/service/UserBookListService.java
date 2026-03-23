package com.neroforte.goodfiction.service;

import com.neroforte.goodfiction.entity.BookStatus;
import com.neroforte.goodfiction.DTO.UserBookListItemResponse;
import com.neroforte.goodfiction.entity.BookEntity;
import com.neroforte.goodfiction.entity.UserBookListItem;
import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.exception.AlreadyExistsException;
import com.neroforte.goodfiction.exception.NotFoundException;
import com.neroforte.goodfiction.mapper.UserBookListMapper;
import com.neroforte.goodfiction.repository.UserBookListRepository;
import com.neroforte.goodfiction.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBookListService {

    private final BookService bookService;

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

    /**
     * CHANGED: bookId (long) -> googleId (String)
     */
    @Transactional
    public UserBookListItemResponse addBookToShelf(String status, String googleId, long userId, int rating) {

        // 1. Find the book in DB or fetch/save it from Google automatically!
        BookEntity book = bookService.findOrCreateBook(googleId);

        // 2. Check if the user already has this book on their shelf
        // (Assuming your repo method uses internal DB IDs: userId and bookId)
        Optional<UserBookListItem> found = userBookListRepository.findByUserIdAndBookId(userId, book.getId());

        if (found.isPresent()) {
            throw new AlreadyExistsException("Such book already exists on your shelves");
        }

        UserEntity user = userService.getUserEntityById(userId);

        UserBookListItem userBookListItem = UserBookListItem.builder()
                .book(book)
                .user(user)
                .bookStatus(BookStatus.valueOf(status.toUpperCase()))
                .userRating(rating)
                .build();

        userBookListRepository.save(userBookListItem);
        return userBookListMapper.userBookListToUserBookListItemResponse(userBookListItem);
    }

    /**
     * NOTE: Adding by Title is dangerous because titles aren't unique (e.g., "The Gathering Storm").
     * I adapted this to use the new BookService, taking the first search result.
     * However, I highly recommend your frontend passes the 'googleId' using the method above instead.
     */
    @Transactional
    public UserBookListItemResponse addBookToShelfByTitle(String status, long userId, String title, int rating) {

        // 1. Search Google via our service
        List<BookEntity> searchResults = bookService.searchByTitle(title);
        if (searchResults.isEmpty()) {
            throw new NotFoundException("Book with such title was not found on Google Books: " + title);
        }

        // 2. Take the most relevant match's Google ID
        String googleId = searchResults.get(0).getGoogleId();

        // 3. Reuse our robust logic
        return addBookToShelf(status, googleId, userId, rating);
    }

    public List<UserBookListItemResponse> findAllBooks(int limit, Long userId) {
        UserEntity user = userService.getUserEntityById(userId);

        return user.getBookListItems().stream()
                .limit(limit)
                .map(userBookListMapper::userBookListToUserBookListItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * CHANGED: bookId (long) -> googleId (String)
     */
    @Transactional
    public UserBookListItemResponse updateBook(Optional<String> status,
                                               Optional<Double> finishedPercentage,
                                               Optional<Integer> rating,
                                               long userId,
                                               String googleId,
                                               Optional<String> review) {

        UserEntity user = userService.getUserEntityById(userId);

        // Filter by the new googleId
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
            return userBookListMapper.userBookListToUserBookListItemResponse(item);
        } else {
            throw new NotFoundException("Book not found in user's list");
        }
    }

    /**
     * CHANGED: bookId (long) -> googleId (String)
     */
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
            return userBookListMapper.userBookListToUserBookListItemResponse(item);
        } else {
            throw new NotFoundException("Book not found in user's list");
        }
    }

    /**
     * CHANGED: bookId (long) -> googleId (String)
     */
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