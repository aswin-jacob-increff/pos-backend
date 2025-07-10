package org.example.dao;

import org.springframework.stereotype.Repository;
import org.example.pojo.InventoryPojo;

@Repository
public class InventoryDao extends AbstractDao<InventoryPojo> {
    public InventoryDao() {
        super(InventoryPojo.class);
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        return selectByField("productBarcode", barcode);
    }

    public InventoryPojo getByProductName(String name) {
        return selectByField("productName", name.trim().toLowerCase());
    }

    @Override
    protected void updateEntity(InventoryPojo existing, InventoryPojo updated) {
        existing.setQuantity(updated.getQuantity());
        existing.setProductBarcode(updated.getProductBarcode());
        existing.setProductName(updated.getProductName());
        existing.setClientName(updated.getClientName());
        existing.setProductMrp(updated.getProductMrp());
        existing.setProductImageUrl(updated.getProductImageUrl());
    }
}
