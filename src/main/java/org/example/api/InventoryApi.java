package org.example.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.exception.ApiException;
import java.util.Objects;
import java.util.List;
import org.example.dao.InventoryDao;
import org.example.pojo.InventoryPojo;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import org.example.api.ProductApi;

@Service
public class InventoryApi extends AbstractApi<InventoryPojo> {

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ProductApi productApi;

    @Override
    protected String getEntityName() {
        return "Inventory";
    }

    @Override
    protected void validateAdd(InventoryPojo inventory) {
        // Check if productId is present
        if (inventory.getProductId() == null) {
            throw new ApiException("Product ID is required for inventory");
        }
        // Check if the product exists and its client is active
        try {
            org.example.pojo.ProductPojo product = productApi.get(inventory.getProductId());
            if (product == null) {
                throw new ApiException("Product with ID '" + inventory.getProductId() + "' not found");
            }
            // Check if the client is active
            if (product.getClientId() != null && product.getClientId() > 0) {
                try {
                    org.example.pojo.ClientPojo client = clientApi.get(product.getClientId());
                    if (client == null) {
                        throw new ApiException("Client for product not found");
                    }
                    if (!client.getStatus()) {
                        throw new ApiException("Client is not active");
                    }
                } catch (Exception e) {
                    throw new ApiException("Error validating client: " + e.getMessage());
                }
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error validating product: " + e.getMessage());
        }
    }

    @Override
    protected void validateUpdate(InventoryPojo existing, InventoryPojo updated) {
        // Check if productId is present
        if (updated.getProductId() == null) {
            throw new ApiException("Product ID is required for inventory");
        }
        // Check if the product exists and its client is active
        try {
            org.example.pojo.ProductPojo product = productApi.get(updated.getProductId());
            if (product == null) {
                throw new ApiException("Product with ID '" + updated.getProductId() + "' not found");
            }
            // Check if the client is active
            if (product.getClientId() != null && product.getClientId() > 0) {
                try {
                    org.example.pojo.ClientPojo client = clientApi.get(product.getClientId());
                    if (client == null) {
                        throw new ApiException("Client for product not found");
                    }
                    if (!client.getStatus()) {
                        throw new ApiException("Client is not active");
                    }
                } catch (Exception e) {
                    throw new ApiException("Error validating client: " + e.getMessage());
                }
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error validating product: " + e.getMessage());
        }
    }

    public InventoryPojo getByProductId(Integer productId) {
        return inventoryDao.getByProductId(productId);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all inventory items with pagination support.
     */
    public PaginationResponse<InventoryPojo> getAllPaginated(PaginationRequest request) {
        return inventoryDao.getAllPaginated(request);
    }

    /**
     * Get inventory by product ID with pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductIdPaginated(Integer productId, PaginationRequest request) {
        return inventoryDao.getByProductIdPaginated(productId, request);
    }

    public InventoryPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Inventory ID cannot be null");
        }
        InventoryPojo inventory = inventoryDao.select(id);
        if (inventory == null) {
            throw new ApiException("Inventory with ID " + id + " not found");
        }
        return inventory;
    }

    /**
     * Add stock to existing inventory by product ID
     */
    public void addStock(Integer productId, Integer quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new ApiException("Quantity to add must be positive");
        }
        InventoryPojo inventory = getByProductId(productId);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        // Check if the product exists and its client is active
        try {
            org.example.pojo.ProductPojo product = productApi.get(productId);
            if (product == null) {
                throw new ApiException("Product with ID '" + productId + "' not found");
            }
            if (product.getClientId() != null && product.getClientId() > 0) {
                org.example.pojo.ClientPojo client = clientApi.get(product.getClientId());
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
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductId(inventory.getProductId());
        updatedInventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }

    /**
     * Remove stock from existing inventory by product ID
     */
    public void removeStock(Integer productId, Integer quantityToRemove) {
        if (quantityToRemove <= 0) {
            throw new ApiException("Quantity to remove must be positive");
        }
        InventoryPojo inventory = getByProductId(productId);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        if (inventory.getQuantity() < quantityToRemove) {
            throw new ApiException("Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: " + quantityToRemove);
        }
        
        // Check if the product exists and its client is active
        try {
            org.example.pojo.ProductPojo product = productApi.get(productId);
            if (product == null) {
                throw new ApiException("Product with ID '" + productId + "' not found");
            }
            if (product.getClientId() != null && product.getClientId() > 0) {
                org.example.pojo.ClientPojo client = clientApi.get(product.getClientId());
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
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductId(inventory.getProductId());
        updatedInventory.setQuantity(inventory.getQuantity() - quantityToRemove);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }

    /**
     * Set stock to a specific quantity by product ID
     */
    public void setStock(Integer productId, Integer newQuantity) {
        if (newQuantity < 0) {
            throw new ApiException("Stock quantity cannot be negative");
        }
        InventoryPojo inventory = getByProductId(productId);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        
        // Check if the product exists and its client is active
        try {
            org.example.pojo.ProductPojo product = productApi.get(productId);
            if (product == null) {
                throw new ApiException("Product with ID '" + productId + "' not found");
            }
            if (product.getClientId() != null && product.getClientId() > 0) {
                org.example.pojo.ClientPojo client = clientApi.get(product.getClientId());
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
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductId(inventory.getProductId());
        updatedInventory.setQuantity(newQuantity);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }

    @Override
    public void add(InventoryPojo inventory) {
        if (Objects.isNull(inventory)) {
            throw new ApiException("Inventory cannot be null");
        }
        super.add(inventory);
    }

    @Override
    public void update(Integer id, InventoryPojo updatedInventory) {
        if (Objects.isNull(updatedInventory)) {
            throw new ApiException("Inventory cannot be null");
        }
        super.update(id, updatedInventory);
    }
} 