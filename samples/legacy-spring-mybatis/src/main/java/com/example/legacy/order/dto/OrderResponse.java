package com.example.legacy.order.dto;

import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId,
        Long customerId,
        String status,
        LocalDateTime orderedAt
) {
}
