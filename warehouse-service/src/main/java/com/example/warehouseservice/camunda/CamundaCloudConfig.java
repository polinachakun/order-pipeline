package com.example.warehouseservice.camunda;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
@EnableZeebeClient
public class CamundaCloudConfig {

    @Autowired
    private ZeebeClient zeebeClient;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {

        log.info("Camunda Cloud configuration initialized");

        try {
            var topology = zeebeClient.newTopologyRequest().send().join();
            log.info("Successfully connected to Zeebe cluster");
            log.info("Cluster size: {}", topology.getClusterSize());
        } catch (Exception e) {
            log.error("Failed to connect to Zeebe cluster", e);
        }
    }
}