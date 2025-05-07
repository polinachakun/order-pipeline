package com.example.orderservice.controller;

import com.example.orderservice.camunda.OrderProcessService;
import com.example.orderservice.dto.OrderDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderCamundaController {

    private final OrderProcessService orderProcessService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderDto orderDto) {
        log.info("Received order request: {}", orderDto);

        String processInstanceKey = orderProcessService.startOrderSaga(orderDto);
        log.info("Started process instance {}", processInstanceKey);

        var body = new OrderResponse(
                processInstanceKey,
                "Order process started successfully",
                "STARTED"
        );

        // Location header with the new process instance URI (optional)
        URI location = URI.create("/api/orders/" + processInstanceKey);
        return ResponseEntity
                .created(location)
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OrderError> handleFailure(Exception e) {
        log.error("Failed to start order process", e);
        var err = new OrderError("ERROR", "Failed to start order process: " + e.getMessage());
        return ResponseEntity.status(500).body(err);
    }

    public record OrderResponse(
            String processInstanceId,
            String message,
            String status
    ) {}

    public record OrderError(
            String status,
            String message
    ) {}
}
