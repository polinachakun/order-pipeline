package com.example.warehouseservice.inventory.repository;

import com.example.warehouseservice.dto.ItemDto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryInventoryRepository implements InventoryRepository {

    private final Map<String, ItemDto> inventory = new ConcurrentHashMap<>();

    @Override
    public Optional<ItemDto> findByItemId(String itemId) {
        if (itemId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(inventory.get(itemId));
    }

    @Override
    public List<ItemDto> findAll() {
        return new ArrayList<>(inventory.values());
    }

    @Override
    public void save(ItemDto item) {
        if (item != null && item.getItemId() != null) {
            inventory.put(item.getItemId(), item);
        }
    }
}
