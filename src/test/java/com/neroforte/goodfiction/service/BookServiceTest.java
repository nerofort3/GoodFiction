package com.neroforte.goodfiction.service;

import com.neroforte.goodfiction.DTO.BookResponse;
import com.neroforte.goodfiction.DTO.GoogleBooksResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import com.neroforte.goodfiction.entity.BookEntity;
import com.neroforte.goodfiction.mapper.BookMapper;
import com.neroforte.goodfiction.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    // Deep stubs allows us to mock fluent API chains like restClient.get().uri(...).retrieve().body(...)
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper googleBookMapper;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setUp() {
        // Set the @Value fields using ReflectionTestUtils since Spring environment is not loaded in unit tests
        ReflectionTestUtils.setField(bookService, "apiKey", System.getenv("GeminiAPIKey"));
        ReflectionTestUtils.setField(bookService, "baseUrl", "https://www.googleapis.com/books/v1/volumes");
    }

    /**
     * Practice Task 1: Test findOrCreateBook(String googleId) Scenario A: Book
     * exists in the database. Scenario B: Book does not exist in the database,
     * fetched from API and saved.
     */
    @Test
    void findOrCreateBook_whenBookExists_shouldReturnExistingBook() {

        // TODO: Implement this test
        // 1. Arrange (given) - stub bookRepository.findByGoogleId to return a Mock/Real BookEntity
        BookEntity expected = new BookEntity();
        expected.setGoogleId("AAA");
        when(bookRepository.findByGoogleId("AAA")).thenReturn(Optional.of(expected));

         // 2. Act (when) - call bookService.findOrCreateBook("some-id")
        BookEntity actual = bookService.findOrCreateBook("AAA");

        // 3. Assert (then) - assert the returned entity is the expected one, and verify API was not called
        assertEquals(expected, actual);
    }

    @Test
    void findOrCreateBook_whenBookDoesNotExist_shouldFetchFromApiAndSave() {
        // TODO: Implement this test
        // 1. Arrange (given) - stub bookRepository.findByGoogleId to return Optional.empty()
        //                      stub restClient response to return a GoogleBooksResponse.Item
        //                      stub googleBookMapper.toEntity to return a BookEntity
        //                      stub bookRepository.save to return the saved BookEntity
        // 2. Act (when) - call bookService.findOrCreateBook("some-id")
        // 3. Assert (then) - assert returned entity is correct, verify repository save and REST client calls

        //given
        BookEntity expected = new BookEntity();
        expected.setGoogleId("AAA");

        GoogleBooksResponse.Item expectedBookResponse = new GoogleBooksResponse.Item("AAA", null);

        when(bookRepository.findByGoogleId("AAA")).thenReturn(Optional.empty());

        when(restClient.get()
                .uri(anyString(), any(Object[].class))
                .retrieve()
                .body(GoogleBooksResponse.Item.class)).thenReturn(expectedBookResponse);

        when(googleBookMapper.toEntity(expectedBookResponse)).thenReturn(expected);
        when(bookRepository.save(expected)).thenReturn(expected);

        //when
        BookEntity actual = bookService.findOrCreateBook("AAA");

        //then
        assertEquals(expected, actual);
        verify(bookRepository).save(expected);
        verify(restClient, times(2)).get();
    }

    /**
     * Practice Task 2: Test searchByTitle(String title) Scenario A: Strict
     * search returns results. Scenario B: Strict search returns empty list,
     * fallback search returns results. Scenario C: Both search and fallback
     * return empty list.
     */
    @Test
    void searchByTitle_whenStrictSearchSucceeds_shouldReturnResults() {
        //given
        BookEntity expectedEntity = new BookEntity();
        String bookTitle = "Gathering Storm";
        expectedEntity.setTitle(bookTitle);
        expectedEntity.setGoogleId("AAA");

        GoogleBooksResponse.Item expectedBookResponseItem = new GoogleBooksResponse.Item("AAA",
                new GoogleBooksResponse.VolumeInfo(bookTitle, null, "", "", "",
                        null, null, null, null, null, null));

        GoogleBooksResponse expectedGoogleBooksResponse = new GoogleBooksResponse(List.of(expectedBookResponseItem), 1);

        when(restClient.get()
                .uri(anyString(), any(Object[].class))
                .retrieve()
                .body(GoogleBooksResponse.class)).thenReturn(expectedGoogleBooksResponse);

        when(bookRepository.findByGoogleId("AAA")).thenReturn(Optional.of(expectedEntity));

        //when
        List<BookEntity> actualItemsList = bookService.searchByTitle(bookTitle);

        //then
        assertEquals(List.of(expectedEntity), actualItemsList);
        verify(restClient, atLeast(1)).get();
        verify(bookRepository, atLeast(1)).findByGoogleId("AAA");
    }

    @Test
    void searchByTitle_whenStrictSearchFailsButFallbackSucceeds_shouldReturnFallbackResults() {
        //given
        BookEntity expectedEntity = new BookEntity();
        String bookTitle = "Gathering Storm";
        expectedEntity.setTitle(bookTitle);
        expectedEntity.setGoogleId("AAA");

        GoogleBooksResponse.Item expectedBookResponseItem = new GoogleBooksResponse.Item("AAA",
                new GoogleBooksResponse.VolumeInfo(bookTitle, null, "", "", "",
                        null, null, null, null, null, null));

        GoogleBooksResponse expectedGoogleBooksResponse = new GoogleBooksResponse(List.of(expectedBookResponseItem), 1);

        when(bookRepository.findByGoogleId("AAA")).thenReturn(Optional.of(expectedEntity));

        when(restClient.get()
                .uri(anyString(), any(Object[].class))
                .retrieve()
                .body(GoogleBooksResponse.class)).thenReturn(null).thenReturn(expectedGoogleBooksResponse);

        when(bookRepository.findByGoogleId("AAA")).thenReturn(Optional.of(expectedEntity));

        //when
        List<BookEntity> actualItemsList = bookService.searchByTitle(bookTitle);

        //then
        assertEquals(List.of(expectedEntity), actualItemsList);
        verify(restClient, times(3)).get();
        verify(bookRepository, atLeast(1)).findByGoogleId("AAA");
    }

    @Test
    void searchByTitle_whenBothSearchesFail_shouldReturnEmptyList() {
        //given
        List<BookEntity> emptyList = new ArrayList<>();
        BookEntity expectedEntity = new BookEntity();
        String bookTitle = "Gathering Storm";
        expectedEntity.setTitle(bookTitle);
        expectedEntity.setGoogleId("AAA");

        when(restClient.get()
                .uri(anyString(), any(Object[].class))
                .retrieve()
                .body(GoogleBooksResponse.class)).thenReturn(null).thenReturn(null);

        //when
        List<BookEntity> actualItemsList = bookService.searchByTitle(bookTitle);

        //then
        assertEquals(emptyList, actualItemsList);
        verify(restClient, times(3)).get();
    }
}
