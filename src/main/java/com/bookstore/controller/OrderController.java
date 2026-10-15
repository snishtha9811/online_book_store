package com.bookstore.controller;

import com.bookstore.dto.OrderDTO;
import com.bookstore.entity.Order;
import com.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create order from cart
     */
    @PostMapping("/{userId}")
    @PreAuthorize("@authorizationService.isCurrentUser(#userId)")
    public ResponseEntity<OrderDTO> createOrder(@PathVariable Long userId) {
        log.info("Creating order for user id: {}", userId);
        OrderDTO order = orderService.createOrderFromCart(userId);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {
        log.info("Fetching order with id: {}", orderId);
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Get all orders for user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("@authorizationService.isCurrentUser(#userId)")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@PathVariable Long userId) {
        log.info("Fetching orders for user id: {}", userId);
        List<OrderDTO> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Cancel order
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long orderId) {
        log.info("Cancelling order with id: {}", orderId);
        OrderDTO order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Update order status (Admin only)
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {
        log.info("Updating order id: {} status to: {}", orderId, status);
        OrderDTO order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }
}

