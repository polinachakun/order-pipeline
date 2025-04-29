package com.example.controller;

import com.example.model.Order;
import com.example.model.OrderItem;
import com.example.producer.OrderProducer;
import com.example.repository.OrderRepository;
import com.example.service.OrderValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.exception.InvalidDeliveryLocationException;
import com.example.exception.InvalidOrderSpecificationException;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderValidationService validationService;
    private final OrderProducer orderProducer;
    private final RestTemplate restTemplate;

    @Value("${warehouse.service.url}")
    private String warehouseServiceUrl;

    @Autowired
    public OrderController(
            OrderRepository orderRepository,
            OrderValidationService validationService,
            OrderProducer orderProducer,
            RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.validationService = validationService;
        this.orderProducer = orderProducer;
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<Object> createOrder(@RequestBody Order order) {
        try {
            System.out.println("Creating order with ID: " + order.getOrderId());
            System.out.println("Delivery location: " + order.getDeliveryLocation());
            System.out.println("Items: " + (order.getItems() != null ? order.getItems().size() : "null"));

            try {
                // Validate the order
                validationService.validateOrder(order);
            } catch (InvalidDeliveryLocationException e) {
                System.out.println("Invalid delivery location: " + e.getMessage());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("timestamp", Instant.now().toString());
                errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
                errorResponse.put("error", "Invalid Delivery Location");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } catch (InvalidOrderSpecificationException e) {
                System.out.println("Invalid order specification: " + e.getMessage());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("timestamp", Instant.now().toString());
                errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
                errorResponse.put("error", "Invalid Order Specification");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Save the order to the repository
            orderRepository.save(order);
            System.out.println("Order saved to repository with ID: " + order.getOrderId());

            // Send the order to Kafka
            orderProducer.sendOrder(order);
            System.out.println("Order sent to Kafka with ID: " + order.getOrderId());

            // Send the order to the warehouse service
            sendOrderToWarehouse(order);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Order created with orderId: " + order.getOrderId());
        } catch (Exception e) {
            System.out.println("Error creating order: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", Instant.now().toString());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<String> getOrderStatus(@PathVariable String orderId) {
        try {
            System.out.println("Looking for order with ID: " + orderId);
            Order order = orderRepository.findById(orderId);
            if (order != null) {
                System.out.println("Order found: " + order.getOrderId());
                return ResponseEntity.ok("Order found with ID: " + orderId +
                    ", Status: " + order.getStatus() +
                    ", Location: " + order.getDeliveryLocation());
            } else {
                System.out.println("Order not found with ID: " + orderId);
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Order not found with ID: " + orderId);
            }
        } catch (Exception e) {
            System.out.println("Error getting order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting order: " + e.getMessage());
        }
    }

    @GetMapping("/test/{orderId}")
    public ResponseEntity<String> testOrderStatus(@PathVariable String orderId) {
        return ResponseEntity.ok("Test endpoint working for order ID: " + orderId);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status) {
        Order order = orderRepository.updateStatus(orderId, status);
        if (order != null) {
            return ResponseEntity.ok("Order status updated to: " + status);
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Order not found with ID: " + orderId);
        }
    }

    /**
     * Sends an order to the warehouse service via HTTP
     */
    private void sendOrderToWarehouse(Order order) {
        try {
            // Prepare the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderId", order.getOrderId());
            requestBody.put("deliveryLocation", order.getDeliveryLocation());
            requestBody.put("requestedItems", order.getItems());

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Send the request to the warehouse service
            String url = warehouseServiceUrl + "/orders/process";
            System.out.println("Sending order to warehouse service: " + url);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            System.out.println("Warehouse service response: " + response.getStatusCode() + " " + response.getBody());
        } catch (Exception e) {
            System.out.println("Error sending order to warehouse service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PostMapping("/process")
    public ResponseEntity<Object> processOrder(@RequestBody Map<String, Object> orderRequest) {
        try {
            System.out.println("Processing order request: " + orderRequest);

            String orderId = orderRequest.containsKey("orderId") ?
                    (String) orderRequest.get("orderId") :
                    UUID.randomUUID().toString();

            String deliveryLocation = (String) orderRequest.get("deliveryLocation");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> requestedItemsMap = (List<Map<String, Object>>) orderRequest.get("requestedItems");

            // Convert the map to OrderItems
            List<OrderItem> requestedItems = requestedItemsMap.stream()
                    .map(item -> new OrderItem(
                            (String) item.get("itemId"),
                            Integer.parseInt(item.get("quantity").toString())
                    ))
                    .toList();

            // Create a new order
            Order order = new Order(orderId, null, deliveryLocation, requestedItems);

            // Validate the order
            validationService.validateOrder(order);

            // Save the order
            orderRepository.save(order);
            System.out.println("Order saved to repository with ID: " + order.getOrderId());

            // Send the order to Kafka
            orderProducer.sendOrder(order);
            System.out.println("Order sent to Kafka with ID: " + order.getOrderId());

            // Send the order to the warehouse service
            sendOrderToWarehouse(order);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getOrderId());
            response.put("status", order.getStatus());
            response.put("message", "Order processed successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InvalidDeliveryLocationException | InvalidOrderSpecificationException e) {
            System.out.println("Validation error: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", Instant.now().toString());
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", e instanceof InvalidDeliveryLocationException ?
                    "Invalid Delivery Location" : "Invalid Order Specification");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            System.out.println("Error processing order: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", Instant.now().toString());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
