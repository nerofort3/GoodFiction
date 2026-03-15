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
//
//    // When the user clicks "Add to my list", the frontend sends the Google ID.
//    // We then fetch the full details, save it to our DB, and return the saved entity.
//    @PostMapping("/import/{googleId}")
//    public ResponseEntity<BookEntity> importBook(@PathVariable String googleId) {
//        return ResponseEntity.ok(bookService.findOrCreateBook(googleId));
//    }
}