package com.example.orderservice.camunda;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderStatus;
import com.example.orderservice.dto.OrderStatusUpdateEventDto;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@AllArgsConstructor
public class OrderProcessService {

    private final ZeebeClient zeebeClient;

    private final OrderService orderService;


    private final ObjectMapper objectMapper;

    public String startOrderSaga(OrderDto orderDto) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("items", orderDto.getRequestedItems());
        variables.put("deliveryLocation", orderDto.getDeliveryLocation());

        ProcessInstanceEvent processInstance = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("OrderSaga")
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        log.info("Started Order Saga process instance: {}", processInstance.getProcessInstanceKey());

        return String.valueOf(processInstance.getProcessInstanceKey());
    }

//    public void notifyDeliveryCompleted(String orderId) {
//        zeebeClient.newPublishMessageCommand()
//                .messageName("DeliveryCompleted")    // must exactly match the BPMN element
//                .correlationKey(orderId)             // matches the process variable “orderId”
//                .variables(Map.of(
//                        "orderId", orderId,
//                        "deliveryStatus", "DELIVERED"
//                ))
//                .send()
//                .join();
//
//        log.info("➡️  Correlated Zeebe message 'DeliveryCompleted' for order={}", orderId);
//    }

//    @KafkaListener(
//            topics           = "${kafka.ordersStatusUpdate.ordersStatusUpdateTopic}",          // e.g. delivery.status
//            groupId =          "${kafka.ordersStatusUpdate.camunda-group-id}",
//            containerFactory = "objectsKafkaListenerContainerFactory"
//    )
//    public void onDeliveryStatus(OrderStatusUpdateEventDto evt) {
//
//        if (evt == null || evt.getOrderId() == null) {
//            log.warn("Ignoring null/invalid DeliveryStatusEvent: {}", evt);
//            return;
//        }
//
//        String orderId = evt.getOrderId();
//        String status = evt.getStatus();
//
//        log.info("Received delivery status: orderId={}, status={}", orderId, status);
//
//        orderService.updateStatus(orderId, status);
//        if ("DELIVERED".equals(status)) {
//            try {
//                zeebeClient.newPublishMessageCommand()
//                        .messageName("deliveryCompleted")  // Use a meaningful message name
//                        .correlationKey(orderId)
//                        .variables(Map.of(
//                                "orderId", orderId,
//                                "deliveryStatus", "DELIVERED"
//                        ))
//                        .send()
//                        .join();
//
//                log.info("Successfully published message for order: {}", orderId);
//            } catch (Exception e) {
//                log.error("Failed to publish message for order: {}", orderId, e);
//            }
//        } else if ("DELIVERY_FAILED".equals(status)) {
//            try {
//                zeebeClient.newPublishMessageCommand()
//                        .messageName("deliveryFailed")  // Use a meaningful message name
//                        .correlationKey(orderId)
//                        .variables(Map.of(
//                                "orderId", orderId,
//                                "deliveryStatus", "DELIVERY_FAILED"
//                        ))
//                        .send()
//                        .join();
//
//
//                log.info("Published delivery failed message for order: {}", orderId);
//            } catch (Exception e) {
//                log.error("Failed to publish delivery failed message for order: {}", orderId, e);
//            }
//        }
//    }
}

