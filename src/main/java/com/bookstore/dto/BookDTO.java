package com.bookstore.dto;

import com.bookstore.entity.Book;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String category;

    private String publisher;

    private Integer publishedYear;

    public static BookDTO fromEntity(Book book) {
        return new BookDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getDescription(),
            book.getPrice(),
            book.getQuantity(),
            book.getCategory(),
            book.getPublisher(),
            book.getPublishedYear()
        );
    }

    public Book toEntity() {
        Book book = new Book();
        book.setId(this.id);
        book.setTitle(this.title);
        book.setAuthor(this.author);
        book.setIsbn(this.isbn);
        book.setDescription(this.description);
        book.setPrice(this.price);
        book.setQuantity(this.quantity);
        book.setCategory(this.category);
        book.setPublisher(this.publisher);
        book.setPublishedYear(this.publishedYear);
        return book;
    }
}

