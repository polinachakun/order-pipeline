// --- 1) CONTROLLER ---

package com.example.controller;

import com.example.dto.OrderDto;
import com.example.exception.InvalidDeliveryLocationException;
import com.example.exception.InvalidOrderSpecificationException;
import com.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    @Autowired
    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        OrderDto created = service.createOrder(orderDto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable String orderId) {
        OrderDto dto = service.findById(orderId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable String orderId,
            @RequestParam String status) {
        OrderDto updated = service.updateStatus(orderId, status);
        return ResponseEntity.ok(updated);
    }
}
