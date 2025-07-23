package org.example.dao;

import org.springframework.stereotype.Repository;
import org.example.pojo.InventoryPojo;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import java.util.List;

@Repository
public class InventoryDao extends AbstractDao<InventoryPojo> {
    public InventoryDao() {
        super(InventoryPojo.class);
    }

    public InventoryPojo getByProductId(Integer productId) {
        return selectByField("productId", productId);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all inventory items with pagination support.
     */
    public PaginationResponse<InventoryPojo> getAllPaginated(PaginationRequest request) {
        return selectAllPaginated(request);
    }

    /**
     * Get inventory by product ID with pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductIdPaginated(Integer productId, PaginationRequest request) {
        return selectByFieldPaginated("productId", productId, request);
    }

    // ========== COUNT METHODS ==========

    /**
     * Count inventory items by product ID.
     */
    public long countByProductId(Integer productId) {
        return countByField("productId", productId);
    }

    @Override
    protected void updateEntity(InventoryPojo existing, InventoryPojo updated) {
        existing.setQuantity(updated.getQuantity());
        existing.setProductId(updated.getProductId());
    }
}
