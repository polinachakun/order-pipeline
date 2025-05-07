package com.example.orderservice.exception;

public class InvalidOrderSpecificationException extends RuntimeException {
    public InvalidOrderSpecificationException(String message) {
        super(message);
    }
}
