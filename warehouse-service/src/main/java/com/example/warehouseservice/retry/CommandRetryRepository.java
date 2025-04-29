package com.example.warehouseservice.retry;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommandRetryRepository  {

    void save(CommandRetryEntity entity);

    Collection<CommandRetryEntity> getAllCommands();

    List<CommandRetryEntity> findByStatusAndNextRetryAtBefore(CommandRetryEntity.RetryStatus status, LocalDateTime time);
}
