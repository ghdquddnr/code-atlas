package com.example.legacy.order.service;

import com.example.legacy.order.dto.CreateOrderRequest;
import com.example.legacy.order.dto.OrderResponse;
import com.example.legacy.order.dto.UpdateOrderStatusRequest;
import com.example.legacy.order.mapper.OrderMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderQueryService orderQueryService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        orderMapper.insertOrder(request);
        return orderMapper.selectOrderDetail(request.customerId());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return orderQueryService.findOrderDetail(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> searchOrders() {
        return orderQueryService.findRecentOrders();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        orderMapper.updateOrderStatus(orderId, request.status());
        return orderMapper.selectOrderDetail(orderId);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        orderMapper.deleteOrder(orderId);
    }

    private OrderResponse findOrderDetail(Long orderId) {
        return orderMapper.selectOrderDetail(orderId);
    }
}
