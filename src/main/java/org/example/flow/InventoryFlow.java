package org.example.flow;

import org.example.pojo.InventoryPojo;
import org.example.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryFlow {

    @Autowired
    private InventoryService inventoryService;

    public InventoryPojo add(InventoryPojo inventoryPojo) {
        inventoryService.add(inventoryPojo);
        return inventoryPojo;
    }

    public InventoryPojo get(Integer id) {
        return inventoryService.get(id);
    }

    public List<InventoryPojo> getAll() {
        return inventoryService.getAll();
    }

    public InventoryPojo getByProductId(Integer productId) {
        return inventoryService.getByProductId(productId);
    }

    public InventoryPojo getByProductName(String productName) {
        return inventoryService.getByProductName(productName);
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        return inventoryService.getByProductBarcode(barcode);
    }

    public InventoryPojo update(Integer id, InventoryPojo inventoryPojo) {
        inventoryService.update(id, inventoryPojo);
        return inventoryService.get(id);
    }

    public void delete(Integer id) {
        inventoryService.delete(id);
    }
}
