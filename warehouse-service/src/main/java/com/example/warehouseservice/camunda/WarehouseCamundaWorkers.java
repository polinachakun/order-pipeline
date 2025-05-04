package com.example.warehouseservice.camunda;

import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.dto.OrderDto;
import com.example.warehouseservice.dto.OrderStatusUpdateEventDto;
import com.example.warehouseservice.inventory.InventoryService;
import com.example.warehouseservice.orders.OrderService;
import com.example.warehouseservice.publisher.WarehouseEventPublisher;
import com.example.warehouseservice.service.WarehouseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class WarehouseCamundaWorkers {

    private final WarehouseService    warehouseService;
    private final InventoryService    inventoryService;
    private final OrderService        orderService;
    private final WarehouseEventPublisher eventPublisher;
    private final ObjectMapper        objectMapper;
    private final ZeebeClient         zeebeClient;

    // ----------------------------------
    // Camunda External Task Workers
    // ----------------------------------

    @JobWorker(type = "processNewOrder", autoComplete = true)
    public void processNewOrder(
            @Variable String orderId,
            @Variable(name = "items") List<Map<String, Object>> rawItems,
            @Variable String deliveryLocation
    ) {
        log.info("[Camunda] processNewOrder {}", orderId);
        List<ItemDto> items = rawItems.stream()
                .map(m -> new ItemDto((String)m.get("itemId"), ((Number)m.get("quantity")).intValue()))
                .collect(Collectors.toList());
        OrderDto dto = OrderDto.builder()
                .orderId(orderId)
                .requestedItems(items)
                .deliveryLocation(deliveryLocation)
                .build();
        warehouseService.processNewOrder(dto);
    }

    @JobWorker(type = "checkStock", autoComplete = true)
    public Map<String,Object> checkStock(
            @Variable String orderId,
            @Variable(name = "items") List<Map<String,Object>> rawItems
    ) throws Exception {
        log.info("[Camunda] checkStock {}", orderId);
        List<ItemDto> items = rawItems.stream()
                .map(m -> new ItemDto((String)m.get("itemId"), ((Number)m.get("quantity")).intValue()))
                .collect(Collectors.toList());

        List<ItemDto> missing = inventoryService.pickItemsForOrder(items);
        boolean available = missing == null || missing.isEmpty();

        Map<String,Object> vars = new HashMap<>();
        vars.put("stockAvailable", available);
        if (!available) {
            vars.put("missingItems", objectMapper.writeValueAsString(missing));
        }
        return vars;
    }

    @JobWorker(type = "fulfillOrder", autoComplete = true)
    public void fulfillOrder(@Variable String orderId) {
        log.info("[Camunda] fulfillOrder {}", orderId);
        orderService.markOrderAsFullyAvailable(orderId);
    }

    @JobWorker(type = "requestToFactory", autoComplete = true)
    public void requestFromFactory(
            @Variable String orderId,
            @Variable(name = "items") List<Map<String,Object>> rawMissing
    ) {
        log.info("[Camunda] requestToFactory {}", orderId);
        List<ItemDto> missing = rawMissing.stream()
                .map(m -> new ItemDto((String)m.get("itemId"), ((Number)m.get("quantity")).intValue()))
                .toList();

        missing.forEach(item -> {
            eventPublisher.publishItemRequest(item);
            log.info(" -> Requested factory: {}", item);
        });
    }

    @JobWorker(type = "updateInventory", autoComplete = true)
    public void updateInventory(
            @Variable String itemId,
            @Variable Integer quantity
    ) {
        log.info("[Camunda] updateInventory {} x{}", itemId, quantity);
        warehouseService.addStockAndRecheckPendingOrders(new ItemDto(itemId, quantity));
    }

    @JobWorker(type = "packageItems", autoComplete = true)
    public void packageItems(@Variable String orderId) {
        log.info("[Camunda] packageItems {}", orderId);
        // packaging already handled by warehouseService
    }

    @JobWorker(type = "releaseStock", autoComplete = true)
    public void releaseStock(@Variable String orderId) {
        log.info("[Camunda][comp] releaseStock {}", orderId);
        // compensation logic if needed
    }

    @JobWorker(type = "restoreStock", autoComplete = true)
    public void restoreStock(@Variable String orderId) {
        log.info("[Camunda][comp] restoreStock {}", orderId);
        warehouseService.compensateInventory(
                new OrderStatusUpdateEventDto(orderId, "DELIVERY_FAILED")
        );
    }

    @JobWorker(type = "unpackItems", autoComplete = true)
    public void unpackItems(@Variable String orderId) {
        log.info("[Camunda][comp] unpackItems {}", orderId);
        // no‐op if restoreStock already handles it
    }

    @JobWorker(type = "cancelDelivery", autoComplete = true)
    public void cancelDelivery(@Variable String orderId) {
        log.info("[Camunda][comp] cancelDelivery {}", orderId);
        // no‐op or notify warehouseService
    }


    /**
     * Listens on the same factory.topic in parallel to your existing consumer,
     * updates local inventory, and then correlates the StockAdded message
     * back into Zeebe so your BPMN message‐catch resumes.
     */
//    @KafkaListener(
//            topics           = "${kafka.factory.topic}",
//            groupId          = "${kafka.factory.group-id}",
//            containerFactory = "objectsKafkaListenerContainerFactory"
//    )
//    public void onFactoryStockAdded(ItemDto item) {
//        if (item == null || item.getItemId() == null) {
//            log.warn("[Camunda] skip null/bad ItemDto: {}", item);
//            return;
//        }
//
//        log.info("[Camunda] factory → stockAdded: {}", item);
//
//        // 1) update inventory & pending orders
//        warehouseService.addStockAndRecheckPendingOrders(item);
//
//        // 2) correlate into Zeebe
//        Map<String,Object> vars = new HashMap<>();
//        vars.put("itemId",   item.getItemId());
//        vars.put("quantity", item.getQuantity());
//
//        zeebeClient.newPublishMessageCommand()
//                .messageName("StockAdded")
//                .correlationKey(item.getItemId())
//                .variables(vars)
//                .send()
//                .join();
//
//        log.info("[Camunda] correlated StockAdded for itemId={}", item.getItemId());
//    }
}
