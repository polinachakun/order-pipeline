package com.example.exception;

public class InvalidOrderSpecificationException extends RuntimeException {
    public InvalidOrderSpecificationException(String message) {
        super(message);
    }
}
