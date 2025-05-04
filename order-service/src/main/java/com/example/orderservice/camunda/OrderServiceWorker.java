package com.example.orderservice.camunda;

import com.example.orderservice.dto.ItemDto;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderStatus;
import com.example.orderservice.service.OrderService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.kafka.annotation.KafkaListener;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@AllArgsConstructor
public class OrderServiceWorker {

    private final OrderService orderService;

    @JobWorker(type = "createOrder", autoComplete = true)
    public Map<String, Object> createOrder(
            @Variable(name = "items") List<Map<String, Object>> rawItems,
            @Variable String deliveryLocation) {

        log.info("[Camunda] createOrder");

        List<ItemDto> items = rawItems.stream()
                .map(m -> new ItemDto(
                        (String) m.get("itemId"),
                        ((Number) m.get("quantity")).intValue()
                ))
                .collect(Collectors.toList());

        OrderDto order = orderService.createOrder(
                OrderDto.builder()
                        .requestedItems(items)
                        .deliveryLocation(deliveryLocation)
                        .status(OrderStatus.CREATED.name())
                        .build()
        );

        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", order.getOrderId());
        vars.put("status", order.getStatus());
        return vars;
    }

    @JobWorker(type = "cancelOrder", autoComplete = true)
    public void cancelOrder(@Variable String orderId) {
        log.info("[Camunda][Compensation] cancelOrder {}", orderId);

        OrderDto order = orderService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(OrderStatus.CANCELLED.name());
        orderService.updateStatus(orderId, order.getStatus());
    }

    @JobWorker(type = "completeOrder", autoComplete = true)
    public void completeOrder(@Variable String orderId) {
        log.info("[Camunda] completeOrder {}", orderId);
        orderService.updateStatus(orderId, OrderStatus.COMPLETED.name());
    }


}