package org.example.flow;

import org.example.pojo.InventoryPojo;
import org.example.api.InventoryApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventoryFlow extends AbstractFlow<InventoryPojo> {

    @Autowired
    private InventoryApi api;

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
}
