package com.neroforte.goodfiction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neroforte.goodfiction.DTO.BookResponse;
import com.neroforte.goodfiction.DTO.OpenLibraryBookDoc;
import com.neroforte.goodfiction.DTO.OpenLibrarySearchResponse;
import com.neroforte.goodfiction.DTO.OpenLibraryWork;
import com.neroforte.goodfiction.entity.BookEntity;
import com.neroforte.goodfiction.exception.NotFoundException;
import com.neroforte.goodfiction.mapper.BookMapper;
import com.neroforte.goodfiction.repository.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Executor asyncExecutor;
    private final BookMapper bookMapper;


    private static final String SEARCH_BY_TITLE_URL = "https://openlibrary.org/search.json?title={title}&limit={limit}&fields=key,title,author_name,cover_i,isbn,first_publish_year";
    private static final String SEARCH_BY_AUTHOR_NAME_URL = "https://openlibrary.org/search.json?author={author}&limit={limit}&fields=key,title,author_name,cover_i,isbn,first_publish_year";
    private static final String SEARCH_BY_ISBN_URL = "https://openlibrary.org/isbn/%s";
    private static final String WORKS_URL = "https://openlibrary.org%s.json";
    private static final String RATINGS_URL = "https://openlibrary.org%s/ratings.json";


    @SneakyThrows
    @Transactional
    public List<BookResponse> findOrFetchBookByTitle(String title, int limit) throws RuntimeException {
        List<BookEntity> found = findBookByTitleInDB(title);
        if (found.isEmpty()) {
            long start = System.currentTimeMillis();
            final OpenLibrarySearchResponse response = searchBookByParam(SEARCH_BY_TITLE_URL, title, limit).get().getBody();
            long end = System.currentTimeMillis();
            log.info("Time spent searching : {} ms", end - start);
            start = System.currentTimeMillis();
            List<BookResponse> results = fetchWorkDetails(response).get();
            end = System.currentTimeMillis();
            log.info("Time spent fetching : {} ms", end - start);
            return results;
        } else {
            System.out.println("Found in db");
            return found.stream().map(bookMapper::bookToBookResponse).collect(Collectors.toList());
        }
    }


    @SneakyThrows
    @Transactional
    public List<BookResponse> findOrFetchBookByAuthorName(String author, int limit) throws RuntimeException {
        List<BookEntity> found = findBookByAuthorNameInDB(author);
        if (found.isEmpty()) {
            long start = System.currentTimeMillis();
            final OpenLibrarySearchResponse response = searchBookByParam(SEARCH_BY_AUTHOR_NAME_URL, author, limit).get().getBody();
            long end = System.currentTimeMillis();
            log.info("Time spent searching : {} ms", end - start);
            start = System.currentTimeMillis();
            List<BookResponse> results = fetchWorkDetails(response).get();
            end = System.currentTimeMillis();
            log.info("Time spent fetching : {} ms", end - start);
            return results;
        } else {
            System.out.println("Found in db");
            return found.stream().map(bookMapper::bookToBookResponse).collect(Collectors.toList());
        }
    }


    private List<BookEntity> findBookByAuthorNameInDB(String authorName) throws RuntimeException {
        return bookRepository.findByAuthorContainingIgnoreCase(authorName).orElseThrow(() -> new NotFoundException("Books by this author were not found"));
    }


    private List<BookEntity> findBookByTitleInDB(String title) throws RuntimeException {
        return bookRepository.findByTitleContainingIgnoreCase(title).orElseThrow(() -> new NotFoundException("Books with such title were not found"));
    }


    private CompletableFuture<ResponseEntity<OpenLibrarySearchResponse>> searchBookByParam(String url, String param, int limit) throws RuntimeException {
        return CompletableFuture.completedFuture(restClient.get()
                .uri(url, param, limit)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ((request, response1) -> {
                    log.warn("Failed to load books for this author: {}", param);
                    throw new RuntimeException("External API Error");
                }))
                .toEntity(OpenLibrarySearchResponse.class));
    }


    private CompletableFuture<List<BookResponse>> fetchWorkDetails(OpenLibrarySearchResponse response) throws RuntimeException {
        List<OpenLibraryBookDoc> docs = response.getDocs();

        List<CompletableFuture<BookResponse>> fetched = docs.stream()
                .map(doc -> CompletableFuture.supplyAsync(() -> processBookDoc(doc), asyncExecutor))
                .toList();

        return CompletableFuture.allOf(fetched.toArray(new CompletableFuture[0]))
                .thenApply(v -> fetched.stream().map(CompletableFuture::join).collect(Collectors.toList()));

    }

    private BookEntity mapToBookEntity(OpenLibraryWork openLibraryWork, OpenLibraryBookDoc doc, double externalRating) {
        Random random = new Random();

        String isbn = Optional.ofNullable(doc.getIsbn())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(list.size() - 1))
                .orElse("unknown_" + random.nextInt());

        return BookEntity.builder()
                .title(doc.getTitle())
                .author(doc.getAuthor_name() != null ? doc.getAuthor_name().getFirst() : "Unknown")
                .openLibraryKey(doc.getKey())
                .cover_i(doc.getCover_i() != null ? doc.getCover_i() : -1)
                .firstPublishYear(doc.getFirst_publish_year() != null ? doc.getFirst_publish_year() : 1666)
                .isbn(isbn)
                .externalRating(externalRating)
                .description(openLibraryWork.getSafeDescription())
                .build();
    }

    private BookResponse processBookDoc(OpenLibraryBookDoc doc) {
        String workKey = doc.getKey();
        OpenLibraryWork work = fetchWorkDetails(workKey).getBody();

        double rating = openLibrarySearchRating(workKey);
        BookEntity book = mapToBookEntity(work, doc, rating);
        book = bookRepository.save(book);

        BookResponse response = bookMapper.bookToBookResponse(book);

        response.setSubjects(work.getSubjects());

        return response;
    }

    private ResponseEntity<OpenLibraryWork> fetchWorkDetails(String workKey) throws RuntimeException {

        String workUrl = String.format(WORKS_URL, workKey);
        return restClient.get()
                .uri(workUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ((req, res) -> {
                    throw new RuntimeException("External API Error");
                }))
                .toEntity(OpenLibraryWork.class);
    }

    private double openLibrarySearchRating(String workKey) {
        try {
            String ratingUrl = String.format(RATINGS_URL, workKey);
            String jsonResponse = restClient.get()
                    .uri(ratingUrl)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode averageRaringNode = rootNode.path("summary").path("average");

            return averageRaringNode.asDouble();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Average rating wasn't found or not a number");
        }
    }

    //      TODO - implement CRUD for book entity
//    public BookResponse updateBook(BookResponse book) {
//
//
//        return null;
//    }
    @Transactional
    public void deleteBook(Long id) throws EmptyResultDataAccessException {
        bookRepository.deleteById(id);
    }

    public BookResponse createBook(OpenLibraryWork bookWork) {
        log.info("some sht");
        return null;
    }

    public String getBookTitleById(Long bookId) {
        return bookRepository.getBookEntityById(bookId).get().getTitle();
    }
}
