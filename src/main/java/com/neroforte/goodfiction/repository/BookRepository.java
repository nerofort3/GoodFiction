package com.neroforte.goodfiction.repository;

import com.neroforte.goodfiction.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
    List<BookEntity> findByTitle(String title);

    List<BookEntity> findByAuthor(String author);

    List<BookEntity> findByTitleContainingIgnoreCase(String title);

    List<BookEntity> findByAuthorContainingIgnoreCase(String authorName);
}
