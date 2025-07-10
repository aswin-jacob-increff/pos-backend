package org.example.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.exception.ApiException;
import java.util.Objects;
import org.example.dao.InventoryDao;
import org.example.pojo.InventoryPojo;

@Service
public class InventoryApi extends AbstractApi<InventoryPojo> {

    @Autowired
    private InventoryDao inventoryDao;

    @Override
    protected String getEntityName() {
        return "Inventory";
    }

    public InventoryPojo getByProductName(String productName) {
        return inventoryDao.getByProductName(productName);
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        return inventoryDao.getByProductBarcode(barcode);
    }

    /**
     * Add stock to existing inventory
     */
    public void addStock(String barcode, Integer quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new ApiException("Quantity to add must be positive");
        }
        InventoryPojo inventory = getByProductBarcode(barcode);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product barcode: " + barcode);
        }
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductBarcode(inventory.getProductBarcode());
        updatedInventory.setProductName(inventory.getProductName());
        updatedInventory.setClientName(inventory.getClientName());
        updatedInventory.setProductMrp(inventory.getProductMrp());
        updatedInventory.setProductImageUrl(inventory.getProductImageUrl());
        updatedInventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }

    /**
     * Remove stock from existing inventory
     */
    public void removeStock(String barcode, Integer quantityToRemove) {
        if (quantityToRemove <= 0) {
            throw new ApiException("Quantity to remove must be positive");
        }
        InventoryPojo inventory = getByProductBarcode(barcode);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product barcode: " + barcode);
        }
        if (inventory.getQuantity() < quantityToRemove) {
            throw new ApiException("Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: " + quantityToRemove);
        }
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductBarcode(inventory.getProductBarcode());
        updatedInventory.setProductName(inventory.getProductName());
        updatedInventory.setClientName(inventory.getClientName());
        updatedInventory.setProductMrp(inventory.getProductMrp());
        updatedInventory.setProductImageUrl(inventory.getProductImageUrl());
        updatedInventory.setQuantity(inventory.getQuantity() - quantityToRemove);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }

    /**
     * Set stock to a specific quantity
     */
    public void setStock(String barcode, Integer newQuantity) {
        if (newQuantity < 0) {
            throw new ApiException("Stock quantity cannot be negative");
        }
        InventoryPojo inventory = getByProductBarcode(barcode);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product barcode: " + barcode);
        }
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductBarcode(inventory.getProductBarcode());
        updatedInventory.setProductName(inventory.getProductName());
        updatedInventory.setClientName(inventory.getClientName());
        updatedInventory.setProductMrp(inventory.getProductMrp());
        updatedInventory.setProductImageUrl(inventory.getProductImageUrl());
        updatedInventory.setQuantity(newQuantity);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }
} 