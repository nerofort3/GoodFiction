package com.neroforte.goodfiction.repository;

import com.neroforte.goodfiction.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {

    // CHANGED: Old "findByOpenLibraryKey" is gone.
    Optional<BookEntity> findByGoogleId(String googleId);

    // Useful to check before saving to avoid exceptions
    boolean existsByGoogleId(String googleId);
}