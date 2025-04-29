package com.example.warehouseservice.controller;

import com.example.warehouseservice.dto.OrderDto;
import com.example.warehouseservice.service.WarehouseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/orders")
@AllArgsConstructor
@Slf4j
public class WarehouseCommandController {

    private final WarehouseService warehouseService;

    @PostMapping("/process")
    public ResponseEntity<String> processOrder(@RequestBody OrderDto orderDto) {

        log.info("Received process order command for order: {}", orderDto.getOrderId());
        warehouseService.processNewOrder(orderDto);

        return ResponseEntity.ok(orderDto.getOrderId());
    }
}