package com.example.warehouseservice.consumer;

import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryConsumer {

    private final WarehouseService warehouseService;

    @KafkaListener(
            topics = "${kafka.factory.topic}",
            groupId = "${kafka.factory.group-id}",
            containerFactory = "objectsKafkaListenerContainerFactory"
    )
    public void handleStockAdded(ItemDto item) {

        if (item == null) {
            log.warn("Received null ItemDto from Factory");
            return;
        }
        if (item.getItemId() == null) {
            log.error("StockAdded event with null itemId: {}", item);
            return;
        }

        log.info("Warehouse received stock from Factory: {} x{}",
                item.getItemId(), item.getQuantity());

        try {
            warehouseService.addStockAndRecheckPendingOrders(item);
        } catch (Exception ex) {
            log.error("Error adding stock for item {} – retrying", item.getItemId(), ex);
            throw ex;
        }
    }
}
