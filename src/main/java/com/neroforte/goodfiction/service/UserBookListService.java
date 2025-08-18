package com.neroforte.goodfiction.service;


import com.neroforte.goodfiction.BookStatus;
import com.neroforte.goodfiction.DTO.UserBookListItemResponse;
import com.neroforte.goodfiction.entity.UserBookListItem;
import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.exception.AlreadyExistsException;
import com.neroforte.goodfiction.exception.NotFoundException;
import com.neroforte.goodfiction.mapper.UserBookListMapper;
import com.neroforte.goodfiction.repository.BookRepository;
import com.neroforte.goodfiction.repository.UserBookListRepository;
import com.neroforte.goodfiction.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private final UserBookListRepository userBookListRepository;
    private final UserBookListMapper userBookListMapper;

    public List<UserBookListItemResponse> findBookListItemsByStatus(String status, int limit, long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with such id not found : " + userId));

        return user.getBookListItems().stream()
                .filter(item -> item.getBookStatus()==BookStatus.valueOf(status))
                .limit(limit)
                .map(userBookListMapper::userBookListToUserBookListItemResponse)
                .collect(Collectors.toList());

    }

    public UserBookListItemResponse addBookToShelf(String status, long bookId, long userId, int rating) {

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

    public List<UserBookListItemResponse> findAllBooks(int limit, Long userId) {

        UserEntity user =userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with such id not found : " + userId));

        return user.getBookListItems().stream()
                .limit(limit)
                .map(userBookListMapper::userBookListToUserBookListItemResponse)
                .collect(Collectors.toList());
    }

    public UserBookListItemResponse updateBook(Optional<String> status, Optional<Integer> rating,long userId, long bookId) {

        UserEntity user =userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with such id not found : " + userId));

        Optional<UserBookListItem> updatedItem = user.getBookListItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();

        if (updatedItem.isPresent()) {
            UserBookListItem item = updatedItem.get();
            status.ifPresent(string -> item.setBookStatus(BookStatus.valueOf(string)));

            rating.ifPresent(item::setUserRating);
            userRepository.save(user);
            return userBookListMapper.userBookListToUserBookListItemResponse(item);
        } else {
            throw new NotFoundException("Book not found in user's list");
        }
    }

    @Transactional
    public void deleteBook(long userId, long bookId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with such id not found : " + userId));


        UserBookListItem bookListItem = user.getBookListItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst().get();

        user.getBookListItems().remove(bookListItem);
        userRepository.save(user);

    }
}
