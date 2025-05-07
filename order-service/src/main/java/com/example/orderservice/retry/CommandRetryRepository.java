package com.example.orderservice.retry;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CommandRetryRepository  {

    void save(CommandRetryEntity entity);

    Collection<CommandRetryEntity> getAllCommands();

    List<CommandRetryEntity> findByStatusAndNextRetryAtBefore(CommandRetryEntity.RetryStatus status, LocalDateTime time);
}
