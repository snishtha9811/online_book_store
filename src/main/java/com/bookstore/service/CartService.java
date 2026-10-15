package com.bookstore.service;

import com.bookstore.dto.CartDTO;
import com.bookstore.dto.CartItemDTO;
import com.bookstore.entity.Book;
import com.bookstore.entity.Cart;
import com.bookstore.entity.CartItem;
import com.bookstore.entity.User;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * Get or create cart for user
     */
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        log.debug("Getting or creating cart for user id: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });
    }

    /**
     * Get cart for user
    */
    @Transactional(readOnly = true)
    public CartDTO getCart(Long userId) {
        log.debug("Fetching cart for user id: {}", userId);

        Cart cart = getOrCreateCart(userId);
        return convertToDTO(cart);
    }

    /**
     * Add item to cart
     */
    @Transactional
    public CartDTO addToCart(Long userId, Long bookId, Integer quantity) {
        log.debug("Adding book id: {} to cart for user id: {}, quantity: {}", bookId, userId, quantity);

        Cart cart = getOrCreateCart(userId);
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (book.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient quantity available for book: " + book.getTitle());
        }

        CartItem existingItem = cartItemRepository.findByCartAndBookId(cart, bookId).orElse(null);

        if (existingItem != null) {
            // Update existing item
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
            log.debug("Updated existing cart item for book id: {}", bookId);
        } else {
            // Create new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setBook(book);
            newItem.setQuantity(quantity);
            newItem.setPriceAtAdd(book.getPrice());
            cartItemRepository.save(newItem);
            log.debug("Added new item to cart for book id: {}", bookId);
        }

        return convertToDTO(cart);
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public CartDTO removeFromCart(Long userId, Long cartItemId) {
        log.debug("Removing cart item id: {} from user id: {} cart", cartItemId, userId);

        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        cartItemRepository.delete(cartItem);
        log.debug("Removed cart item from cart");

        return convertToDTO(cart);
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public CartDTO updateQuantity(Long userId, Long cartItemId, Integer newQuantity) {
        log.debug("Updating cart item id: {} quantity to: {} for user id: {}", cartItemId, newQuantity, userId);

        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Book book = cartItem.getBook();
        if (book.getQuantity() < newQuantity - cartItem.getQuantity()) {
            throw new IllegalArgumentException("Insufficient quantity available for book: " + book.getTitle());
        }

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
        log.debug("Updated cart item quantity");

        return convertToDTO(cart);
    }

    /**
     * Clear cart
     */
    @Transactional
    public void clearCart(Long userId) {
        log.debug("Clearing cart for user id: {}", userId);

        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.debug("Cart cleared");
    }

    /**
     * Convert Cart entity to CartDTO
     */
    private CartDTO convertToDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setUserId(cart.getUser().getId());

        cartDTO.setItems(cart.getItems().stream()
            .map(item -> {
                CartItemDTO itemDTO = new CartItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setBookId(item.getBook().getId());
                itemDTO.setBookTitle(item.getBook().getTitle());
                itemDTO.setBookAuthor(item.getBook().getAuthor());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setPriceAtAdd(item.getPriceAtAdd());
                itemDTO.setSubtotal(item.getPriceAtAdd().multiply(new BigDecimal(item.getQuantity())));
                return itemDTO;
            })
            .collect(Collectors.toList())
        );

        return cartDTO;
    }
}

