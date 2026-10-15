package com.bookstore.dto;

import com.bookstore.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;

    private Long userId;

    private List<OrderItemDTO> items = new ArrayList<>();

    private Order.OrderStatus status;

    private BigDecimal totalAmount;

    private LocalDateTime orderDate;

    public static OrderDTO fromEntity(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderDate(order.getOrderDate());

        order.getItems().forEach(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setBookId(item.getBook().getId());
            itemDTO.setBookTitle(item.getBook().getTitle());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPrice(item.getPrice());
            dto.getItems().add(itemDTO);
        });

        return dto;
    }
}

