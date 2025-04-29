package com.example.warehouseservice.service.impl;

import com.example.warehouseservice.command.Command;
import com.example.warehouseservice.command.dto.StartDeliveryCommand;
import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.dto.OrderDto;
import com.example.warehouseservice.dto.OrderStatus;
import com.example.warehouseservice.dto.OrderStatusUpdateEventDto;
import com.example.warehouseservice.inventory.InventoryService;
import com.example.warehouseservice.orders.OrderService;
import com.example.warehouseservice.orders.repository.OrderRepository;
import com.example.warehouseservice.publisher.WarehouseEventPublisher;
import com.example.warehouseservice.retry.StatefulRetryCommandHandler;
import com.example.warehouseservice.service.WarehouseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final WarehouseEventPublisher eventPublisher;
    private final StatefulRetryCommandHandler statefulRetryCommandHandler;

    @Override
    public void processNewOrder(OrderDto orderDto) {
        if (orderDto == null || orderDto.getOrderId() == null) {
            log.error("Cannot process null order or order with null ID");
            return;
        }

        log.info("Processing new order: {}", orderDto.getOrderId());

        orderService.initializeNewOrder(orderDto);
        attemptFulfillment(orderDto);
    }

    @Override
    public void addStockAndRecheckPendingOrders(ItemDto itemDto) {
        if (itemDto == null || itemDto.getItemId() == null) {
            log.error("Cannot add null stock or stock with null ID");
            return;
        }

        log.info("New stock added. item={}", itemDto);
        inventoryService.addStock(itemDto);

        List<OrderDto> pendingOrders = orderService.getOrdersByStatuses(
                OrderStatus.READY_FOR_PICKING,
                OrderStatus.PARTIALLY_FULFILLMENT
        );

        if (pendingOrders == null) {
            log.warn("No pending orders found to recheck");
            return;
        }

        for (OrderDto order : pendingOrders) {
            if (order == null || order.getOrderId() == null) {
                log.warn("Skipping null order or order with null ID during recheck");
                continue;
            }
            log.info("Rechecking fulfillment possibility for Order {}", order.getOrderId());
            attemptFulfillment(order);
        }
    }

    private void attemptFulfillment(OrderDto orderDto) {
        if (orderDto == null || orderDto.getOrderId() == null) {
            log.error("Cannot attempt fulfillment for null order or order with null ID");
            return;
        }

        List<ItemDto> itemsToCheck;
        if (orderDto.getMissingItems() == null) {
            itemsToCheck = orderDto.getRequestedItems();
        } else {
            itemsToCheck = orderDto.getMissingItems().isEmpty()
                    ? orderDto.getRequestedItems()
                    : orderDto.getMissingItems();
        }

        if (itemsToCheck == null) {
            log.error("No items to check for order {}", orderDto.getOrderId());
            return;
        }

        List<ItemDto> missingItemsAfterAttempt = inventoryService.pickItemsForOrder(itemsToCheck);

        if (missingItemsAfterAttempt == null || missingItemsAfterAttempt.isEmpty()) {
            orderDto.setMissingItems(Collections.emptyList());
            orderService.markOrderAsFullyAvailable(orderDto.getOrderId());

            StartDeliveryCommand payload = new StartDeliveryCommand(
                    orderDto.getOrderId(),
                    orderDto.getDeliveryLocation()
            );

            log.info("Sending delivery command for order {}", orderDto.getOrderId());
            statefulRetryCommandHandler.handle(payload);
        } else {
            orderDto.setMissingItems(missingItemsAfterAttempt);

            List<String> missingSkus = extractSkus(missingItemsAfterAttempt);
            orderService.markOrderAsPartiallyAvailable(orderDto.getOrderId(), missingSkus);

            for (ItemDto missingItem : missingItemsAfterAttempt) {
                if (missingItem != null && missingItem.getItemId() != null) {
                    publishItemRequestToFactory(missingItem);
                }
            }
        }

        orderRepository.save(orderDto);
    }

    @Override
    public void compensateInventory(OrderStatusUpdateEventDto event) {
        if (event == null || event.getOrderId() == null) {
            log.error("Cannot compensate inventory for null event or event with null orderId");
            return;
        }

        log.warn("Compensating stock for failed delivery, orderId={}", event.getOrderId());

        try {
            OrderDto order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

            if (order.getRequestedItems() == null) {
                log.warn("Order {} has no requested items to restore", event.getOrderId());
                return;
            }

            inventoryService.restoreStock(order.getRequestedItems());
            orderService.markOrderAsCancelled(order.getOrderId());
        } catch (Exception e) {
            log.error("Error compensating inventory: {}", e.getMessage(), e);
        }
    }

    private List<String> extractSkus(List<ItemDto> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .filter(item -> item != null && item.getItemId() != null)
                .map(ItemDto::getItemId)
                .toList();
    }

    private void publishItemRequestToFactory(ItemDto itemDto) {
        if (itemDto == null || itemDto.getItemId() == null) {
            log.warn("Cannot publish item request for null item or item with null ID");
            return;
        }
        log.info("Publishing item request to factory: itemId={}, Quantity={}", itemDto.getItemId(), itemDto.getQuantity());
        eventPublisher.publishItemRequest(itemDto);
    }

}