package com.example.factoryservice.config;

import com.example.factoryservice.dto.AbstractDto;
import com.example.factoryservice.dto.ItemDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProps;

    @Bean
    public ConsumerFactory<String, AbstractDto> objectsConsumerFactory() {

        Map<String, Object> props = kafkaProps.buildConsumerProperties();
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        /* ── 1. Полиморфный Json-десериализатор ───────────────────── */
        JsonDeserializer<AbstractDto> delegate =
                new JsonDeserializer<>(AbstractDto.class);  // true по-умолчанию → читает __TypeId__

        // доверяем оба пакета (и склад, и фабрика)
        delegate.addTrustedPackages(
                "com.example.warehouseservice.dto",
                "com.example.factoryservice.dto",
                "com.example.dto"
        );

        /* ── 2. Маппинг чужой TypeId → локальный класс ────────────── */
        DefaultJackson2JavaTypeMapper mapper = new DefaultJackson2JavaTypeMapper();
        mapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        mapper.setIdClassMapping(Map.of(
                "com.example.warehouseservice.dto.ItemDto",  // FQCN, который пишет склад
                ItemDto.class                                 // ваш класс
                // при появлении нового события → просто добавьте ещё пару
        ));
        delegate.setTypeMapper(mapper);

        /* ── 3. Оборачиваем в ErrorHandlingDeserializer ───────────── */
        var valueDeserializer = new ErrorHandlingDeserializer<>(delegate);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean(name = "itemDtoListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, AbstractDto>
    listenerContainerFactory(ConsumerFactory<String, AbstractDto> cf) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, AbstractDto>();
        factory.setConsumerFactory(cf);

        // fail-fast: 0 ретраев, 0 задержки
        factory.setCommonErrorHandler(
                new DefaultErrorHandler(new FixedBackOff(0L, 0))
        );
        return factory;
    }
}
