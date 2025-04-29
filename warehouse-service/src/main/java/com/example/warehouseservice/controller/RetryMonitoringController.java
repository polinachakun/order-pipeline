package com.example.warehouseservice.controller;

import com.example.warehouseservice.retry.CommandRetryEntity;
import com.example.warehouseservice.retry.InMemoryCommandRetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class RetryMonitoringController {

    private final InMemoryCommandRetryRepository retryRepository;

    @GetMapping("/commands")
    public Collection<CommandRetryEntity> getAllCommands() {
        return retryRepository.getAllCommands();
    }
}