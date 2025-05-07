package com.example.orderservice.orchestration;

import com.example.orderservice.dto.OrderDto;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CamundaOrchestrationStrategy implements OrchestrationStrategy {

    private final ZeebeClient zeebeClient;

    @Override
    public void startOrderProcess(OrderDto orderRequest) {
        String orderId = UUID.randomUUID().toString();

        Map<String, Object> variables = Map.of(
                "orderId", orderId,
                "items", orderRequest.getRequestedItems(),
                "deliveryAddress", orderRequest.getDeliveryLocation(),
                "orderDto", orderRequest
        );

        zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("OrderSaga")
                .latestVersion()
                .variables(variables)
                .send()
                .join();
    }

    @Override
    public void cancelOrder(String orderId) {

        zeebeClient.newPublishMessageCommand()
                .messageName("OrderCancellation")
                .correlationKey(orderId)
                .variables(Map.of("reason", "Customer cancellation"))
                .send()
                .join();
    }

    @Override
    public OrchestrationMode getMode() {
        return OrchestrationMode.CAMUNDA;
    }
}
