package com.example.warehouseservice.camunda;

import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.dto.OrderDto;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class WarehouseProcessService {

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * This method is not typically used in warehouse service since
     * warehouse workers respond to tasks from the main process.
     * But it can be useful for testing or manual interventions.
     */
    public String triggerStockCheck(String orderId, String items) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", orderId);
            variables.put("items", items);

            // Note: You typically don't start new processes from warehouse service
            // Instead, you complete existing jobs/tasks
            // This is just for demonstration/testing purposes

            log.info("Triggering stock check for order: {}", orderId);

            // If you need to correlate a message to a waiting process
            zeebeClient.newPublishMessageCommand()
                    .messageName("StockAdded")
                    .correlationKey(orderId)
                    .variables(variables)
                    .send()
                    .join();

            return "Message published";
        } catch (Exception e) {
            log.error("Failed to trigger stock check", e);
            throw new RuntimeException("Failed to trigger stock check: " + e.getMessage(), e);
        }
    }

    /**
     * Notify the process that stock has been added
     */
    public void notifyStockAdded(String itemId, Integer quantity) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("itemId", itemId);
            variables.put("quantity", quantity);

            // Publish message event that the process is waiting for
            zeebeClient.newPublishMessageCommand()
                    .messageName("StockAdded")
                    .correlationKey(itemId)  // Use itemId as correlation key
                    .variables(variables)
                    .send()
                    .join();

            log.info("Stock added notification sent for item: {}", itemId);
        } catch (Exception e) {
            log.error("Failed to notify stock added", e);
            throw new RuntimeException("Failed to notify stock added: " + e.getMessage(), e);
        }
    }

    /**
     * Complete a manual task (if needed)
     */
    public void completeManualTask(String jobKey, Map<String, Object> variables) {
        try {
            zeebeClient.newCompleteCommand(Long.parseLong(jobKey))
                    .variables(variables)
                    .send()
                    .join();

            log.info("Manual task completed: {}", jobKey);
        } catch (Exception e) {
            log.error("Failed to complete manual task", e);
            throw new RuntimeException("Failed to complete task: " + e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics           = "${kafka.factory.topic}",
            groupId          = "${kafka.factory.group-id}",
            containerFactory = "objectsKafkaListenerContainerFactory"
    )
    public void onStockAdded(ItemDto item) {
        if (item == null || item.getItemId() == null) {
            log.warn("Ignoring StockAdded without orderId: {}", item);
            return;
        }

        log.info("📦 Received StockAdded from Factory → orderId={}, itemId={}, qty={}",
                item.getItemId(), item.getItemId(), item.getQuantity());

        // Publish the Zeebe message
        zeebeClient
                .newPublishMessageCommand()
                .messageName("StockAdded")                   // must match your BPMN catch‐message name
                .correlationKey(item.getItemId())           // must match the process variable “orderId”
                .variables(Map.of(                           // pass along any extra payload you need
                        "orderId",  item.getItemId(),
                        "itemId",   item.getItemId(),
                        "quantity", item.getQuantity()
                ))
                .send()
                .join();

        log.info("➡️  Correlated Zeebe message 'StockAdded' for order={}", item.getItemId());
    }
}