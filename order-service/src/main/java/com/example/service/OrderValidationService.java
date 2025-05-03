package com.example.service;

import com.example.exception.InvalidDeliveryLocationException;
import com.example.exception.InvalidOrderSpecificationException;
import com.example.model.Order;
import com.example.model.OrderItem;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderValidationService {

    private static final Set<String> VALID_SWISS_LOCATIONS = new HashSet<>(Arrays.asList(
            "zurich", "geneva", "basel", "bern", "lausanne", "winterthur", "lucerne", "st. gallen", 
            "lugano", "biel", "thun", "köniz", "la chaux-de-fonds", "fribourg", "schaffhausen", 
            "chur", "vernier", "neuchâtel", "uster", "sion"
    ));

    public void validateOrder(Order order) {
        System.out.println("Validating order: " + order.getOrderId());
        System.out.println("Delivery location: " + order.getDeliveryLocation());
        System.out.println("Items: " + (order.getRequestedItems() != null ? order.getRequestedItems().size() : "null"));

        validateDeliveryLocation(order.getDeliveryLocation());
        validateOrderItems(order.getRequestedItems());

        System.out.println("Order validation successful");
    }

    private void validateDeliveryLocation(String location) {
        System.out.println("Validating delivery location: " + location);

        if (location == null || location.trim().isEmpty()) {
            System.out.println("Location is empty or null");
            throw new InvalidDeliveryLocationException("Delivery location cannot be empty");
        }

        String normalizedLocation = location.toLowerCase().trim();
        System.out.println("Normalized location: " + normalizedLocation);
        System.out.println("Is valid Swiss location: " + VALID_SWISS_LOCATIONS.contains(normalizedLocation));

        if (!VALID_SWISS_LOCATIONS.contains(normalizedLocation)) {
            System.out.println("Throwing InvalidDeliveryLocationException");
            throw new InvalidDeliveryLocationException(
                "Delivery location '" + location + "' is not supported. " +
                "We currently only deliver to major Swiss cities."
            );
        }

        System.out.println("Delivery location validation successful");
    }

    private void validateOrderItems(List<OrderItem> items) {
        System.out.println("Validating order items");

        if (items == null || items.isEmpty()) {
            System.out.println("Items list is null or empty");
            throw new InvalidOrderSpecificationException("Order must contain at least one item");
        }

        System.out.println("Number of items: " + items.size());

        for (OrderItem item : items) {
            System.out.println("Validating item: " + item.getItemId() + ", quantity: " + item.getQuantity());

            if (item.getQuantity() <= 0) {
                System.out.println("Invalid quantity: " + item.getQuantity());
                throw new InvalidOrderSpecificationException(
                    "Invalid quantity for item " + item.getItemId() + ": " + item.getQuantity()
                );
            }
        }

        System.out.println("Order items validation successful");
    }
}