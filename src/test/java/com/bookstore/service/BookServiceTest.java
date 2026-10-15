package com.bookstore.service;

import com.bookstore.dto.BookDTO;
import com.bookstore.entity.Book;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("123-456-789");
        book.setPrice(BigDecimal.valueOf(29.99));
        book.setQuantity(10);
        book.setCategory("Fiction");

        bookDTO = BookDTO.fromEntity(book);
    }

    @Test
    @DisplayName("Should get all books successfully")
    void testGetAllBooks() {
        // Arrange
        List<Book> books = Arrays.asList(book);
        when(bookRepository.findAll()).thenReturn(books);

        // Act
        List<BookDTO> result = bookService.getAllBooks();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get available books")
    void testGetAvailableBooks() {
        // Arrange
        List<Book> books = Arrays.asList(book);
        when(bookRepository.findAvailableBooks()).thenReturn(books);

        // Act
        List<BookDTO> result = bookService.getAvailableBooks();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(bookRepository, times(1)).findAvailableBooks();
    }

    @Test
    @DisplayName("Should get book by ID successfully")
    void testGetBookById() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Act
        BookDTO result = bookService.getBookById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when book not found")
    void testGetBookByIdNotFound() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.getBookById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Book not found");
    }

    @Test
    @DisplayName("Should search books by keyword")
    void testSearchBooks() {
        // Arrange
        List<Book> books = Arrays.asList(book);
        when(bookRepository.searchBooks("Test")).thenReturn(books);

        // Act
        List<BookDTO> result = bookService.searchBooks("Test");

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(bookRepository, times(1)).searchBooks("Test");
    }

    @Test
    @DisplayName("Should create book successfully")
    void testCreateBook() {
        // Arrange
        when(bookRepository.findByIsbn(bookDTO.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        BookDTO result = bookService.createBook(bookDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findByIsbn(any());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ISBN already exists")
    void testCreateBookWithDuplicateISBN() {
        // Arrange
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

        // Act & Assert
        assertThatThrownBy(() -> bookService.createBook(bookDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should update book successfully")
    void testUpdateBook() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        bookDTO.setTitle("Updated Title");

        // Act
        BookDTO result = bookService.updateBook(1L, bookDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should delete book successfully")
    void testDeleteBook() {
        // Arrange
        when(bookRepository.existsById(1L)).thenReturn(true);

        // Act
        bookService.deleteBook(1L);

        // Assert
        verify(bookRepository, times(1)).existsById(1L);
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should get books by category")
    void testGetBooksByCategory() {
        // Arrange
        List<Book> books = Arrays.asList(book);
        when(bookRepository.findByCategory("Fiction")).thenReturn(books);

        // Act
        List<BookDTO> result = bookService.getBooksByCategory("Fiction");

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(bookRepository, times(1)).findByCategory("Fiction");
    }

    @Test
    @DisplayName("Should reduce quantity successfully")
    void testReduceQuantity() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        bookService.reduceQuantity(1L, 5);

        // Assert
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when reducing quantity with insufficient stock")
    void testReduceQuantityInsufficientStock() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Act & Assert
        assertThatThrownBy(() -> bookService.reduceQuantity(1L, 20))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Insufficient quantity");
    }

    @Test
    @DisplayName("Should throw exception when reducing quantity for non-existent book")
    void testReduceQuantityBookNotFound() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.reduceQuantity(999L, 5))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Book not found");
    }

    @Test
    @DisplayName("Should increase quantity successfully")
    void testIncreaseQuantity() {
        // Arrange
        Book bookCopy = new Book();
        bookCopy.setId(1L);
        bookCopy.setTitle("Test Book");
        bookCopy.setQuantity(10);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(bookCopy));
        when(bookRepository.save(any(Book.class))).thenReturn(bookCopy);

        // Act
        bookService.increaseQuantity(1L, 5);

        // Assert
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when increasing quantity for non-existent book")
    void testIncreaseQuantityBookNotFound() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.increaseQuantity(999L, 5))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Book not found");
    }

    @Test
    @DisplayName("Should throw exception when delete non-existent book")
    void testDeleteBookNotFound() {
        // Arrange
        when(bookRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> bookService.deleteBook(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Book not found");
    }

    @Test
    @DisplayName("Should return empty list when searching with no results")
    void testSearchBooksNoResults() {
        // Arrange
        when(bookRepository.searchBooks("NonExistent")).thenReturn(Arrays.asList());

        // Act
        List<BookDTO> result = bookService.searchBooks("NonExistent");

        // Assert
        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).searchBooks("NonExistent");
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent book")
    void testUpdateBookNotFound() {
        // Arrange
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.updateBook(999L, bookDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Book not found");
    }
}

