package com.bookstore.controller;

import com.bookstore.dto.CartDTO;
import com.bookstore.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Get cart for the current user
     */
    @GetMapping("/{userId}")
    @PreAuthorize("@authorizationService.isCurrentUser(#userId)")
    public ResponseEntity<CartDTO> getCart(@PathVariable Long userId) {
        log.info("Fetching cart for user id: {}", userId);
        CartDTO cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Add book to cart
     */
    @PostMapping("/{userId}/items")
    @PreAuthorize("@authorizationService.isCurrentUser(#userId)")
    public ResponseEntity<CartDTO> addToCart(
            @PathVariable Long userId,
            @RequestParam Long bookId,
            @RequestParam Integer quantity) {
        log.info("Adding book id: {} to cart for user id: {}, quantity: {}", bookId, userId, quantity);
        CartDTO cart = cartService.addToCart(userId, bookId, quantity);
        return ResponseEntity.ok(cart);
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/{userId}/items/{cartItemId}")
    @PreAuthorize("@authorizationService.isCurrentUser(#userId)")
    public ResponseEntity<CartDTO> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long cartItemId) {
        log.info("Removing cart item id: {} from user id: {} cart", cartItemId, userId);
        CartDTO cart = cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/{userId}/items/{cartItemId}")
    @PreAuthorize("@authorizationService.isCurrentUser(#userId)")
    public ResponseEntity<CartDTO> updateQuantity(
            @PathVariable Long userId,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        log.info("Updating cart item id: {} quantity to: {} for user id: {}", cartItemId, quantity, userId);
        CartDTO cart = cartService.updateQuantity(userId, cartItemId, quantity);
        return ResponseEntity.ok(cart);
    }

    /**
     * Clear cart
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("@authorizationService.isCurrentUser(#userId)")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        log.info("Clearing cart for user id: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}

