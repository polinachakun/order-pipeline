package com.example.producer;

import com.example.model.Order;
import com.example.model.OrderItem;
import com.example.producer.events.OrderCreatedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class OrderProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${kafka.orders.topic}")
    private String ordersTopic;

    @Value("${spring.application.name}")
    private String applicationName;

    public OrderProducer(@Qualifier("orderKafkaTemplate") KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        // Uncomment the line below to enable automatic order simulation
        // startOrderSimulation();
    }

    private void startOrderSimulation() {
        scheduler.scheduleAtFixedRate(this::generateAndSendOrder, 0, 30, TimeUnit.SECONDS);
    }

    public void sendOrder(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getOrderId(),
                order.getLocation(),
                order.getOrderDate(),
                order.getItems(),
                "CREATED",
                Instant.now()
        );
        kafkaTemplate.send(new ProducerRecord<>(ordersTopic, event.getOrderId(), event));
        System.out.println("[" + applicationName + "] Sent order to Kafka: " + event.getOrderId());
    }

    private void generateAndSendOrder() {
        List<OrderItem> items = generateRandomItems();
        Order order = new Order(
                UUID.randomUUID().toString(),
                LocalDate.now(),
                getRandomLocation(),
                items
        );
        sendOrder(order);
    }

    private List<OrderItem> generateRandomItems() {
        List<OrderItem> items = new ArrayList<>();
        int itemCount = random.nextInt(2) + 1;
        for (int i = 0; i < itemCount; i++) {
            String itemId = getRandomItemId();
            int quantity = random.nextInt(5) + 1;
            items.add(new OrderItem(itemId, quantity));
        }
        return items;
    }

    private String getRandomItemId() {
        // Using the format from the README example: ITEM-001, ITEM-002
        String[] itemIds = {"ITEM-001", "ITEM-002", "ITEM-003"};
        return itemIds[random.nextInt(itemIds.length)];
    }

    private String getRandomLocation() {
        String[] locations = {"Zurich", "Bern", "Geneva", "Lausanne", "Basel"};
        return locations[random.nextInt(locations.length)];
    }
}
