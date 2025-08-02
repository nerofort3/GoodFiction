package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.DTO.BookResponse;
import com.neroforte.goodfiction.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@AllArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/title/{title}")
    public List<BookResponse> findBooksByTitle(@PathVariable String title) {
        return bookService.findOrFetchBookByTitle(title);
    }

    @GetMapping("/author/{author_name}")
    public List<BookResponse> findBooksByAuthorName(@PathVariable String author_name) {
        return bookService.findOrFetchBookByAuthorName(author_name);
    }


//    @PutMapping("/update")
//    public BookResponse updateBook(@RequestBody BookResponse book) {
//        return bookService.updateBook(book);
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Book deleted successfully");
    }

}
