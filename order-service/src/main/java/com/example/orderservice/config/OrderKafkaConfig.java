//package com.example.config;
//import com.example.model.Order;
//import com.example.orderservice.producer.events.OrderCreatedEvent;
//
//
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.support.PropertiesLoaderUtils;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//@Configuration
//public class OrderKafkaConfig {
//
//    private final Properties properties = new Properties();
//
//    public OrderKafkaConfig() {
//        try {
//            Resource resource = new ClassPathResource("orderproducer.properties");
//            properties.putAll(PropertiesLoaderUtils.loadProperties(resource));
//        } catch (IOException e) {
//            throw new RuntimeException("Could not load orderproducer.properties", e);
//        }
//    }
//
//    @Bean
//    public ProducerFactory<String, OrderCreatedEvent> orderProducerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap.servers"));
//        configProps.put(ProducerConfig.ACKS_CONFIG, properties.getProperty("acks"));
//        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.parseInt(properties.getProperty("retries")));
//        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.parseInt(properties.getProperty("batch.size")));
//        configProps.put(ProducerConfig.LINGER_MS_CONFIG, Integer.parseInt(properties.getProperty("linger.ms")));
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
//    @Bean
//    @org.springframework.context.annotation.Primary
//    public KafkaTemplate<String, OrderCreatedEvent> orderKafkaTemplate() {
//        return new KafkaTemplate<>(orderProducerFactory());
//    }
//
//    @Bean
//    public ProducerFactory<String, OrderCreatedEvent> orderCreatedEventProducerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap.servers"));
//        configProps.put(ProducerConfig.ACKS_CONFIG, properties.getProperty("acks"));
//        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.parseInt(properties.getProperty("retries")));
//        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.parseInt(properties.getProperty("batch.size")));
//        configProps.put(ProducerConfig.LINGER_MS_CONFIG, Integer.parseInt(properties.getProperty("linger.ms")));
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
//    @Bean
//    public KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventKafkaTemplate() {
//        return new KafkaTemplate<>(orderCreatedEventProducerFactory());
//    }
//}
