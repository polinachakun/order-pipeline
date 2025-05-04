package com.example.deliveryservice.controller;

import com.example.deliveryservice.camunda.DeliveryProcessService;
import com.example.deliveryservice.dto.StartDeliveryCommand;
import com.example.deliveryservice.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/delivery/camunda")
public class DeliveryCamundaController {

    @Autowired
    private DeliveryProcessService deliveryProcessService;

    @Autowired
    private DeliveryService deliveryService;

    /**
     * Manually trigger delivery completion (for testing)
     */
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse> completeDelivery(@RequestBody DeliveryCompletionRequest request) {
        log.info("Manually completing delivery for order: {}", request.getOrderId());

        try {
            deliveryProcessService.triggerDeliveryCompletion(
                    request.getOrderId(),
                    request.isSuccess()
            );

            ApiResponse response = ApiResponse.builder()
                    .message("Delivery completion triggered")
                    .status("SUCCESS")
                    .data(request)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to trigger delivery completion", e);

            ApiResponse errorResponse = ApiResponse.builder()
                    .message("Failed to trigger delivery completion: " + e.getMessage())
                    .status("ERROR")
                    .build();

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get all deliveries
     */
    @GetMapping("/deliveries")
    public ResponseEntity<ApiResponse> getAllDeliveries() {
        try {
            var deliveries = deliveryService.findAll();

            ApiResponse response = ApiResponse.builder()
                    .message("Deliveries retrieved successfully")
                    .status("SUCCESS")
                    .data(deliveries)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get deliveries", e);

            ApiResponse errorResponse = ApiResponse.builder()
                    .message("Failed to get deliveries: " + e.getMessage())
                    .status("ERROR")
                    .build();

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Manually start a delivery (for testing)
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse> startDelivery(@RequestBody StartDeliveryRequest request) {
        log.info("Manually starting delivery for order: {}", request.getOrderId());

        try {
            StartDeliveryCommand command = new StartDeliveryCommand();
            command.setOrderId(request.getOrderId());
            command.setDeliveryLocation(request.getDeliveryLocation());

            String deliveryId = deliveryService.handleStartDeliveryCommand(command);

            ApiResponse response = ApiResponse.builder()
                    .message("Delivery started successfully")
                    .status("SUCCESS")
                    .data(Map.of("deliveryId", deliveryId))
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to start delivery", e);

            ApiResponse errorResponse = ApiResponse.builder()
                    .message("Failed to start delivery: " + e.getMessage())
                    .status("ERROR")
                    .build();

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Force a delivery to fail (for testing)
     */
    @PostMapping("/fail/{orderId}")
    public ResponseEntity<ApiResponse> failDelivery(@PathVariable String orderId) {
        log.info("Forcing delivery failure for order: {}", orderId);

        try {
            // This is a test endpoint to force delivery failure
            deliveryProcessService.triggerDeliveryCompletion(orderId, false);

            ApiResponse response = ApiResponse.builder()
                    .message("Delivery failure triggered")
                    .status("SUCCESS")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to trigger delivery failure", e);

            ApiResponse errorResponse = ApiResponse.builder()
                    .message("Failed to trigger delivery failure: " + e.getMessage())
                    .status("ERROR")
                    .build();

            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

// DTO classes
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ApiResponse {
    private String message;
    private String status;
    private Object data;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class DeliveryCompletionRequest {
    private String orderId;
    private boolean success;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class StartDeliveryRequest {
    private String orderId;
    private String deliveryLocation;
}
