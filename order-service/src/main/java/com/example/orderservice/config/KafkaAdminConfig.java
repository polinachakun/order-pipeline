//package com.example.config;
//
//import org.apache.kafka.clients.admin.AdminClientConfig;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.core.KafkaAdmin;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class KafkaAdminConfig {
//
//    @Value("${kafka.general.bootstrap-servers}")
//    private String bootstrapServers;
//
//    @Bean
//    public KafkaAdmin kafkaAdmin() {
//        Map<String, Object> configs = new HashMap<>();
//        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        // Set a longer timeout for topic creation
//        configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "300000"); // 5 minutes
//        configs.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "300000"); // 5 minutes
//        configs.put(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, "1000"); // 1 second
//        return new KafkaAdmin(configs);
//    }
//}