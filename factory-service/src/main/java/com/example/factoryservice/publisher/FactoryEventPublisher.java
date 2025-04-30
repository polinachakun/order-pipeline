package com.example.factoryservice.publisher;

import com.example.factoryservice.config.KafkaTopicConfig;
import com.example.factoryservice.dto.AbstractDto;
import com.example.factoryservice.dto.ItemDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class FactoryEventPublisher {

    private final KafkaTemplate<String, AbstractDto> objectKafkaTemplate;
    private final KafkaTopicConfig kafkaTopicConfig;

    public void publishStockAddedTopicName(ItemDto event) {
        publishEvent(kafkaTopicConfig.getStockAddedTopicName(), event);
    }

    public void publishItemRequestTopic(ItemDto event) {
        publishEvent(kafkaTopicConfig.getItemRequestTopicName(), event);
    }

    private void publishEvent(String topicName, ItemDto event) {
        log.info("Sending items to {}: item {}; quantity {}",
                topicName, event.getItemId(), event.getQuantity());

        objectKafkaTemplate.send(topicName, event);
    }

}