package com.example.warehouseservice.inventory.repository;


import com.example.warehouseservice.dto.ItemDto;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    Optional<ItemDto> findByItemId(String itemId);

    List<ItemDto> findAll();

    void save(ItemDto item);
}
