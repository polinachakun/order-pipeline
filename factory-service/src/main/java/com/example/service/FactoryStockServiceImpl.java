package com.example.service;

import com.example.dto.ItemDto;
import com.example.publisher.FactoryEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryStockServiceImpl implements FactoryStockService{

    private static final int MIN_BATCH = 10;
    private final FactoryEventPublisher publisher;

    public void onItemRequest(ItemDto request) {
        log.info("Factory received item request: {}", request);

        int producedQty = Math.max(request.getQuantity(), MIN_BATCH);

        ItemDto stockAdded = ItemDto.builder()
                .itemId(request.getItemId())
                .quantity(producedQty)
                .build();

        publisher.publishStockAddedTopicName(stockAdded);
        log.info("Factory published stockAdded: {}", stockAdded);
    }

    @Scheduled(fixedDelayString = "${factory.replenish-interval-ms}")
    public void periodicReplenishment() {
        ItemDto demo = ItemDto.builder()
                .itemId("demo-item-id-123")
                .quantity(MIN_BATCH)
                .build();
        publisher.publishStockAddedTopicName(demo);
        log.debug("Factory published scheduled stockAdded: {}", demo);
    }

}
