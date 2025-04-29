package com.example.warehouseservice.orders;

import com.example.warehouseservice.dto.OrderDto;
import com.example.warehouseservice.dto.OrderStatus;
import com.example.warehouseservice.dto.OrderStatusUpdateEventDto;
import com.example.warehouseservice.orders.repository.OrderRepository;
import com.example.warehouseservice.publisher.WarehouseEventPublisher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WarehouseEventPublisher eventPublisher;

    @Override
    public void initializeNewOrder(OrderDto orderDto) {
        orderDto.setStatus(OrderStatus.READY_FOR_PICKING);
        orderRepository.save(orderDto);
        publishStatusUpdate(orderDto.getOrderId(), OrderStatus.READY_FOR_PICKING);

        log.info("Order {} saved, status={}", orderDto, OrderStatus.READY_FOR_PICKING);
    }

    @Override
    public void markOrderAsFullyAvailable(String orderId) {
        log.info("All items are available, Order is {}.", OrderStatus.FULFILLMENT);
        updateOrderStatus(orderId, OrderStatus.FULFILLMENT);

        log.info("Order is {}", OrderStatus.READY_FOR_PACKING);
        updateOrderStatus(orderId, OrderStatus.READY_FOR_PACKING);

        log.info("Order is {}", OrderStatus.PACKED);
        updateOrderStatus(orderId, OrderStatus.PACKED);

        log.info("Order is {}", OrderStatus.SENT_TO_DELIVERY);
        updateOrderStatus(orderId, OrderStatus.SENT_TO_DELIVERY);
    }

    @Override
    public void markOrderAsPartiallyAvailable(String orderId, List<String> missingSkus) {
        log.info("Order {} {}. Missing items: {}", orderId, OrderStatus.PARTIALLY_FULFILLMENT, missingSkus);
        updateOrderStatus(orderId, OrderStatus.PARTIALLY_FULFILLMENT);
    }

    @Override
    public void markOrderAsReadyForPicking(String orderId) {
        log.info("Order {} completely out of stock. Status={}", orderId, OrderStatus.READY_FOR_PICKING);
        updateOrderStatus(orderId, OrderStatus.READY_FOR_PICKING);
    }

    @Override
    public void markOrderAsCancelled(String orderId) {
        log.info("Order {} is cancelled due to failed delivery. Status={}", orderId, OrderStatus.DELIVERY_FAILED_ORDER_CANCELLED);
        updateOrderStatus(orderId, OrderStatus.DELIVERY_FAILED_ORDER_CANCELLED);
    }


    @Override
    public List<OrderDto> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public List<OrderDto> getOrdersByStatuses(OrderStatus... statuses) {
        return orderRepository.findByStatuses(Arrays.asList(statuses));
    }

    private void updateOrderStatus(String orderId, OrderStatus status) {
        orderRepository.updateStatus(orderId, status);
        publishStatusUpdate(orderId, status);
    }

    private void publishStatusUpdate(String orderId, OrderStatus status) {
        OrderStatusUpdateEventDto event = new OrderStatusUpdateEventDto(orderId, status.name());
        eventPublisher.publishOrderStatusUpdate(event);
    }
}
