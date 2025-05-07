package com.example.orderservice.producer;

import com.example.orderservice.config.KafkaTopicConfig;
import com.example.orderservice.dto.AbstractDto;
import com.example.orderservice.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, AbstractDto> objectKafkaTemplate;
    private final KafkaTopicConfig kafkaTopicConfig;

    public void sendOrder(OrderDto event) {
//        OrderDto event = new OrderCreatedEvent(
//                order.getOrderId(),
//                order.getDeliveryLocation(),
//                order.getOrderDate(),
//                order.getRequestedItems(),
//                "CREATED",
//                Instant.now()
//        );

        objectKafkaTemplate.send(kafkaTopicConfig.getOrdersTopicName(), event);
        // AbstractDto is the super‐type of OrderCreatedEvent
        log.info("[order-service] Sent OrderCreatedEvent for orderId={}", event.getOrderId());
    }
}
//
//    private void generateAndSendOrder() {
//        List<OrderItem> items = generateRandomItems();
//        Order order = new Order(
//                UUID.randomUUID().toString(),
//                getRandomLocation(),
//                items
//        );
//        sendOrder(order);
//    }

//    private List<OrderItem> generateRandomItems() {
//        List<OrderItem> items = new ArrayList<>();
//        int itemCount = random.nextInt(2) + 1;
//        for (int i = 0; i < itemCount; i++) {
//            String itemId = getRandomItemId();
//            int quantity = random.nextInt(5) + 1;
//            items.add(new OrderItem(itemId, quantity));
//        }
//        return items;
//    }
//
//    private String getRandomItemId() {
//        // Using the format from the README example: ITEM-001, ITEM-002
//        String[] itemIds = {"ITEM-001", "ITEM-002", "ITEM-003"};
//        return itemIds[random.nextInt(itemIds.length)];
//    }
//
//    private String getRandomLocation() {
//        String[] locations = {"Zurich", "Bern", "Geneva", "Lausanne", "Basel"};
//        return locations[random.nextInt(locations.length)];
//    }



