package com.example.orderservice.service;

import com.example.orderservice.dto.ItemDto;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.producer.OrderProducer;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repo;
    private final OrderProducer producer;
    private final RestTemplate rest;
    private final OrderValidationService validator;

//    @Value("${warehouse.service.url:http://localhost:8084}")
//    private String warehouseUrl;

    @Override
    public OrderDto createOrder(OrderDto dto) {
        log.info("Received createOrder request: {}", dto);

        // validator.validateOrderDto(dto);
        log.debug("Order DTO passed validation: {}", dto);

        if (dto.getOrderId() == null || dto.getOrderId().isBlank()) {
            dto.setOrderId(UUID.randomUUID().toString());
        }

        Order order = toModel(dto);

        Order saved = repo.save(order);
        log.info("Saved Order to repository with ID {}", saved.getOrderId());

        producer.sendOrder(dto);
        log.info("Published Order {} to Kafka", saved.getOrderId());

//        callWarehouse(saved);

        OrderDto result = toDto(saved);

        return result;
    }

    @Override
    public Optional<OrderDto> findById(String orderId) {
        Order order = repo.findById(orderId);
        OrderDto dto = toDto(order);
        log.info("Found Order: {}, mapped to DTO: {}", order, dto);
        return Optional.ofNullable(dto);
    }

    @Override
    public OrderDto updateStatus(String orderId, String newStatus) {
        Order updated = repo.updateStatus(orderId, newStatus);
        OrderDto dto = toDto(updated);
        log.info("Updated Order status, new DTO: {}", dto);
        return dto;
    }

//    private void callWarehouse(Order order) {
//        OrderDto dto = toDto(order);
//        log.info("Sending Order {} to warehouse at {}", order.getOrderId(), warehouseUrl);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<OrderDto> req = new HttpEntity<>(dto, headers);
//
//        try {
//            rest.postForEntity(warehouseUrl + "/orders/process", req, Void.class);
//            log.info("Successfully sent Order {} to warehouse", order.getOrderId());
//        } catch (Exception e) {
//            log.error("Error sending Order {} to warehouse: {}", order.getOrderId(), e.getMessage(), e);
//        }
//    }

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
