package com.example.orderservice.command;

import com.example.orderservice.command.dto.StartOrderCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.orderservice.dto.AbstractDto;

@Service
@RequiredArgsConstructor
@Slf4j
@Qualifier("orderCommand")
public class OrderCommandServiceImpl implements Command {

    private final RestTemplate restTemplate;

    @Value("${warehouse.service.url}")
    private String warehouseServiceUrl;

    @Override
    public void execute(AbstractDto payload) {
        if (payload == null) {
            log.error("Cannot execute command with null payload");
            return;
        }

        StartOrderCommand commandPayload = (StartOrderCommand) payload;
        sendOrderCommand(commandPayload);
    }

    private void sendOrderCommand(StartOrderCommand commandPayload) {
        log.info("Sending start order command to {}: {}", warehouseServiceUrl, commandPayload);
        restTemplate.postForLocation(warehouseServiceUrl, commandPayload);
        log.info("Successfully sent delivery command");
    }
}