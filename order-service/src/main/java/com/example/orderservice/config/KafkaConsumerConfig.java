package com.example.orderservice.config;

import com.example.orderservice.dto.AbstractDto;
import com.example.orderservice.dto.OrderStatusUpdateEventDto;
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

    @Value("${kafka.ordersStatusUpdate.group-id}")
    private String ordersGroupId;

    @Value("${kafka.ordersStatusUpdate.camunda-group-id}")
    private String camundaGroupId;

    @Bean
    public ConsumerFactory<String, AbstractDto> objectsConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, ordersGroupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        JsonDeserializer<AbstractDto> jsonDeserializer = new JsonDeserializer<>(AbstractDto.class);
        jsonDeserializer.addTrustedPackages("com.example.orderservice.dto",
                "com.example.factoryservice.dto",
                "com.example.warehouseservice.dto",
                "com.example.deliveryservice.dto",
                "com.example.orderservice.producer.events");


        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("com.example.orderservice.dto.OrderStatusUpdateEventDto",
                OrderStatusUpdateEventDto.class);
        mappings.put("com.example.warehouseservice.dto.OrderStatusUpdateEventDto",
                com.example.orderservice.dto.OrderStatusUpdateEventDto.class);
        mappings.put("com.example.deliveryservice.dto.OrderStatusUpdateEventDto",
                com.example.orderservice.dto.OrderStatusUpdateEventDto.class);
        mappings.put("com.example.factoryservice.dto.OrderStatusUpdateEventDto",
                com.example.orderservice.dto.OrderStatusUpdateEventDto.class);

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
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(0L, 0L)));
        return factory;
    }


    @Bean
    public ConsumerFactory<String, String> camundaConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, camundaGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    camundaKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(camundaConsumerFactory());
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 2)));
        return factory;
    }
}