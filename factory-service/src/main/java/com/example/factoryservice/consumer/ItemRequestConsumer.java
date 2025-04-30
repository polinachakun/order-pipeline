package com.example.factoryservice.consumer;


import com.example.factoryservice.dto.ItemDto;
import com.example.factoryservice.service.FactoryStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestConsumer {

    private final FactoryStockService factoryStockService;

    @KafkaListener(
            topics = "${kafka.itemRequest.topic}",
            groupId = "${kafka.itemRequest.group-id}",
            containerFactory = "itemDtoListenerContainerFactory"
    )
    public void listenItemRequest(ItemDto request) {
        if (request == null) {
            log.warn("Received null ItemDto from Warehouse");
            return;
        }
        log.info("Factory received item-request: {} x{}", request.getItemId(), request.getQuantity());

        try {
            factoryStockService.onItemRequest(request);
        } catch (Exception ex) {
            log.error("Failed to process ItemDto {}, will retry", request, ex);
            throw ex;
        }
    }
}

