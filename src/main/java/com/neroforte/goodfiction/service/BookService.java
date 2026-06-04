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
        // Google Books API дозволяє робити пошук по назві використовуючи "intitle:"
        List<GoogleBooksResponse.Item> googleItems = fetchFromGoogle("intitle:" + title);

        if (googleItems.isEmpty()) {
            log.info("Strict title search returned 0 results, trying fallback search for: {}", title);
            googleItems = fetchFromGoogle(title);
            if (googleItems.isEmpty()) {
                return Collections.emptyList();
            }
        }

        return googleItems.stream()
                .map(this::processSingleBookEntity)
                .toList();
    }

    @Transactional
    public BookResponse getBookDetails(String googleId) {
        log.info("Fetching details for book: {}", googleId);
        // Робить запит у бд. Якщо деталі відсутні - запит йде в Google Books API
        BookEntity entity = findOrCreateBook(googleId);
        return googleBookMapper.toResponse(entity);
    }

    @Transactional
    public List<BookResponse> searchByAuthor(String authorName) {
        log.info("Searching books by author: {}", authorName);

        List<GoogleBooksResponse.Item> googleItems = fetchFromGoogle("inauthor:" + authorName);

        if (googleItems.isEmpty()) {
            log.info("Strict author search returned 0 results, trying fallback search for: {}", authorName);
            googleItems = fetchFromGoogle(authorName);
            if (googleItems.isEmpty()) {
                return Collections.emptyList();
            }
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
        String optimized = optimizeQuery(query);
        List<GoogleBooksResponse.Item> results = executeGoogleSearch(optimized);

        if (results.isEmpty() && !optimized.equals(query)) {
            log.info("Optimized query [{}] returned 0 results, falling back to original query: {}", optimized, query);
            results = executeGoogleSearch(query);
        }
        return results;
    }

    private String optimizeQuery(String query) {
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        // 1. ISBN check
        String cleanQuery = trimmed.replaceAll("[\\s-]", "");
        if (cleanQuery.matches("\\d{10}") || cleanQuery.matches("\\d{13}")) {
            return "isbn:" + cleanQuery;
        }

        // 2. Handle "by" separator: e.g. "Title by Author"
        if (trimmed.toLowerCase().contains(" by ")) {
            int index = trimmed.toLowerCase().indexOf(" by ");
            String titlePart = trimmed.substring(0, index).trim();
            String authorPart = trimmed.substring(index + 4).trim();
            if (!titlePart.isEmpty() && !authorPart.isEmpty()) {
                return String.format("(intitle:(%s) inauthor:(%s))", titlePart, authorPart);
            }
        }

        // 3. Handle " - " separator: e.g. "Author - Title" or "Title - Author"
        if (trimmed.contains(" - ") || trimmed.contains(" – ")) {
            String separator = trimmed.contains(" - ") ? " - " : " – ";
            int index = trimmed.indexOf(separator);
            String part1 = trimmed.substring(0, index).trim();
            String part2 = trimmed.substring(index + separator.length()).trim();
            if (!part1.isEmpty() && !part2.isEmpty()) {
                return String.format("(intitle:(%s) inauthor:(%s)) OR (intitle:(%s) inauthor:(%s))",
                        part1, part2, part2, part1);
            }
        }

        // 4. Handle ":" separator: e.g. "Author: Title"
        if (trimmed.contains(": ")) {
            int index = trimmed.indexOf(": ");
            String part1 = trimmed.substring(0, index).trim();
            String part2 = trimmed.substring(index + 2).trim();
            if (!part1.isEmpty() && !part2.isEmpty()) {
                return String.format("(intitle:(%s) inauthor:(%s)) OR (intitle:(%s) inauthor:(%s))",
                        part1, part2, part2, part1);
            }
        }

        return trimmed;
    }

    private List<GoogleBooksResponse.Item> executeGoogleSearch(String query) {
        try {
            GoogleBooksResponse response = restClient.get()
                    .uri(baseUrl + "?q={q}&maxResults=40&key={key}",
                            query, apiKey)
                    .retrieve()
                    .body(GoogleBooksResponse.class);

            if (response == null || response.items() == null) {
                return Collections.emptyList();
            }

            List<GoogleBooksResponse.Item> items = response.items();

            return items.stream()
                    .filter(item -> {
                        if (item.volumeInfo() == null) return false;
                        String lang = item.volumeInfo().language();
                        return lang == null || !"ru".equalsIgnoreCase(lang);
                    })
                    .sorted((book1, book2) -> {
                        int idx1 = items.indexOf(book1);
                        int idx2 = items.indexOf(book2);

                        // Base relevance score based on Google's original position (decreasing score)
                        double score1 = 100.0 - 2.5 * idx1;
                        double score2 = 100.0 - 2.5 * idx2;

                        // Add language boost for Ukrainian results (up to 10 positions boost)
                        if ("uk".equalsIgnoreCase(book1.volumeInfo().language())) {
                            score1 += 25.0;
                        }
                        if ("uk".equalsIgnoreCase(book2.volumeInfo().language())) {
                            score2 += 25.0;
                        }

                        return Double.compare(score2, score1);
                    })
                    .limit(25)
                    .toList();
        } catch (Exception e) {
            log.error("Google Books API call failed for query: {}", query, e);
            return Collections.emptyList();
        }
    }

    private BookEntity fetchFromGoogleByIdAndSave(String googleId) { // шукаємо та зберігаємо книгу
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