package com.neroforte.goodfiction.repository;

import com.neroforte.goodfiction.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<BookEntity, Long> {

    Optional <List<BookEntity>> findByTitleContainingIgnoreCase(String title);

    Optional <List<BookEntity>>  findByAuthorContainingIgnoreCase(String authorName);

    Optional<BookEntity> findByIsbn(String isbn);

    Optional<BookEntity> getBookEntityById(Long id);
}
