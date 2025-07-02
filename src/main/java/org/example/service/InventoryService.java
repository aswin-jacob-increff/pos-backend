package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.exception.ApiException;

import java.util.List;
import org.example.dao.InventoryDao;
import org.example.pojo.InventoryPojo;

@Service
public class InventoryService {

    @Autowired
    private InventoryDao inventoryDao;

    public void add(InventoryPojo inventoryPojo) {
        inventoryDao.insert(inventoryPojo);
    }

    public InventoryPojo get(Integer id) {
        InventoryPojo inventory = inventoryDao.select(id);
        if (inventory == null) {
            throw new ApiException("Inventory with ID " + id + " not found");
        }
        return inventory;
    }

    public List<InventoryPojo> getAll() {
        return inventoryDao.selectAll();
    }

    public InventoryPojo getByProductId(Integer productId) {
        return inventoryDao.getByProductId(productId);
    }

    public InventoryPojo getByProductName(String productName) {
        return inventoryDao.getByProductName(productName);
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        return inventoryDao.getByProductBarcode(barcode);
    }

    public void update(Integer id, InventoryPojo updatedInventory) {
        InventoryPojo existingInventory = inventoryDao.select(id);
        if (existingInventory == null) {
            throw new ApiException("Inventory with ID " + id + " not found");
        }
        
        // Only update quantity, don't change product reference
        existingInventory.setQuantity(updatedInventory.getQuantity());
        inventoryDao.update(id, existingInventory);
    }
    
    /**
     * Add stock to existing inventory
     */
    public void addStock(Integer productId, Integer quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new ApiException("Quantity to add must be positive");
        }
        
        InventoryPojo inventory = getByProductId(productId);
        if (inventory == null) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        inventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        inventoryDao.update(inventory.getId(), inventory);
    }
    
    /**
     * Remove stock from existing inventory
     */
    public void removeStock(Integer productId, Integer quantityToRemove) {
        if (quantityToRemove <= 0) {
            throw new ApiException("Quantity to remove must be positive");
        }
        
        InventoryPojo inventory = getByProductId(productId);
        if (inventory == null) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        if (inventory.getQuantity() < quantityToRemove) {
            throw new ApiException("Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: " + quantityToRemove);
        }
        
        inventory.setQuantity(inventory.getQuantity() - quantityToRemove);
        inventoryDao.update(inventory.getId(), inventory);
    }
    
    /**
     * Set stock to a specific quantity
     */
    public void setStock(Integer productId, Integer newQuantity) {
        if (newQuantity < 0) {
            throw new ApiException("Stock quantity cannot be negative");
        }
        
        InventoryPojo inventory = getByProductId(productId);
        if (inventory == null) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        inventory.setQuantity(newQuantity);
        inventoryDao.update(inventory.getId(), inventory);
    }

    public void delete(Integer id) {
        InventoryPojo inventory = inventoryDao.select(id);
        if (inventory == null) {
            throw new ApiException("Inventory with ID " + id + " not found");
        }
        inventoryDao.delete(id);
    }
}
