package com.example.warehouseservice.inventory;

import com.example.warehouseservice.dto.ItemDto;

import java.util.List;

public interface InventoryService {
    List<ItemDto> pickItemsForOrder(List<ItemDto> requestedItems);

    void addStock(ItemDto itemDto);

    List<ItemDto> findAll();

    void restoreStock(List<ItemDto> itemsToRestore);

}
