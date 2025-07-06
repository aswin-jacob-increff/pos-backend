package org.example.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.exception.ApiException;
import java.util.List;
import java.util.Objects;
import org.example.dao.InventoryDao;
import org.example.pojo.InventoryPojo;

@Service
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public void add(InventoryPojo inventoryPojo) {
        inventoryDao.insert(inventoryPojo);
    }

    public InventoryPojo get(Integer id) {
        InventoryPojo inventory = inventoryDao.select(id);
        if (Objects.isNull(inventory)) {
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
        if (Objects.isNull(existingInventory)) {
            throw new ApiException("Inventory with ID " + id + " not found");
        }
        
        inventoryDao.update(id, updatedInventory);
    }
    
    /**
     * Add stock to existing inventory
     */
    public void addStock(Integer productId, Integer quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new ApiException("Quantity to add must be positive");
        }
        
        InventoryPojo inventory = getByProductId(productId);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProduct(inventory.getProduct());
        updatedInventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }
    
    /**
     * Remove stock from existing inventory
     */
    public void removeStock(Integer productId, Integer quantityToRemove) {
        if (quantityToRemove <= 0) {
            throw new ApiException("Quantity to remove must be positive");
        }
        
        InventoryPojo inventory = getByProductId(productId);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        if (inventory.getQuantity() < quantityToRemove) {
            throw new ApiException("Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: " + quantityToRemove);
        }
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProduct(inventory.getProduct());
        updatedInventory.setQuantity(inventory.getQuantity() - quantityToRemove);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }
    
    /**
     * Set stock to a specific quantity
     */
    public void setStock(Integer productId, Integer newQuantity) {
        if (newQuantity < 0) {
            throw new ApiException("Stock quantity cannot be negative");
        }
        
        InventoryPojo inventory = getByProductId(productId);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProduct(inventory.getProduct());
        updatedInventory.setQuantity(newQuantity);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }

    public void delete(Integer id) {
        InventoryPojo inventory = inventoryDao.select(id);
        if (Objects.isNull(inventory)) {
            throw new ApiException("Inventory with ID " + id + " not found");
        }
        inventoryDao.delete(id);
    }
} 