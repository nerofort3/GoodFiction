package com.neroforte.goodfiction.service;


import com.neroforte.goodfiction.DTO.BookResponse;
import com.neroforte.goodfiction.DTO.GoogleBooksResponse;
import com.neroforte.goodfiction.entity.BookEntity;
import com.neroforte.goodfiction.mapper.BookMapper;
import com.neroforte.goodfiction.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final RestClient restClient;
    private final BookRepository bookRepository;
    private final BookMapper googleBookMapper;

    @Value("${google.books.api-key}")
    private String apiKey;

    @Value("${google.books.base-url}")
    private String baseUrl;

    @Transactional
    public List<BookResponse> searchAndPersist(String query) {
        log.info("Searching and persisting books for query: {}", query);

        List<GoogleBooksResponse.Item> googleItems = fetchFromGoogle(query);
        if (googleItems.isEmpty()) {
            return Collections.emptyList();
        }

        return googleItems.stream()
                .map(this::processSingleBookResponse)
                .toList();
    }

    @Transactional
    public List<BookEntity> searchByTitle(String title) {
        log.info("Searching books by title: {}", title);
        // Google Books API supports specific field searches using "intitle:"
        List<GoogleBooksResponse.Item> googleItems = fetchFromGoogle("intitle:" + title);

        if (googleItems.isEmpty()) {
            return Collections.emptyList();
        }

        return googleItems.stream()
                .map(this::processSingleBookEntity)
                .toList();
    }

    @Transactional
    public List<BookResponse> searchByAuthor(String authorName) {
        log.info("Searching books by author: {}", authorName);

        List<GoogleBooksResponse.Item> googleItems = fetchFromGoogle("inauthor:" + authorName);

        if (googleItems.isEmpty()) {
            return Collections.emptyList();
        }

        return googleItems.stream()
                .map(this::processSingleBookResponse)
                .toList();
    }

    @Transactional
    public BookEntity findOrCreateBook(String googleId) {
        Optional<BookEntity> existing = bookRepository.findByGoogleId(googleId);
        if (existing.isPresent()) {
            return existing.get();
        }

        return fetchFromGoogleByIdAndSave(googleId);
    }

    private List<GoogleBooksResponse.Item> fetchFromGoogle(String query) {
        try {
            String modifiedQuery = query + "+subject:fiction";

            GoogleBooksResponse response = restClient.get()
                    .uri(baseUrl + "?q={q}&langRestrict=en&maxResults=10&key={key}",
                            modifiedQuery, apiKey)
                    .retrieve()
                    .body(GoogleBooksResponse.class);

            return response.items().stream()
                    .filter(item -> {
                        String lang = item.volumeInfo().language();
                        return lang == null || !"ru".equalsIgnoreCase(lang);
                    })
                    .sorted((book1, book2) -> {
                        // PRIORITIZE UKRAINIAN BOOKS
                        String lang1 = book1.volumeInfo().language();
                        String lang2 = book2.volumeInfo().language();

                        boolean isUk1 = "uk".equalsIgnoreCase(lang1);
                        boolean isUk2 = "uk".equalsIgnoreCase(lang2);

                        if (isUk1 && !isUk2) return -1;
                        if (!isUk1 && isUk2) return 1;
                        return 0;
                    })
                    .limit(25)
                    .toList();
        } catch (Exception e) {
            log.error("Google Books API call failed for query: {}", query, e);
            return Collections.emptyList();
        }
    }

    private BookEntity fetchFromGoogleByIdAndSave(String googleId) {
        log.info("Fetching details for Google ID: {}", googleId);
        try {
            GoogleBooksResponse.Item item = restClient.get()
                    .uri(baseUrl + "/{id}?key={key}", googleId, apiKey)
                    .retrieve()
                    .body(GoogleBooksResponse.Item.class);

            if (item == null) {
                throw new IllegalArgumentException("Book not found on Google Books: " + googleId);
            }

            return processSingleBookEntity(item);
        } catch (Exception e) {
            throw new RuntimeException("Could not fetch book details", e);
        }
    }

    private BookEntity processSingleBookEntity(GoogleBooksResponse.Item item) {
        String googleId = item.id();

        Optional<BookEntity> existing = bookRepository.findByGoogleId(googleId);

        if (existing.isPresent()) {
            return existing.get();
        } else {
            BookEntity newEntity = googleBookMapper.toEntity(item);
            return bookRepository.save(newEntity);
        }
    }

    private BookResponse processSingleBookResponse(GoogleBooksResponse.Item item) {
        BookEntity entity = processSingleBookEntity(item);
        return googleBookMapper.toResponse(entity);
    }
}