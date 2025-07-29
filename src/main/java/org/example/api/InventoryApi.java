package org.example.api;

import org.springframework.transaction.annotation.Transactional;
import org.example.exception.ApiException;
import org.example.pojo.InventoryPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.dao.InventoryDao;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class InventoryApi extends AbstractApi<InventoryPojo> {

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ProductApi productApi;

    public InventoryApi() {
        super(InventoryPojo.class);
    }

    public InventoryPojo getByProductId(Integer productId) {
        return ((InventoryDao) dao).getByProductId(productId);
    }

    /**
     * Validate product and its client before inventory operations
     */
    private void validateProductAndClient(Integer productId) {
        try {
            ProductPojo product = productApi.get(productId);
            if (product == null) {
                throw new ApiException("Product with ID '" + productId + "' not found");
            }
            if (product.getClientId() != null && product.getClientId() > 0) {
                ClientPojo client = clientApi.get(product.getClientId());
                if (client == null) {
                    throw new ApiException("Client for product not found");
                }
                if (!client.getStatus()) {
                    throw new ApiException("Client is not active");
                }
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error validating product: " + e.getMessage());
        }
    }

    /**
     * Add stock to existing inventory by product ID
     */
    public void addStock(Integer productId, Integer quantityToAdd) {
        validatePositive(quantityToAdd, "Quantity to add");
        InventoryPojo inventory = getByProductId(productId);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        validateProductAndClient(productId);
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductId(inventory.getProductId());
        updatedInventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        dao.update(inventory.getId(), updatedInventory);
    }

    /**
     * Remove stock from existing inventory by product ID
     */
    public void removeStock(Integer productId, Integer quantityToRemove) {
        validatePositive(quantityToRemove, "Quantity to remove");
        InventoryPojo inventory = getByProductId(productId);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        if (inventory.getQuantity() < quantityToRemove) {
            throw new ApiException("Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: " + quantityToRemove);
        }
        
        validateProductAndClient(productId);
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductId(inventory.getProductId());
        updatedInventory.setQuantity(inventory.getQuantity() - quantityToRemove);
        dao.update(inventory.getId(), updatedInventory);
    }

    /**
     * Set stock to a specific quantity by product ID
     */
    public void setStock(Integer productId, Integer newQuantity) {
        validateNonNegative(newQuantity, "Stock quantity");
        InventoryPojo inventory = getByProductId(productId);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        validateProductAndClient(productId);
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductId(inventory.getProductId());
        updatedInventory.setQuantity(newQuantity);
        dao.update(inventory.getId(), updatedInventory);
    }

    public PaginationResponse<InventoryPojo> getByProductIdPaginated(Integer productId, PaginationRequest request) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        if (request == null) {
            request = new PaginationRequest();
        }
        return getPaginated(PaginationQuery.byField("productId", productId, request));
    }
} 