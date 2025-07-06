package org.example.flow;

import org.example.pojo.InventoryPojo;
import org.example.api.InventoryApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryFlow {

    @Autowired
    private InventoryApi api;

    public InventoryPojo add(InventoryPojo inventoryPojo) {
        api.add(inventoryPojo);
        return inventoryPojo;
    }

    public InventoryPojo get(Integer id) {
        return api.get(id);
    }

    public List<InventoryPojo> getAll() {
        return api.getAll();
    }

    public InventoryPojo getByProductId(Integer productId) {
        return api.getByProductId(productId);
    }

    public InventoryPojo getByProductName(String productName) {
        return api.getByProductName(productName);
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        return api.getByProductBarcode(barcode);
    }

    public InventoryPojo update(Integer id, InventoryPojo inventoryPojo) {
        api.update(id, inventoryPojo);
        return api.get(id);
    }

    public void delete(Integer id) {
        api.delete(id);
    }

    public void addStock(Integer productId, Integer quantity) {
        api.addStock(productId, quantity);
    }

    public void removeStock(Integer productId, Integer quantity) {
        api.removeStock(productId, quantity);
    }

    public void setStock(Integer productId, Integer quantity) {
        api.setStock(productId, quantity);
    }
}
