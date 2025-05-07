package com.example.orderservice.consumer;

import com.example.orderservice.model.Order;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {
    @KafkaListener(topics = "orders", groupId = "order-group")
    public void listenOrder(ConsumerRecord<String, Order> record) {
        System.out.println("Received Order: " + record.value());
    }
}
