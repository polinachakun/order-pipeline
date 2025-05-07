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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class WarehouseCamundaWorkers {

    private final WarehouseService warehouseService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final WarehouseEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final ZeebeClient zeebeClient;

    @JobWorker(type = "processNewOrder", autoComplete = true)
    public void processNewOrder(
            @Variable String orderId,
            @Variable(name = "items") List<Map<String, Object>> rawItems,
            @Variable String deliveryLocation
    ) {
        log.info("[Camunda] processNewOrder {}", orderId);
        List<ItemDto> items = rawItems.stream()
                .map(m -> new ItemDto((String) m.get("itemId"), ((Number) m.get("quantity")).intValue()))
                .collect(Collectors.toList());
        OrderDto dto = OrderDto.builder()
                .orderId(orderId)
                .requestedItems(items)
                .deliveryLocation(deliveryLocation)
                .build();
        warehouseService.processNewOrder(dto);
    }

    @JobWorker(type = "checkStock", autoComplete = true)
    public Map<String, Object> checkStock(
            @Variable String orderId,
            @Variable(name = "items") List<Map<String, Object>> rawItems
    ) throws Exception {
        log.info("[Camunda] checkStock {}", orderId);
        List<ItemDto> items = rawItems.stream()
                .map(m -> new ItemDto((String) m.get("itemId"), ((Number) m.get("quantity")).intValue()))
                .collect(Collectors.toList());

        List<ItemDto> missing = inventoryService.pickItemsForOrder(items);
        boolean available = missing == null || missing.isEmpty();

        Map<String, Object> vars = new HashMap<>();
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
    public Map<String, Object> requestFromFactory(
            @Variable String orderId,
            @Variable(name = "items") List<Map<String, Object>> rawMissing
    ) {
        log.info("[Camunda] requestToFactory {}", orderId);

        Map<String, Object> variables = new HashMap<>();

        List<ItemDto> missing = rawMissing.stream()
                .map(m -> new ItemDto((String) m.get("itemId"), ((Number) m.get("quantity")).intValue()))
                .toList();
        if (!missing.isEmpty()) {
            ItemDto firstItem = missing.get(0);
            String itemId = firstItem.getItemId();
            variables.put("itemId", itemId);
            log.info("[Camunda] Setting itemId={} for message correlation", itemId);
        }

        missing.forEach(item -> {
            eventPublisher.publishItemRequest(item);
            log.info(" -> Requested factory: {}", item);
        });

        return variables;
    }

    @JobWorker(type = "updateInventory", autoComplete = true)
    public void updateInventory(
            @Variable String itemId,
            @Variable Integer quantity
    ) {
        log.info("[Camunda] updateInventory {} x{}", itemId, quantity);
        try {
            warehouseService.addStockAndRecheckPendingOrders(new ItemDto(itemId, quantity));
            log.info("[Camunda] Successfully updated inventory for item: {}", itemId);
        } catch (Exception e) {
            log.error("[Camunda] Failed to update inventory: {}", e.getMessage(), e);
            throw e;  // Rethrow to let Camunda handle the failure
        }
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
}

