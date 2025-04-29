package com.example.service;

import com.example.dto.ItemDto;
import com.example.dto.OrderDto;
import com.example.model.Order;
import com.example.model.OrderItem;
import com.example.producer.OrderProducer;
import com.example.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;
    private final RestTemplate restTemplate;

    @Value("${warehouse.service.url:http://localhost:8082}")
    private String warehouseServiceUrl;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, OrderProducer orderProducer, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.orderProducer = orderProducer;
        this.restTemplate = restTemplate;
    }

    @Override
    public Order saveOrder(Order order) {
        System.out.println("Saving order: " + order.getOrderId());
        return orderRepository.save(order);
    }

    @Override
    public Order findById(String orderId) {
        System.out.println("Finding order by ID: " + orderId);
        return orderRepository.findById(orderId);
    }

    @Override
    public Order updateStatus(String orderId, String status) {
        System.out.println("Updating order status: " + orderId + " to " + status);
        return orderRepository.updateStatus(orderId, status);
    }

    @Override
    public void sendOrderToWarehouse(Order order) {
        System.out.println("Sending order to warehouse: " + order.getOrderId());

        // First, publish the order to Kafka
        orderProducer.sendOrder(order);

        // Then, send a direct HTTP request to the warehouse service
        OrderDto orderDto = convertToOrderDto(order);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderDto> request = new HttpEntity<>(orderDto, headers);

        try {
            String url = warehouseServiceUrl + "/orders/process";
            System.out.println("Sending HTTP request to: " + url);
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("Order successfully sent to warehouse service");
        } catch (Exception e) {
            System.out.println("Error sending order to warehouse service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public OrderDto processOrder(OrderDto orderDto) {
        System.out.println("Processing order: " + orderDto.getOrderId());

        // Save the order in our local repository
        Order order = convertToOrder(orderDto);
        orderRepository.save(order);

        // Send the order to the warehouse
        sendOrderToWarehouse(order);

        return orderDto;
    }

    private OrderDto convertToOrderDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .deliveryLocation(order.getDeliveryLocation())
                .status(order.getStatus())
                .requestedItems(order.getItems().stream()
                        .map(item -> new ItemDto(item.getItemId(), item.getQuantity()))
                        .collect(Collectors.toList()))
                .build();
    }

    private Order convertToOrder(OrderDto orderDto) {
        return new Order(
                orderDto.getOrderId(),
                null, // orderDate will be set to current date
                orderDto.getDeliveryLocation(),
                orderDto.getRequestedItems().stream()
                        .map(item -> new OrderItem(item.getItemId(), item.getQuantity()))
                        .collect(Collectors.toList())
        );
    }
}