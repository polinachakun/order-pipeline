package com.example.warehouseservice.camunda;

import com.example.warehouseservice.dto.ItemDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WarehouseProcessService {

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ObjectMapper objectMapper;

    public String triggerStockCheck(String orderId, String items) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", orderId);
            variables.put("items", items);

            log.info("Triggering stock check for order: {}", orderId);

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


    public void notifyStockAdded(String itemId, Integer quantity) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("itemId", itemId);
            variables.put("quantity", quantity);

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
            topics = "${kafka.factory.topic}",
            groupId = "${kafka.factory.camunda-group-id}",
            containerFactory = "objectsKafkaListenerContainerFactory"
    )
    public void onStockAdded(ItemDto item) {
        if (item == null || item.getItemId() == null) {
            log.warn("Ignoring StockAdded without orderId: {}", item);
            return;
        }

        log.info("Received StockAdded from Factory → itemId={}, qty={}",
                item.getItemId(), item.getQuantity());
        try {
            zeebeClient
                    .newPublishMessageCommand()
                    .messageName("StockAdded")
                    .correlationKey(item.getItemId())
                    .variables(Map.of(
                            "itemId", item.getItemId(),
                            "quantity", item.getQuantity()
                    ))
                    .send()
                    .join();
            log.info("Successfully published StockAdded message for item: {}", item.getItemId());

        } catch (Exception e) {
            log.error("Failed to publish message", e);
        }
    }
}
