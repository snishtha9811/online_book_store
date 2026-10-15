package com.bookstore.controller;

import com.bookstore.dto.BookDTO;
import com.bookstore.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    /**
     * Get all books
     */
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        log.info("Fetching all books");
        List<BookDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Get available books (stock > 0)
     */
    @GetMapping("/available")
    public ResponseEntity<List<BookDTO>> getAvailableBooks() {
        log.info("Fetching available books");
        List<BookDTO> books = bookService.getAvailableBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Get book by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        log.info("Fetching book with id: {}", id);
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    /**
     * Get books by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<BookDTO>> getBooksByCategory(@PathVariable String category) {
        log.info("Fetching books by category: {}", category);
        List<BookDTO> books = bookService.getBooksByCategory(category);
        return ResponseEntity.ok(books);
    }

    /**
     * Search books by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(@RequestParam String keyword) {
        log.info("Searching books with keyword: {}", keyword);
        List<BookDTO> books = bookService.searchBooks(keyword);
        return ResponseEntity.ok(books);
    }

    /**
     * Create a new book (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO bookDTO) {
        log.info("Creating new book with title: {}", bookDTO.getTitle());
        BookDTO createdBook = bookService.createBook(bookDTO);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    /**
     * Update a book (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO bookDTO) {
        log.info("Updating book with id: {}", id);
        BookDTO updatedBook = bookService.updateBook(id, bookDTO);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Delete a book (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("Deleting book with id: {}", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}

