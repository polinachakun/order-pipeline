package com.example.warehouseservice.inventory;

import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.inventory.repository.InventoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    public List<ItemDto> pickItemsForOrder(List<ItemDto> requestedItems) {
        List<ItemDto> missingItems = new ArrayList<>();

        if (requestedItems == null) {
            log.warn("Received null requestedItems list");
            return missingItems;
        }

        for (ItemDto requestedItem : requestedItems) {
            if (requestedItem == null || requestedItem.getItemId() == null) {
                log.warn("Skipping null item or item with null ID");
                continue;
            }

            Optional<ItemDto> availableItemOpt = inventoryRepository.findByItemId(requestedItem.getItemId());

            if (availableItemOpt.isPresent()) {
                ItemDto availableItem = availableItemOpt.get();
                int availableQuantity = availableItem.getQuantity();
                int requestedQuantity = requestedItem.getQuantity();

                if (availableQuantity >= requestedQuantity) {
                    availableItem.setQuantity(availableQuantity - requestedQuantity);
                    log.info("Fully picked {} units of itemId {}. Remaining: {}", requestedQuantity, requestedItem.getItemId(), availableItem.getQuantity());
                } else if (availableQuantity > 0) {
                    availableItem.setQuantity(0);
                    log.info("Partially picked itemId {}. Picked: {}, Still missing: {}", requestedItem.getItemId(), availableQuantity, requestedQuantity - availableQuantity);
                    missingItems.add(new ItemDto(requestedItem.getItemId(), requestedQuantity - availableQuantity));
                } else {
                    log.warn("SKU {} is completely out of stock.", requestedItem.getItemId());
                    missingItems.add(new ItemDto(requestedItem.getItemId(), requestedQuantity));
                }

                inventoryRepository.save(availableItem);
            } else {
                log.warn("itemId {} not found.", requestedItem.getItemId());
                missingItems.add(new ItemDto(requestedItem.getItemId(), requestedItem.getQuantity()));
            }
        }

        return missingItems;
    }


    @Override
    public void addStock(ItemDto itemDto) {
        if (itemDto == null || itemDto.getItemId() == null) {
            log.warn("Cannot add stock for null item or item with null ID");
            return;
        }

        Optional<ItemDto> itemOptional = inventoryRepository.findByItemId(itemDto.getItemId());
        ItemDto item = itemExistsOrCreate(itemOptional, itemDto.getItemId());
        item.setQuantity(item.getQuantity() + itemDto.getQuantity());
        inventoryRepository.save(item);

        log.info("Item added to stock: item={}", itemDto);
    }

    @Override
    public void restoreStock(List<ItemDto> itemsToRestore) {
        if (itemsToRestore == null) {
            log.warn("Cannot restore stock for null items list");
            return;
        }

        for (ItemDto itemDto : itemsToRestore) {
            if (itemDto == null || itemDto.getItemId() == null) {
                log.warn("Skipping null item or item with null ID during stock restoration");
                continue;
            }

            Optional<ItemDto> existingItemOpt = inventoryRepository.findByItemId(itemDto.getItemId());
            ItemDto item = itemExistsOrCreate(existingItemOpt, itemDto.getItemId());

            int restoredQuantity = itemDto.getQuantity();
            item.setQuantity(item.getQuantity() + restoredQuantity);

            inventoryRepository.save(item);

            log.info("Restored {} units of itemId {} to stock. New total: {}",
                    restoredQuantity, item.getItemId(), item.getQuantity());
        }
    }

    @Override
    public List<ItemDto> findAll() {
        return inventoryRepository.findAll();
    }

    private ItemDto itemExistsOrCreate(Optional<ItemDto> existingItem, String sku) {
        if (sku == null) {
            log.warn("Cannot create item with null SKU");
            return new ItemDto("unknown", 0);
        }
        return existingItem.orElseGet(() -> new ItemDto(sku, 0));
    }
}