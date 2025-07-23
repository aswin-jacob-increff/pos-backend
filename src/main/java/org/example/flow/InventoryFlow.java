package org.example.flow;

import org.example.pojo.InventoryPojo;
import org.example.api.InventoryApi;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
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

    public InventoryPojo getByProductId(Integer productId) {
        return api.getByProductId(productId);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all inventory items with pagination support.
     */
    public PaginationResponse<InventoryPojo> getAllPaginated(PaginationRequest request) {
        return api.getAllPaginated(request);
    }

    /**
     * Get inventory by product ID with pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductIdPaginated(Integer productId, PaginationRequest request) {
        return api.getByProductIdPaginated(productId, request);
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
