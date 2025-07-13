package org.example.flow;

import org.example.pojo.InventoryPojo;
import org.example.api.InventoryApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.example.exception.ApiException;

@Service
public class InventoryFlow extends AbstractFlow<InventoryPojo> {

    @Autowired
    private InventoryApi api;

    public InventoryFlow() {
        super(InventoryPojo.class);
    }

    public InventoryPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Inventory ID cannot be null");
        }
        return api.get(id);
    }

    @Override
    protected Integer getEntityId(InventoryPojo entity) {
        return entity.getId();
    }

    @Override
    protected String getEntityName() {
        return "Inventory";
    }

    public InventoryPojo getByProductName(String productName) {
        return api.getByProductName(productName);
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        return api.getByProductBarcode(barcode);
    }

    public void addStock(String barcode, Integer quantity) {
        api.addStock(barcode, quantity);
    }

    public void removeStock(String barcode, Integer quantity) {
        api.removeStock(barcode, quantity);
    }

    public void setStock(String barcode, Integer quantity) {
        api.setStock(barcode, quantity);
    }

    @Override
    public InventoryPojo add(InventoryPojo inventoryPojo) {
        if (inventoryPojo == null) {
            throw new org.example.exception.ApiException("Inventory cannot be null");
        }
        return super.add(inventoryPojo);
    }

    @Override
    public void update(Integer id, InventoryPojo entity) {
        if (id == null) {
            throw new ApiException("ID cannot be null");
        }
        if (entity == null) {
            throw new ApiException("Entity cannot be null");
        }
        api.update(id, entity);
    }
}
