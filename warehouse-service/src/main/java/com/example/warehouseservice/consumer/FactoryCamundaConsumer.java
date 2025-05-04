package com.example.warehouseservice.consumer;

import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.service.WarehouseService;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryCamundaConsumer {

    private final ZeebeClient     zeebeClient;
    private final WarehouseService warehouseService;

    /**
     * In parallel to the existing FactoryConsumer, listen for stock‐added events
     * and publish a Zeebe message "StockAdded" correlated on itemId.
     */
    @KafkaListener(
            topics           = "${kafka.factory.topic}",
            groupId          = "${kafka.factory.camunda-group-id}", // e.g. "camunda-group"
            containerFactory = "objectsKafkaListenerContainerFactory"
    )
    public void onStockAdded(ItemDto item) {
        if (item == null || item.getItemId() == null) {
            log.warn("Skipping null/bad ItemDto for Camunda correlation: {}", item);
            return;
        }

        log.info("[Camunda] received stock for item={}, qty={}",
                item.getItemId(), item.getQuantity());

        // Optionally still update inventory & pending‐order logic:
        warehouseService.addStockAndRecheckPendingOrders(item);

        // Correlate into Zeebe on itemId
        zeebeClient
                .newPublishMessageCommand()
                .messageName("StockAdded")          // must match BPMN message name
                .correlationKey(item.getItemId())   // catch on `itemId` variable
                .variables(Map.of(
                        "itemId",   item.getItemId(),
                        "quantity", item.getQuantity()
                ))
                .send()
                .join();

        log.info("[Camunda] published StockAdded message for itemId={}", item.getItemId());
    }
}
