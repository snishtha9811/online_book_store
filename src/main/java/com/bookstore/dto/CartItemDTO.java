package com.bookstore.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Long id;

    private Long bookId;

    private String bookTitle;

    private String bookAuthor;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private BigDecimal priceAtAdd;

    private BigDecimal subtotal;
}

