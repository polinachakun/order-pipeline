package com.example.warehouseservice.consumer;

import com.example.warehouseservice.dto.AbstractDto;
import com.example.warehouseservice.dto.OrderDto;
import com.example.warehouseservice.service.WarehouseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class WarehouseConsumer {

    private final WarehouseService warehouseService;

    @KafkaListener(
            topics = "${kafka.orders.topic}",
            groupId = "${kafka.orders.group-id}",
            containerFactory = "objectsKafkaListenerContainerFactory"
    )
    public void consumeNewOrder(AbstractDto dto) {
        if (dto == null) {
            log.error("Received null order");
            return;
        }

        try {
            if (dto instanceof OrderDto orderDto) {
                if (orderDto.getOrderId() == null) {
                    log.error("Received order with null orderId");
                    return;
                }

                log.info("Warehouse received new order: {}", orderDto.getOrderId());
                warehouseService.processNewOrder(orderDto);
            } else {
                log.warn("Received unknown DTO type: {}", dto.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error processing order: {}", e.getMessage(), e);
        }
    }
}
