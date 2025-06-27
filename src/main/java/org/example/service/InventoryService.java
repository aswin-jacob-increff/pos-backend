package org.example.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.example.dao.InventoryDao;
import org.example.pojo.InventoryPojo;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryDao inventoryDao;

    public void add(InventoryPojo inventoryPojo) {
        inventoryDao.insert(inventoryPojo);
    }

    public InventoryPojo get(Integer id) {
        return inventoryDao.select(id);
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
        existingInventory.setProduct(updatedInventory.getProduct());
        existingInventory.setQuantity(updatedInventory.getQuantity());
        inventoryDao.update(id, existingInventory);
    }

    public void delete(Integer id) {
        inventoryDao.delete(id);
    }
}
