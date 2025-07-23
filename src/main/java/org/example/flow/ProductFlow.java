package org.example.flow;

import org.example.pojo.InventoryPojo;
import org.example.pojo.ProductPojo;
import org.example.api.ProductApi;
import org.example.api.InventoryApi;
import org.example.exception.ApiException;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import org.example.pojo.ClientPojo;
import org.example.api.ClientApi;

@Service
@Transactional
public class ProductFlow extends AbstractFlow<ProductPojo> {

    @Autowired
    private ProductApi api;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ClientApi clientApi;

    public ProductFlow() {
        super(ProductPojo.class);
    }

    @Override
    protected Integer getEntityId(ProductPojo entity) {
        return entity.getId();
    }

    @Override
    protected String getEntityName() {
        return "Product";
    }

    @Override
    public ProductPojo add(ProductPojo productPojo) {
        if (Objects.isNull(productPojo)) {
            throw new ApiException("Product cannot be null");
        }
        return super.add(productPojo);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void update(Integer id, ProductPojo productPojo) {
        if (id == null) {
            throw new ApiException("Product ID cannot be null");
        }
        if (productPojo == null) {
            throw new ApiException("Product cannot be null");
        }

        // Get the existing product to compare changes
        ProductPojo existingProduct = api.get(id);
        if (existingProduct == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }

        // Store old values for comparison
        String oldBarcode = existingProduct.getBarcode();
        String newBarcode = productPojo.getBarcode();
        String oldName = existingProduct.getName();
        String newName = productPojo.getName();
        Double oldMrp = existingProduct.getMrp();
        Double newMrp = productPojo.getMrp();
        Integer oldClientId = existingProduct.getClientId();
        Integer newClientId = productPojo.getClientId();

        // Get client name for the new client ID
        String newClientName = getClientNameById(newClientId);

        // Check if any inventory-related fields have changed
        boolean inventoryNeedsUpdate = !Objects.equals(oldBarcode, newBarcode) ||
                                     !Objects.equals(oldName, newName) ||
                                     !Objects.equals(oldMrp, newMrp) ||
                                     !Objects.equals(oldClientId, newClientId);

        // Update the product
        api.update(id, productPojo);
        
        // Note: Inventory no longer needs to be updated when product details change
        // because inventory now only stores productId and fetches product details
        // dynamically through the product API
    }
    
    /**
     * Helper method to get client name by client ID
     */
    private String getClientNameById(Integer clientId) {
        if (clientId == null) {
            return null;
        }
        
        try {
            // Get client name from client API
            ClientPojo client = clientApi.get(clientId);
            return client != null ? client.getClientName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<ProductPojo> getAll() {
        return api.getAll();
    }
    
    public ProductPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Product ID cannot be null");
        }
        return api.get(id);
    }
    
    public ProductPojo getByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        return api.getByBarcode(barcode);
    }

    public List<ProductPojo> getByBarcodeLike(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        return api.getByBarcodeLike(barcode);
    }

    public ProductPojo getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
        return api.getByName(name);
    }

    public List<ProductPojo> getByNameLike(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
        return api.getByNameLike(name);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all products with pagination support.
     */
    public PaginationResponse<ProductPojo> getAllPaginated(PaginationRequest request) {
        return api.getAllPaginated(request);
    }

    /**
     * Get products by barcode with pagination support.
     */
    public PaginationResponse<ProductPojo> getByBarcodePaginated(String barcode, PaginationRequest request) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        return api.getByBarcodePaginated(barcode, request);
    }

    /**
     * Get products by barcode with partial matching and pagination support.
     */
    public PaginationResponse<ProductPojo> getByBarcodeLikePaginated(String barcode, PaginationRequest request) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        return api.getByBarcodeLikePaginated(barcode, request);
    }

    /**
     * Get products by name with pagination support.
     */
    public PaginationResponse<ProductPojo> getByNamePaginated(String name, PaginationRequest request) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
        return api.getByNamePaginated(name, request);
    }

    /**
     * Get products by name with partial matching and pagination support.
     */
    public PaginationResponse<ProductPojo> getByNameLikePaginated(String name, PaginationRequest request) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
        return api.getByNameLikePaginated(name, request);
    }

    /**
     * Get products by client ID with pagination support.
     */
    public PaginationResponse<ProductPojo> getByClientIdPaginated(Integer clientId, PaginationRequest request) {
        if (clientId == null) {
            throw new ApiException("Client ID cannot be null");
        }
        return api.getByClientIdPaginated(clientId, request);
    }

    /**
     * Get products by client name with pagination support.
     */
    public PaginationResponse<ProductPojo> getByClientNamePaginated(String clientName, PaginationRequest request) {
        if (clientName == null || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        return api.getByClientNamePaginated(clientName, request);
    }
}