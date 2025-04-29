package com.example.warehouseservice.consumer;

import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.service.WarehouseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class FactoryConsumer {

    private final WarehouseService warehouseService;

    @KafkaListener(
            topics = "${kafka.factory.topic}",
            groupId = "${kafka.factory.group-id}",
            containerFactory = "objectsKafkaListenerContainerFactory"
    )
    public void consumeFactorySuppliedItem(ItemDto event) {
        if (event == null) {
            throw new IllegalArgumentException("Received null ItemRequestReadyEvent");
        }

        log.info("Warehouse received ready item SKU: {}, Quantity: {}", event.getItemId(), event.getQuantity());

        warehouseService.addStockAndRecheckPendingOrders(event);
    }
}
