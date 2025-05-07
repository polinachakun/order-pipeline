//package com.example.orchestration;
//
//import com.example.dto.OrderDto;
//import com.example.orchestration.OrchestrationStrategy;
//import com.example.orchestration.OrchestrationMode;
//import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class KafkaOrchestrationStrategy implements OrchestrationStrategy {
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//    private final KafkaConfig kafkaConfig;
//
//    @Override
//    public void startOrderProcess(OrderDto orderRequest) {
//        String orderId = UUID.randomUUID().toString();
//
//        // Create order
//        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
//                .orderId(orderId)
//                .customerId(orderRequest.getCustomerId())
//                .items(orderRequest.getItems())
//                .deliveryAddress(orderRequest.getDeliveryAddress())
//                .build();
//
//        kafkaTemplate.send(kafkaConfig.getOrders().getTopic(), orderId, orderCreatedEvent);
//    }
//
//
//    @Override
//    public void cancelOrder(String orderId) {
//        OrderCancelledEvent event = OrderCancelledEvent.builder()
//                .orderId(orderId)
//                .reason("Customer cancellation")
//                .build();
//
//        kafkaTemplate.send(kafkaConfig.getOrdersStatusUpdate().getTopic(), orderId, event);
//    }
//
//    @Override
//    public OrchestrationMode getMode() {
//        return OrchestrationMode.KAFKA;
//    }
//}