//package com.example.consumer;
//
//import com.example.orderservice.repository.OrderRepository;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//public class OrderStatusUpdateConsumer {
//    private final OrderRepository orderRepository;
//
//    public OrderStatusUpdateConsumer(OrderRepository orderRepository) {
//        this.orderRepository = orderRepository;
//    }
//
//    @KafkaListener(topics = "ordersStatusUpdateTopic", groupId = "order-group")
//    public void listenStatusUpdate(ConsumerRecord<String, String> record) {
//        // For now, let's just log the message
//        String value = record.value();
//        System.out.println("Received status update: " + value);
//
//        // Extract orderId and status from the message
//        // This is a simple approach - in a real system, you'd use proper JSON parsing
//        if (value.contains("orderId") && value.contains("status")) {
//            String orderId = extractValue(value, "orderId");
//            String status = extractValue(value, "status");
//
//            if (orderId != null && status != null) {
//                orderRepository.updateStatus(orderId, status);
//                System.out.println("Updated order status to " + status + " for orderId: " + orderId);
//            }
//        }
//    }
//
//    @KafkaListener(topics = "orderServiceStatusUpdateTopic", groupId = "order-group")
//    public void listenStatusUpdateByDelivery(ConsumerRecord<String, String> record) {
//        // For now, let's just log the message
//        String value = record.value();
//        System.out.println("Received status update: " + value);
//
//        // Extract orderId and status from the message
//        // This is a simple approach - in a real system, you'd use proper JSON parsing
//        if (value.contains("orderId") && value.contains("status")) {
//            String orderId = extractValue(value, "orderId");
//            String status = extractValue(value, "status");
//
//            if (orderId != null && status != null) {
//                orderRepository.updateStatus(orderId, status);
//                System.out.println("Updated order status to " + status + " for orderId: " + orderId);
//            }
//        }
//    }
//
//    private String extractValue(String json, String key) {
//        int keyIndex = json.indexOf("\"" + key + "\"");
//        if (keyIndex == -1) return null;
//
//        int valueStart = json.indexOf(":", keyIndex) + 1;
//        int valueEnd = json.indexOf(",", valueStart);
//        if (valueEnd == -1) {
//            valueEnd = json.indexOf("}", valueStart);
//        }
//
//        if (valueStart != -1 && valueEnd != -1) {
//            String value = json.substring(valueStart, valueEnd).trim();
//            // Remove quotes if present
//            if (value.startsWith("\"") && value.endsWith("\"")) {
//                value = value.substring(1, value.length() - 1);
//            }
//            return value;
//        }
//
//        return null;
//    }
//}

package com.example.orderservice.consumer;

import com.example.orderservice.dto.OrderStatusUpdateEventDto;
import com.example.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderStatusUpdateConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderStatusUpdateConsumer.class);
    private final OrderService orderService;


    @KafkaListener(
            topics  = {"ordersStatusUpdateTopic", "orderServiceStatusUpdateTopic"},
            groupId = "${kafka.ordersStatusUpdate.group-id}",
            containerFactory = "objectsKafkaListenerContainerFactory"

    )
    public void handleOrderStatusUpdateEvent(OrderStatusUpdateEventDto event) {
        if (event == null) {
            log.warn("Received null OrderStatusUpdateEvent");
            return;
        }

        orderService.updateStatus(event.getOrderId(), event.getStatus());
        log.info("Received OrderStatusUpdateEvent: {}", event);

    }

}
