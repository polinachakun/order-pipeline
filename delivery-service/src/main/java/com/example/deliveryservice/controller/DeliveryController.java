package com.example.deliveryservice.controller;

import com.example.deliveryservice.dto.StartDeliveryCommand;
import com.example.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping()
    public ResponseEntity<String> startDelivery(@RequestBody StartDeliveryCommand command) {
        if (command == null) {
            log.error("Received null delivery command");
            return ResponseEntity.badRequest().body("Command cannot be null");
        }

        log.info("Received start delivery command: {}", command);

        // Fix: Only call handleStartDeliveryCommand once
        String deliveryId = deliveryService.handleStartDeliveryCommand(command);

        if (deliveryId == null) {
            return ResponseEntity.badRequest().body("Failed to start delivery");
        }

        return ResponseEntity.ok(deliveryId);
    }

    @GetMapping
    public ResponseEntity<List<StartDeliveryCommand>> getDeliveries() {
        return ResponseEntity.ok(deliveryService.findAll());
    }

}
