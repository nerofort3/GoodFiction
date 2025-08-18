package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.DTO.BookResponse;
import com.neroforte.goodfiction.DTO.OpenLibraryBookDoc;
import com.neroforte.goodfiction.DTO.OpenLibraryWork;
import com.neroforte.goodfiction.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@AllArgsConstructor
@ApiController
public class BookController {
    private final BookService bookService;

    @Tag(name = "get book", description = "get a book from DB or OpenLibrary API by title or it's author's name ")
    @GetMapping("/title/{title}")
    public List<BookResponse> findBooksByTitle(@PathVariable String title, @RequestParam(required = false, defaultValue = "3") int limit) {
        return bookService.findOrFetchBookByTitle(title, limit);
    }
    @Tag(name = "get book", description = "get a book from DB or OpenLibrary API by title or it's author's name ")
    @GetMapping("/author/{author_name}")
    public List<BookResponse> findBooksByAuthorName(@PathVariable String author_name , @RequestParam(required = false, defaultValue = "3")int limit) {
        return bookService.findOrFetchBookByAuthorName(author_name, limit);
    }


    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public BookResponse createBook(@RequestBody OpenLibraryWork openLibraryWork) {
        return bookService.createBook(openLibraryWork);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Book deleted successfully");
    }

}
