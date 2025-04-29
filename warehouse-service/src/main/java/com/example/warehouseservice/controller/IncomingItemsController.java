package com.example.warehouseservice.controller;

import com.example.warehouseservice.config.KafkaTopicConfig;
import com.example.warehouseservice.dto.AbstractDto;
import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.inventory.InventoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/factory/items")
@AllArgsConstructor
@Slf4j
public class IncomingItemsController {

    private final KafkaTemplate<String, AbstractDto> objectKafkaTemplate;
    private final KafkaTopicConfig kafkaTopicConfig;

    private final InventoryService inventoryService;

    @PostMapping("/publish")
    public ResponseEntity<String> sendNewOrderToKafka(@RequestBody ItemDto itemDto) {

        log.info("publish item to the kafka topic " + kafkaTopicConfig.getFactorySuppliedItemsTopicName() + " " + itemDto);

        objectKafkaTemplate.send(kafkaTopicConfig.getFactorySuppliedItemsTopicName(), itemDto);

        return ResponseEntity.ok("Message sent to Kafka topic " + kafkaTopicConfig.getFactorySuppliedItemsTopicName());
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getInventory() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

}
