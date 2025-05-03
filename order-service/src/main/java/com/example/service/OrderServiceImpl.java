package com.example.service;

import com.example.dto.ItemDto;
import com.example.dto.OrderDto;
import com.example.exception.InvalidDeliveryLocationException;
import com.example.exception.InvalidOrderSpecificationException;
import com.example.model.Order;
import com.example.model.OrderItem;
import com.example.producer.OrderProducer;
import com.example.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repo;
    private final OrderProducer producer;
    private final RestTemplate rest;
    private final OrderValidationService validator;

    @Value("${warehouse.service.url:http://localhost:8082}")
    private String warehouseUrl;

    @Override
    public OrderDto createOrder(OrderDto dto) {
//        validator.validateOrderDto(dto);

        Order order = toModel(dto);
        Order saved = repo.save(order);

        producer.sendOrder(saved);

        callWarehouse(saved);

        return toDto(saved);
    }

    @Override
    public OrderDto findById(String orderId) {
        Order order = repo.findById(orderId);
        return toDto(order);
    }

    @Override
    public OrderDto updateStatus(String orderId, String newStatus) {
        Order updated = repo.updateStatus(orderId, newStatus);
        return toDto(updated);
    }

    private void callWarehouse(Order order) {
        OrderDto dto = toDto(order);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderDto> req = new HttpEntity<>(dto, headers);
        rest.postForEntity(warehouseUrl + "/orders/process", req, Void.class);
    }

    private Order toModel(OrderDto dto) {
        return new Order(
                dto.getOrderId(),
                dto.getDeliveryLocation(),
                dto.getRequestedItems().stream()
                        .map(i -> new OrderItem(i.getItemId(), i.getQuantity()))
                        .collect(Collectors.toList())
        );
    }

    private OrderDto toDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getOrderDate())
                .deliveryLocation(order.getDeliveryLocation())
                .status(order.getStatus())
                .requestedItems(order.getRequestedItems().stream()
                        .map(i -> new ItemDto(i.getItemId(), i.getQuantity()))
                        .collect(Collectors.toList()))
                .build();
    }

}
