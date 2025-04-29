package com.example.warehouseservice.consumer;

import com.example.warehouseservice.dto.OrderStatusUpdateEventDto;
import com.example.warehouseservice.service.WarehouseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.example.warehouseservice.dto.OrderStatus.DELIVERY_FAILED;

@Service
@AllArgsConstructor
@Slf4j
public class OrdersStatusUpdateConsumer {

    private final WarehouseService warehouseService;

    @KafkaListener(
            topics = "${kafka.ordersStatusUpdate.topic}",
            groupId = "${kafka.ordersStatusUpdate.group-id}",
            containerFactory = "objectsKafkaListenerContainerFactory"
    )
    public void handleOrderStatusUpdateEvent(OrderStatusUpdateEventDto event) {
        if (event == null) {
            log.warn("Received null OrderStatusUpdateEvent");
            return;
        }

        log.info("Received OrderStatusUpdateEvent: {}", event);

        if (DELIVERY_FAILED.name().equals(event.getStatus())) {
            try {
                warehouseService.compensateInventory(event);
            } catch (Exception ex) {
                log.error("Failed to compensate inventory for order {}. Retrying...", event.getOrderId(), ex);
                throw ex;
            }
        }
    }

}
