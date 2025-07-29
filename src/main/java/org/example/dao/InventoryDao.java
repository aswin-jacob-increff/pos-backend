package org.example.dao;

import org.springframework.stereotype.Repository;
import org.example.pojo.InventoryPojo;
import java.util.List;

@Repository
public class InventoryDao extends AbstractDao<InventoryPojo> {
    
    public InventoryDao() {
        super(InventoryPojo.class);
    }

    public InventoryPojo getByProductId(Integer productId) {
        List<InventoryPojo> results = getByParams("productId", productId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    protected void updateEntity(InventoryPojo existing, InventoryPojo updated) {
        existing.setQuantity(updated.getQuantity());
        existing.setProductId(updated.getProductId());
    }
}
