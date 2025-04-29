package com.example.warehouseservice.config;

import com.example.warehouseservice.dto.AbstractDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${kafka.general.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.orders.group-id}")
    private String ordersGroupId;

    public ConsumerFactory<String, AbstractDto> objectsConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, ordersGroupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        JsonDeserializer<AbstractDto> jsonDeserializer = new JsonDeserializer<>(AbstractDto.class);
        jsonDeserializer.addTrustedPackages("com.example.deliveryservice.dto");
        jsonDeserializer.addTrustedPackages("com.example.warehouseservice.dto");
        jsonDeserializer.addTrustedPackages("com.example.producer.events");
        jsonDeserializer.addTrustedPackages("com.example.dto");

        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("com.example.deliveryservice.dto.OrderStatusUpdateEventDto",
                com.example.warehouseservice.dto.OrderStatusUpdateEventDto.class);
        mappings.put("com.example.producer.events.OrderCreatedEvent",
                com.example.warehouseservice.dto.OrderDto.class);

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.setIdClassMapping(mappings);

        jsonDeserializer.setTypeMapper(typeMapper);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AbstractDto>
    objectsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AbstractDto> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(objectsConsumerFactory());

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(0L, 0));
        factory.setCommonErrorHandler(errorHandler);

//        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(0L, 0));
//
//        errorHandler.setLogLevel(org.springframework.kafka.listener.LoggingErrorHandler.Level.WARN);
//
//        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }


}
