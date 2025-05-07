//package com.example.orderservice.consumer;
//
//import com.example.orderservice.dto.OrderStatusUpdateEventDto;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.camunda.zeebe.client.ZeebeClient;
//import lombok.AllArgsConstructor;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Slf4j
//@Service
//@AllArgsConstructor
//public class DeliveryCamundaConsumer {
//
//    private final ZeebeClient zeebeClient;
//
//    @KafkaListener(
//            topics           = "${kafka.ordersStatusUpdate.delivered-order-service-topic}",
//            groupId          = "${kafka.ordersStatusUpdate.camunda-group-id}",
//            containerFactory = "camundaKafkaListenerContainerFactory"
//    )
//    public void onDeliveryStatus(String payload) throws JsonProcessingException {
//
//        var node = new ObjectMapper().readTree(payload);
//        String orderId = node.get("orderId").asText();
//        String status  = node.get("status").asText();
//
//        log.info("[Camunda‑Delivery] orderId={}, status={}", orderId, status);
//
//        String messageName = switch (status) {
//            case "DELIVERED"       -> "Delivery_Completed";
//            case "DELIVERY_FAILED" -> "Delivery_Failed";
//            default                -> null;
//        };
//
//        if (messageName != null) {
//            zeebeClient.newPublishMessageCommand()
//                    .messageName(messageName)
//                    .correlationKey(orderId)
//                    .variables(Map.of("orderId", orderId))
//                    .send()
//                    .join();
//            log.info("→ Published {} for orderId={}", messageName, orderId);
//        }
//    }
//}
