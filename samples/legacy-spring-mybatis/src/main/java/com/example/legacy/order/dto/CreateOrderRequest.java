package com.example.legacy.order.dto;

import java.util.List;

public record CreateOrderRequest(
        Long customerId,
        List<Long> productIds,
        String memo
) {
}
