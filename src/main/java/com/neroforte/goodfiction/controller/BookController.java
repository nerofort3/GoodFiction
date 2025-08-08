package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.DTO.BookResponse;
import com.neroforte.goodfiction.DTO.OpenLibraryBookDoc;
import com.neroforte.goodfiction.DTO.OpenLibraryWork;
import com.neroforte.goodfiction.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@AllArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/title/{title}")
    public List<BookResponse> findBooksByTitle(@PathVariable String title, @RequestParam(required = false, defaultValue = "3") int limit) {
        return bookService.findOrFetchBookByTitle(title, limit);
    }

    @GetMapping("/author/{author_name}")
    public List<BookResponse> findBooksByAuthorName(@PathVariable String author_name , @RequestParam(required = false, defaultValue = "3")int limit) {
        return bookService.findOrFetchBookByAuthorName(author_name, limit);
    }

    @PostMapping
    public BookResponse createBook(@RequestBody OpenLibraryWork openLibraryWork) {
        return bookService.createBook(openLibraryWork);
    }


//    @PutMapping()
//    public BookResponse updateBook(@RequestBody BookResponse book) {
//        return bookService.updateBook(book);
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Book deleted successfully");
    }

}
