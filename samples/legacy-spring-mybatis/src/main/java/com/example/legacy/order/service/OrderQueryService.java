package com.example.legacy.order.service;

import com.example.legacy.order.dto.OrderResponse;
import com.example.legacy.order.mapper.OrderMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryService {

    @Autowired
    private OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public OrderResponse findOrderDetail(Long orderId) {
        return orderMapper.selectOrderDetail(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findRecentOrders() {
        return orderMapper.selectRecentOrders();
    }
}
