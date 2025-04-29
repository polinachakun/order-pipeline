package com.example.warehouseservice.controller;

import com.example.warehouseservice.config.KafkaTopicConfig;
import com.example.warehouseservice.dto.AbstractDto;
import com.example.warehouseservice.dto.OrderDto;
import com.example.warehouseservice.orders.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/orders")
@AllArgsConstructor
@Slf4j
public class IncomingOrdersController {

    private final KafkaTemplate<String, AbstractDto> objectKafkaTemplate;
    private final KafkaTopicConfig kafkaTopicConfig;


    private OrderService orderService;

    @PostMapping("/publish")
    public ResponseEntity<String> sendNewOrderToKafka(@RequestBody OrderDto order) {

        log.info("publish order to the kafka topic " + kafkaTopicConfig.getOrdersTopicName() + " " + order);

        objectKafkaTemplate.send(kafkaTopicConfig.getOrdersTopicName(), order);

        return ResponseEntity.ok("Message sent to Kafka topic " + kafkaTopicConfig.getOrdersTopicName());
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> get() {
        return ResponseEntity.ok(orderService.findAll());
    }

}
