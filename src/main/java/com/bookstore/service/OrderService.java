package com.bookstore.service;

import com.bookstore.dto.OrderDTO;
import com.bookstore.entity.*;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    /**
     * Create order from cart
     */
    @Transactional
    public OrderDTO createOrderFromCart(Long userId) {
        log.debug("Creating order from cart for user id: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user id: " + userId));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from empty cart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(cartItem.getBook());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPriceAtAdd());

            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(
                cartItem.getPriceAtAdd().multiply(new BigDecimal(cartItem.getQuantity()))
            );
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Clear cart after order creation
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order created successfully with id: {}", savedOrder.getId());
        return OrderDTO.fromEntity(savedOrder);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        log.debug("Fetching order with id: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return OrderDTO.fromEntity(order);
    }

    /**
     * Get all orders for user
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(Long userId) {
        log.debug("Fetching orders for user id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return orderRepository.findByUserId(userId).stream()
            .map(OrderDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Update order status
     */
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status) {
        log.debug("Updating order id: {} status to: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully");
        return OrderDTO.fromEntity(updatedOrder);
    }

    /**
     * Cancel order
     */
    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        log.debug("Cancelling order id: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel a delivered order");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order cancelled successfully");
        return OrderDTO.fromEntity(updatedOrder);
    }
}

