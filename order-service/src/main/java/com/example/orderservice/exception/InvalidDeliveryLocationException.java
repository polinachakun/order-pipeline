package com.example.orderservice.exception;

public class InvalidDeliveryLocationException extends RuntimeException {
    public InvalidDeliveryLocationException(String message) {
        super(message);
    }
}
