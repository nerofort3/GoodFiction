package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.DTO.BookResponse;
import com.neroforte.goodfiction.entity.BookEntity;
import com.neroforte.goodfiction.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> searchBooks(@RequestParam String query) {
        return ResponseEntity.ok(bookService.searchAndPersist(query));
    }

    @GetMapping("/search/author")
    public ResponseEntity<List<BookResponse>> searchByAuthor(@RequestParam String name) {
        return ResponseEntity.ok(bookService.searchByAuthor(name));
    }

    @GetMapping("/{googleId}")
    public ResponseEntity<BookResponse> getBookDetails(@PathVariable String googleId) {
        return ResponseEntity.ok(bookService.getBookDetails(googleId));
    }
}