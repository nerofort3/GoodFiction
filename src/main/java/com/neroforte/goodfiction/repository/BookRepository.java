package com.neroforte.goodfiction.repository;

import com.neroforte.goodfiction.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<BookEntity, Integer> {
    BookEntity findByTitle(String title);

    List<BookEntity> findByAuthor(String author);

}
