package com.neroforte.goodfiction.repository;

import com.neroforte.goodfiction.BookStatus;
import com.neroforte.goodfiction.entity.UserBookListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookListRepository extends JpaRepository <UserBookListItem, Long> {



    List<UserBookListItem> findAllByUserIdAndBookStatus(Long id, BookStatus bookStatus, Pageable pageable);


    Optional<UserBookListItem> findByUserIdAndBookId(Long userId, Long bookId);


    List<UserBookListItem> findByUserId(Long userId, Pageable pageable);
}
