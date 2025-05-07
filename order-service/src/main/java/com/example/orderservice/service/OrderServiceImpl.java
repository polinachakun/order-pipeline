package com.example.orderservice.service;

import com.example.orderservice.command.dto.StartOrderCommand;
import com.example.orderservice.dto.ItemDto;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.producer.OrderProducer;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.retry.StatefulRetryCommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.orderservice.dto.OrderStatus.CREATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repo;
    private final OrderProducer producer;
    private final RestTemplate rest;
    private final OrderValidationService validator;
    private final StatefulRetryCommandHandler command;

    @Override
    public OrderDto createOrder(OrderDto dto) {
        log.info("Received createOrder request: {}", dto);

        // validator.validateOrderDto(dto);

        log.debug("Order DTO passed validation: {}", dto);

        if (dto.getOrderId() == null || dto.getOrderId().isBlank()) {
            dto.setOrderId(UUID.randomUUID().toString());
        }
        if (dto.getStatus() == null) {
            dto.setStatus(String.valueOf(CREATED));
        }

        Order order = toModel(dto);

        Order saved = repo.save(order);
        log.info("Saved Order to repository with ID {}", saved.getOrderId());


        StartOrderCommand payload = new StartOrderCommand(
                dto.getOrderId(),
                dto.getDeliveryLocation(),
                dto.getRequestedItems(),
                dto.getStatus()
        );

        log.info("Sending delivery command for order {}", dto.getOrderId());
        command.handle(payload);
//        producer.sendOrder(dto);
//        log.info("Published Order {} to Kafka", saved.getOrderId());

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
