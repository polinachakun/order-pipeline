package com.example.warehouseservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/warehouse")
@RequiredArgsConstructor
public class HealthController {

    @GetMapping("/health")
    public String healthCheck() {
        return "Warehouse Service is up and running!";
    }
}
