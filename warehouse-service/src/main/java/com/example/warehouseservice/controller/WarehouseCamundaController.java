package com.example.warehouseservice.controller;

import com.example.warehouseservice.camunda.WarehouseProcessService;
import com.example.warehouseservice.dto.ItemDto;
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
@RequestMapping("/api/warehouse/camunda")
public class WarehouseCamundaController {

    @Autowired
    private WarehouseProcessService warehouseProcessService;

    /**
     * Endpoint to notify Camunda process that stock has been added
     * This will trigger the message event in the BPMN process
     */
    @PostMapping("/stock-added")
    public ResponseEntity<ProcessResponse> notifyStockAdded(@RequestBody StockNotification notification) {
        log.info("Received stock added notification: {}", notification);

        try {
            warehouseProcessService.notifyStockAdded(
                    notification.getItemId(),
                    notification.getQuantity()
            );

            ProcessResponse response = ProcessResponse.builder()
                    .message("Stock added notification sent successfully")
                    .status("SUCCESS")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to notify stock added", e);

            ProcessResponse errorResponse = ProcessResponse.builder()
                    .message("Failed to notify stock added: " + e.getMessage())
                    .status("ERROR")
                    .build();

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Endpoint to manually trigger stock check (for testing purposes)
     */
    @PostMapping("/trigger-stock-check")
    public ResponseEntity<ProcessResponse> triggerStockCheck(@RequestBody StockCheckRequest request) {
        log.info("Manually triggering stock check for order: {}", request.getOrderId());

        try {
            String result = warehouseProcessService.triggerStockCheck(
                    request.getOrderId(),
                    request.getItems()
            );

            ProcessResponse response = ProcessResponse.builder()
                    .message("Stock check triggered: " + result)
                    .status("SUCCESS")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to trigger stock check", e);

            ProcessResponse errorResponse = ProcessResponse.builder()
                    .message("Failed to trigger stock check: " + e.getMessage())
                    .status("ERROR")
                    .build();

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Endpoint to complete a manual task
     */
    @PostMapping("/complete-task")
    public ResponseEntity<ProcessResponse> completeTask(@RequestBody TaskCompletionRequest request) {
        log.info("Completing manual task: {}", request.getJobKey());

        try {
            warehouseProcessService.completeManualTask(
                    request.getJobKey(),
                    request.getVariables()
            );

            ProcessResponse response = ProcessResponse.builder()
                    .message("Task completed successfully")
                    .status("SUCCESS")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to complete task", e);

            ProcessResponse errorResponse = ProcessResponse.builder()
                    .message("Failed to complete task: " + e.getMessage())
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
class ProcessResponse {
    private String message;
    private String status;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class StockNotification {
    private String itemId;
    private Integer quantity;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class StockCheckRequest {
    private String orderId;
    private String items;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TaskCompletionRequest {
    private String jobKey;
    private Map<String, Object> variables;
}
