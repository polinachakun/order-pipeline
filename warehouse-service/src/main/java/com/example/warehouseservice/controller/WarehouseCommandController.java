package com.example.warehouseservice.controller;

import com.example.warehouseservice.dto.StartOrderCommand;
import com.example.warehouseservice.service.WarehouseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.example.warehouseservice.dto.OrderStatus.READY_FOR_PICKING;

@RestController
//@RequestMapping(value = "/orders")
@AllArgsConstructor
@Slf4j
public class WarehouseCommandController {

    private final WarehouseService warehouseService;

//    @PostMapping("/process")
//    public ResponseEntity<String> processOrder(@RequestBody OrderDto orderDto) {
//
//        log.info("Received process order command for order: {}", orderDto.getOrderId());
//        warehouseService.processNewOrder(orderDto);
//
//        return ResponseEntity.ok(orderDto.getOrderId());
//    }
//
//    @PostMapping("/process")
//    public ResponseEntity<String> processOrderCommand(@RequestBody Map<String, Object> command) {
//        String orderId = (String) command.get("orderId");
//        String deliveryLocation = (String) command.get("deliveryLocation");
//        String statusStr = (String) command.get("status");
//
//        log.info("Received process order command for order ID: {}", orderId);
//
//        OrderDto orderDto = new OrderDto();
//        orderDto.setOrderId(orderId);
//        orderDto.setDeliveryLocation(deliveryLocation);
//
//        if (statusStr != null) {
//            try {
//                OrderStatus status = OrderStatus.valueOf(statusStr);
//                orderDto.setStatus(status);
//            } catch (IllegalArgumentException e) {
//                log.warn("Invalid order status: {}. Setting to CREATED.", statusStr);
//                orderDto.setStatus(OrderStatus.CREATED);
//            }
//        } else {
//            orderDto.setStatus(OrderStatus.CREATED);
//        }
//
//        if (command.containsKey("requestedItems")) {
//            List<Map<String, Object>> items = (List<Map<String, Object>>) command.get("requestedItems");
//            List<ItemDto> requestedItems = items.stream()
//                    .map(item -> {
//                        String itemId = (String) item.get("itemId");
//                        Integer quantity = (Integer) item.get("quantity");
//                        return new ItemDto(itemId, quantity);
//                    })
//                    .toList();
//            orderDto.setRequestedItems(requestedItems);
//        }
//
//        warehouseService.processNewOrder(orderDto);
//        return ResponseEntity.ok(orderId);
//    }

    @PostMapping()
    public ResponseEntity<String> StartOrderCommand(@RequestBody StartOrderCommand command) {
        if (command == null) {
            log.error("Received null order command");
            return ResponseEntity.badRequest().body("Command cannot be null");
        }
        if (command.getStatus() == null) {
            command.setStatus(String.valueOf(READY_FOR_PICKING));
        }

        log.info("Received start order command: {}", command);

        String deliveryId = warehouseService.handleStartOrderCommand(command);

        if (deliveryId == null) {
            return ResponseEntity.badRequest().body("Failed to start order");
        }

        return ResponseEntity.ok(deliveryId);
    }
}