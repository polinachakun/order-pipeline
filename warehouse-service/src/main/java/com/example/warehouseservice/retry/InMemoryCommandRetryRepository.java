package com.example.warehouseservice.retry;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryCommandRetryRepository implements CommandRetryRepository{

    private final Map<String, CommandRetryEntity> storage = new ConcurrentHashMap<>();

    public void save(CommandRetryEntity entity) {
        storage.put(entity.getId(), entity);
    }

    public List<CommandRetryEntity> findByStatusAndNextRetryAtBefore(CommandRetryEntity.RetryStatus status, LocalDateTime time) {
        return storage.values().stream()
                .filter(e -> e.getStatus() == status && e.getNextRetryAt().isBefore(time))
                .collect(Collectors.toList());
    }

    public Collection<CommandRetryEntity> getAllCommands() {
        return storage.values();
    }
}

