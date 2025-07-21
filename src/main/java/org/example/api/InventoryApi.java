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

@Service
public class InventoryApi extends AbstractApi<InventoryPojo> {

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private ClientApi clientApi;

    @Override
    protected String getEntityName() {
        return "Inventory";
    }

    @Override
    protected void validateAdd(InventoryPojo inventory) {
        // Check if the client name is present
        if (inventory.getClientName() == null) {
            throw new ApiException("Client name is required for inventory");
        }
        // Check if the client is active before adding inventory
        if (inventory.getClientName() != null) {
            try {
                var client = clientApi.getByName(inventory.getClientName());
                if (client == null) {
                    throw new ApiException("Client '" + inventory.getClientName() + "' not found");
                }
                if (!client.getStatus()) {
                    throw new ApiException("Client is not active");
                }
            } catch (ApiException e) {
                throw e;
            } catch (Exception e) {
                throw new ApiException("Error validating client: " + e.getMessage());
            }
        }
    }

    @Override
    protected void validateUpdate(InventoryPojo existing, InventoryPojo updated) {
        // Check if the client name is present
        if (updated.getClientName() == null) {
            throw new ApiException("Client name is required for inventory");
        }
        // Check if the client is active before updating inventory
        if (updated.getClientName() != null) {
            try {
                var client = clientApi.getByName(updated.getClientName());
                if (client == null) {
                    throw new ApiException("Client '" + updated.getClientName() + "' not found");
                }
                if (!client.getStatus()) {
                    throw new ApiException("Client is not active");
                }
            } catch (ApiException e) {
                throw e;
            } catch (Exception e) {
                throw new ApiException("Error validating client: " + e.getMessage());
            }
        }
    }

    public InventoryPojo getByProductName(String productName) {
        return inventoryDao.getByProductName(productName);
    }

    public List<InventoryPojo> getByProductNameLike(String productName) {
        return inventoryDao.getByProductNameLike(productName);
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        return inventoryDao.getByProductBarcode(barcode);
    }

    public List<InventoryPojo> getByProductBarcodeLike(String barcode) {
        return inventoryDao.getByProductBarcodeLike(barcode);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all inventory items with pagination support.
     */
    public PaginationResponse<InventoryPojo> getAllPaginated(PaginationRequest request) {
        return inventoryDao.getAllPaginated(request);
    }

    /**
     * Get inventory by product name with pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductNamePaginated(String productName, PaginationRequest request) {
        return inventoryDao.getByProductNamePaginated(productName, request);
    }

    /**
     * Get inventory by product name with partial matching and pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductNameLikePaginated(String productName, PaginationRequest request) {
        return inventoryDao.getByProductNameLikePaginated(productName, request);
    }

    /**
     * Get inventory by product barcode with pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductBarcodePaginated(String barcode, PaginationRequest request) {
        return inventoryDao.getByProductBarcodePaginated(barcode, request);
    }

    /**
     * Get inventory by product barcode with partial matching and pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductBarcodeLikePaginated(String barcode, PaginationRequest request) {
        return inventoryDao.getByProductBarcodeLikePaginated(barcode, request);
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
     * Add stock to existing inventory
     */
    public void addStock(String barcode, Integer quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new ApiException("Quantity to add must be positive");
        }
        InventoryPojo inventory = getByProductBarcode(barcode);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product barcode: " + barcode);
        }
        
        // Check if the client is active before adding stock
        try {
            var client = clientApi.getByName(inventory.getClientName());
            if (client == null) {
                throw new ApiException("Client '" + inventory.getClientName() + "' not found");
            }
            if (!client.getStatus()) {
                throw new ApiException("Client is not active");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error validating client: " + e.getMessage());
        }
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductBarcode(inventory.getProductBarcode());
        updatedInventory.setProductName(inventory.getProductName());
        updatedInventory.setClientName(inventory.getClientName());
        updatedInventory.setProductMrp(inventory.getProductMrp());
        updatedInventory.setProductImageUrl(inventory.getProductImageUrl());
        updatedInventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }

    /**
     * Remove stock from existing inventory
     */
    public void removeStock(String barcode, Integer quantityToRemove) {
        if (quantityToRemove <= 0) {
            throw new ApiException("Quantity to remove must be positive");
        }
        InventoryPojo inventory = getByProductBarcode(barcode);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product barcode: " + barcode);
        }
        if (inventory.getQuantity() < quantityToRemove) {
            throw new ApiException("Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: " + quantityToRemove);
        }
        
        // Check if the client is active before removing stock
        try {
            var client = clientApi.getByName(inventory.getClientName());
            if (client == null) {
                throw new ApiException("Client '" + inventory.getClientName() + "' not found");
            }
            if (!client.getStatus()) {
                throw new ApiException("Client is not active");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error validating client: " + e.getMessage());
        }
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductBarcode(inventory.getProductBarcode());
        updatedInventory.setProductName(inventory.getProductName());
        updatedInventory.setClientName(inventory.getClientName());
        updatedInventory.setProductMrp(inventory.getProductMrp());
        updatedInventory.setProductImageUrl(inventory.getProductImageUrl());
        updatedInventory.setQuantity(inventory.getQuantity() - quantityToRemove);
        inventoryDao.update(inventory.getId(), updatedInventory);
    }

    /**
     * Set stock to a specific quantity
     */
    public void setStock(String barcode, Integer newQuantity) {
        if (newQuantity < 0) {
            throw new ApiException("Stock quantity cannot be negative");
        }
        InventoryPojo inventory = getByProductBarcode(barcode);
        if (Objects.isNull(inventory)) {
            throw new ApiException("No inventory found for product barcode: " + barcode);
        }
        
        // Check if the client is active before setting stock
        try {
            var client = clientApi.getByName(inventory.getClientName());
            if (client == null) {
                throw new ApiException("Client '" + inventory.getClientName() + "' not found");
            }
            if (!client.getStatus()) {
                throw new ApiException("Client is not active");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error validating client: " + e.getMessage());
        }
        
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductBarcode(inventory.getProductBarcode());
        updatedInventory.setProductName(inventory.getProductName());
        updatedInventory.setClientName(inventory.getClientName());
        updatedInventory.setProductMrp(inventory.getProductMrp());
        updatedInventory.setProductImageUrl(inventory.getProductImageUrl());
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