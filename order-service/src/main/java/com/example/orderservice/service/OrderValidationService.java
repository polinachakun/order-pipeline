package com.example.orderservice.service;

import com.example.orderservice.exception.InvalidDeliveryLocationException;
import com.example.orderservice.exception.InvalidOrderSpecificationException;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class OrderValidationService {

    private static final Set<String> VALID_SWISS_LOCATIONS = new HashSet<>(Arrays.asList(
            "zurich", "geneva", "basel", "bern", "lausanne", "winterthur", "lucerne", "st. gallen",
            "lugano", "biel", "thun", "köniz", "la chaux-de-fonds", "fribourg", "schaffhausen",
            "chur", "vernier", "neuchâtel", "uster", "sion"
    ));

    public void validateOrder(Order order) {
        log.info("Validating order: {}", order.getOrderId());
        log.debug("Delivery location: {}", order.getDeliveryLocation());
        log.debug("Items count: {}", order.getRequestedItems() != null ? order.getRequestedItems().size() : "null");

        validateDeliveryLocation(order.getDeliveryLocation());
        validateOrderItems(order.getRequestedItems());

        log.info("Order validation successful for: {}", order.getOrderId());
    }

    private void validateDeliveryLocation(String location) {
        log.debug("Validating delivery location: {}", location);

        if (location == null || location.trim().isEmpty()) {
            log.warn("Location is empty or null");
            throw new InvalidDeliveryLocationException("Delivery location cannot be empty");
        }

        String normalizedLocation = location.toLowerCase().trim();
        log.debug("Normalized location: {}", normalizedLocation);
        boolean valid = VALID_SWISS_LOCATIONS.contains(normalizedLocation);
        log.debug("Is valid Swiss location: {}", valid);

        if (!valid) {
            log.warn("Unsupported delivery location: {}", location);
            throw new InvalidDeliveryLocationException(
                    "Delivery location '" + location + "' is not supported. " +
                            "We currently only deliver to major Swiss cities."
            );
        }

        log.info("Delivery location validation successful: {}", location);
    }

    private void validateOrderItems(List<OrderItem> items) {
        log.debug("Validating order items");

        if (items == null || items.isEmpty()) {
            log.warn("Items list is null or empty");
            throw new InvalidOrderSpecificationException("Order must contain at least one item");
        }

        log.debug("Number of items: {}", items.size());

        for (OrderItem item : items) {
            log.debug("Validating item: {} with quantity: {}", item.getItemId(), item.getQuantity());

            if (item.getQuantity() <= 0) {
                log.warn("Invalid quantity ({}) for item {}", item.getQuantity(), item.getItemId());
                throw new InvalidOrderSpecificationException(
                        "Invalid quantity for item " + item.getItemId() + ": " + item.getQuantity()
                );
            }
        }

        log.info("Order items validation successful");
    }
}