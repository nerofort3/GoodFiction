package com.neroforte.goodfiction.service;


import com.neroforte.goodfiction.BookStatus;
import com.neroforte.goodfiction.DTO.UserBookListItemResponse;
import com.neroforte.goodfiction.entity.BookEntity;
import com.neroforte.goodfiction.entity.UserBookListItem;
import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.exception.AlreadyExistsException;
import com.neroforte.goodfiction.exception.NotFoundException;
import com.neroforte.goodfiction.mapper.UserBookListMapper;
import com.neroforte.goodfiction.repository.BookRepository;
import com.neroforte.goodfiction.repository.UserBookListRepository;
import com.neroforte.goodfiction.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBookListService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserBookListRepository userBookListRepository;
    private final UserBookListMapper userBookListMapper;

    public List<UserBookListItemResponse> findBookListItemsByStatus(String status,
                                                                    int limit,
                                                                    long userId) {

        UserEntity user = userService.getUserEntityById(userId);

        return user.getBookListItems().stream()
                .filter(item -> item.getBookStatus()==BookStatus.valueOf(status))
                .limit(limit)
                .map(userBookListMapper::userBookListToUserBookListItemResponse)
                .collect(Collectors.toList());

    }

    public UserBookListItemResponse addBookToShelf(String status,
                                                   long bookId,
                                                   long userId,
                                                   int rating)
    {

        Optional<UserBookListItem> found = userBookListRepository.findByUserIdAndBookId(userId, bookId);

        if (found.isPresent()) {
            throw new AlreadyExistsException("Such book already exists on your shelves");
        } else {
            UserBookListItem userBookListItem = UserBookListItem.builder()
                    .book(bookRepository.getBookEntityById(bookId)
                            .orElseThrow(() -> new NotFoundException("Book with such id not found : " + bookId)))
                    .user(userRepository.findById(userId)
                            .orElseThrow(() -> new NotFoundException("User with such id not found : " + userId)))
                    .bookStatus(BookStatus.valueOf(status))
                    .userRating(rating)
                    .build();
            userBookListRepository.save(userBookListItem);
            return userBookListMapper.userBookListToUserBookListItemResponse(userBookListItem);
        }
    }

    public UserBookListItemResponse addBookToShelfByTitle(String status,
                                                          long userId,
                                                          String title,
                                                          int rating)
    {
//        Optional<UserBookListItem> found = userBookListRepository.findByUserIdAndBookTitle(userId, title);

        Optional<UserBookListItem> found = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with such id was not found: " + userId))
                .getBookListItems()
                .stream()
                .filter(item -> item.getBook().getTitle().equals(title))
                .findFirst();


        if (found.isPresent()) {
            throw new AlreadyExistsException("Such book already exists on your shelves");
        } else {

            List<BookEntity> foundBook = bookRepository.findByTitleContainingIgnoreCase(title)
                    .orElseThrow(() -> new NotFoundException("Book with such title was not found: " + title));

            UserBookListItem userBookListItem = UserBookListItem.builder()
                    .book(foundBook.getFirst())
                    .user(userRepository.findById(userId)
                            .orElseThrow(() -> new NotFoundException("User with such id not found : " + userId)))
                    .bookStatus(BookStatus.valueOf(status))
                    .userRating(rating)
                    .build();
            userBookListRepository.save(userBookListItem);
            return userBookListMapper.userBookListToUserBookListItemResponse(userBookListItem);
        }

    }


    public List<UserBookListItemResponse> findAllBooks(int limit, Long userId) {

        UserEntity user = userService.getUserEntityById(userId);

        return user.getBookListItems().stream()
                .limit(limit)
                .map(userBookListMapper::userBookListToUserBookListItemResponse)
                .collect(Collectors.toList());
    }

    public UserBookListItemResponse updateBook(Optional<String> status,
                                               Optional<Double>finishedPercentage,
                                               Optional<Integer> rating,
                                               long userId,
                                               long bookId)
    {

        UserEntity user = userService.getUserEntityById(userId);

        Optional<UserBookListItem> updatedItem = user.getBookListItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();



        if (updatedItem.isPresent()) {
            UserBookListItem item = updatedItem.get();
            status.ifPresent(string -> item.setBookStatus(BookStatus.valueOf(string)));

            item.setFinishedPercentage(finishedPercentage.orElse(0.0));

            if(item.getBookStatus().equals(BookStatus.FINISHED)){
                item.setFinishedPercentage(100.0);
            }

            rating.ifPresent(item::setUserRating);
            userRepository.save(user);
            return userBookListMapper.userBookListToUserBookListItemResponse(item);
        } else {
            throw new NotFoundException("Book not found in user's list");
        }
    }

    public UserBookListItemResponse updateReview(String review,
                                                 long userId,
                                                 long bookId)
    {
        UserEntity user = userService.getUserEntityById(userId);

        Optional<UserBookListItem> foundBook = user.getBookListItems()
                .stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();

        foundBook.ifPresent(userBookListItem -> userBookListItem.setReview(review));

        return userBookListMapper.userBookListToUserBookListItemResponse(foundBook.get());
    }

    @Transactional
    public void deleteBook(long userId, long bookId) {
        UserEntity user = userService.getUserEntityById(userId);


        UserBookListItem bookListItem = user.getBookListItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst().get();

        user.getBookListItems().remove(bookListItem);
        userRepository.save(user);

    }
}
