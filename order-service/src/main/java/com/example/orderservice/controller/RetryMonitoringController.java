package com.example.orderservice.controller;

import com.example.orderservice.retry.CommandRetryEntity;
import com.example.orderservice.retry.InMemoryCommandRetryRepository;
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