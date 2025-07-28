package org.example.dao;

import org.springframework.stereotype.Repository;
import org.example.pojo.InventoryPojo;

@Repository
public class InventoryDao extends AbstractDao<InventoryPojo> {
    public InventoryDao() {
        super(InventoryPojo.class);
    }

    public InventoryPojo getByProductId(Integer productId) {
        return selectByField("productId", productId);
    }

    @Override
    protected void updateEntity(InventoryPojo existing, InventoryPojo updated) {
        existing.setQuantity(updated.getQuantity());
        existing.setProductId(updated.getProductId());
    }
}
