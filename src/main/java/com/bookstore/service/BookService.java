package com.bookstore.service;

import com.bookstore.dto.BookDTO;
import com.bookstore.entity.Book;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;

    /**
     * Get all available books
     */
    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        log.debug("Fetching all books");
        return bookRepository.findAll().stream()
            .map(BookDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get available books with stock > 0
     */
    @Transactional(readOnly = true)
    public List<BookDTO> getAvailableBooks() {
        log.debug("Fetching available books");
        return bookRepository.findAvailableBooks().stream()
            .map(BookDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get a book by ID
     */
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        log.debug("Fetching book with id: {}", id);
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return BookDTO.fromEntity(book);
    }

    /**
     * Get books by category
     */
    @Transactional(readOnly = true)
    public List<BookDTO> getBooksByCategory(String category) {
        log.debug("Fetching books by category: {}", category);
        return bookRepository.findByCategory(category).stream()
            .map(BookDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Search books by keyword
     */
    @Transactional(readOnly = true)
    public List<BookDTO> searchBooks(String keyword) {
        log.debug("Searching books with keyword: {}", keyword);
        return bookRepository.searchBooks(keyword).stream()
            .map(BookDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Create a new book
     */
    @Transactional
    public BookDTO createBook(BookDTO bookDTO) {
        log.debug("Creating new book with ISBN: {}", bookDTO.getIsbn());

        // Check if ISBN already exists
        if (bookRepository.findByIsbn(bookDTO.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
        }

        Book book = bookDTO.toEntity();
        Book savedBook = bookRepository.save(book);
        log.info("Book created successfully with id: {}", savedBook.getId());
        return BookDTO.fromEntity(savedBook);
    }

    /**
     * Update an existing book
     */
    @Transactional
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        log.debug("Updating book with id: {}", id);

        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setDescription(bookDTO.getDescription());
        book.setPrice(bookDTO.getPrice());
        book.setQuantity(bookDTO.getQuantity());
        book.setCategory(bookDTO.getCategory());
        book.setPublisher(bookDTO.getPublisher());
        book.setPublishedYear(bookDTO.getPublishedYear());

        Book updatedBook = bookRepository.save(book);
        log.info("Book updated successfully with id: {}", id);
        return BookDTO.fromEntity(updatedBook);
    }

    /**
     * Delete a book
     */
    @Transactional
    public void deleteBook(Long id) {
        log.debug("Deleting book with id: {}", id);

        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }

        bookRepository.deleteById(id);
        log.info("Book deleted successfully with id: {}", id);
    }

    /**
     * Reduce book quantity when added to cart
     */
    @Transactional
    public void reduceQuantity(Long bookId, Integer quantity) {
        log.debug("Reducing quantity for book id: {}, by: {}", bookId, quantity);

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (book.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient quantity available for book: " + book.getTitle());
        }

        book.setQuantity(book.getQuantity() - quantity);
        bookRepository.save(book);
    }

    /**
     * Increase book quantity when removed from cart
     */
    @Transactional
    public void increaseQuantity(Long bookId, Integer quantity) {
        log.debug("Increasing quantity for book id: {}, by: {}", bookId, quantity);

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        book.setQuantity(book.getQuantity() + quantity);
        bookRepository.save(book);
    }
}

