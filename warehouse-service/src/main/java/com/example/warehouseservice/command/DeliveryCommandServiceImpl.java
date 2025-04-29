package com.example.warehouseservice.command;

import com.example.warehouseservice.command.dto.StartDeliveryCommand;
import com.example.warehouseservice.dto.AbstractDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
@Qualifier("deliveryCommand")
public class DeliveryCommandServiceImpl implements Command {

    private final RestTemplate restTemplate;

    @Value("${delivery.service.url}")
    private String deliveryServiceUrl;

    @Override
    public void execute(AbstractDto payload) {
        if (payload == null) {
            log.error("Cannot execute command with null payload");
            return;
        }

        StartDeliveryCommand commandPayload = (StartDeliveryCommand) payload;
        sendDeliveryCommand(commandPayload);
    }

    private void sendDeliveryCommand(StartDeliveryCommand commandPayload) {
        log.info("Sending start delivery command to {}: {}", deliveryServiceUrl, commandPayload);
        restTemplate.postForLocation(deliveryServiceUrl, commandPayload);
        log.info("Successfully sent delivery command");
    }
}