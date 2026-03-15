package com.neroforte.goodfiction.repository;

import com.neroforte.goodfiction.entity.BookStatus;
import com.neroforte.goodfiction.entity.UserBookListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookListRepository extends JpaRepository <UserBookListItem, Long> {

    List<UserBookListItem> findAllByUserIdAndBookStatus(Long id, BookStatus bookStatus, Pageable pageable);

    Optional<UserBookListItem> findByUserIdAndBookId(Long userId, Long bookId);

    @Query("""
            SELECT ub FROM UserBookListItem ub
            LEFT JOIN FETCH ub.book
            WHERE ub.user.id = :userId
            AND ub.userRating > 0""")
    List<UserBookListItem> findBooksByUser(@Param("userId") Long userId);

    List<UserBookListItem> findByUserId(Long userId, Pageable pageable);

    Optional<UserBookListItem> findByUserIdAndBookTitle(Long userId, String title);
}
