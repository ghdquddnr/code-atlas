package com.example.legacy.order.mapper;

import com.example.legacy.order.dto.CreateOrderRequest;
import com.example.legacy.order.dto.OrderResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {

    void insertOrder(CreateOrderRequest request);

    OrderResponse selectOrderDetail(@Param("orderId") Long orderId);

    List<OrderResponse> selectRecentOrders();

    void updateOrderStatus(@Param("orderId") Long orderId, @Param("status") String status);

    void deleteOrder(@Param("orderId") Long orderId);
}
