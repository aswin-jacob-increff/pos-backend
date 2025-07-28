package org.example.api;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.pojo.ProductPojo;
import org.example.dao.InventoryDao;
import org.example.exception.ApiException;
import java.util.List;
import java.util.Objects;

@Service
public class ProductApi extends AbstractApi<ProductPojo> {

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private ClientApi clientApi;

    @Override
    protected String getEntityName() {
        return "Product";
    }

    @Override
    protected void validateAdd(ProductPojo product) {
        if (product == null) {
            throw new ApiException("Product cannot be null");
        }
        
        // Check if the client is active before adding a product
        if (product.getClientId() != null) {
            try {
                var client = clientApi.get(product.getClientId());
                if (client == null) {
                    throw new ApiException("Client with ID '" + product.getClientId() + "' not found");
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
        
        // Check for duplicate barcode
        if (product.getBarcode() != null) {
            ProductPojo existingProduct = ((org.example.dao.ProductDao) dao).selectByBarcode(product.getBarcode());
            if (existingProduct != null) {
                throw new ApiException("Product with barcode '" + product.getBarcode() + "' already exists");
            }
        }
    }

    @Override
    protected void validateUpdate(ProductPojo existing, ProductPojo updated) {
        // Check if the client is active before updating a product
        if (updated.getClientId() != null) {
            try {
                var client = clientApi.get(updated.getClientId());
                if (client == null) {
                    throw new ApiException("Client with ID '" + updated.getClientId() + "' not found");
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
        
        // Check for duplicate barcode (only if barcode is being changed)
        if (updated.getBarcode() != null && !updated.getBarcode().equals(existing.getBarcode())) {
            ProductPojo existingProduct = ((org.example.dao.ProductDao) dao).selectByBarcode(updated.getBarcode());
            if (existingProduct != null) {
                throw new ApiException("Product with barcode '" + updated.getBarcode() + "' already exists");
            }
        }
    }

    public ProductPojo getByName(String name) {
        if (Objects.isNull(name) || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
        return getByField("name", name);
    }

    public List<ProductPojo> getByNameLike(String name) {
        if (Objects.isNull(name) || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
        return dao.selectByFieldLike("name", name);
    }

    public ProductPojo getByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        ProductPojo product = ((org.example.dao.ProductDao) dao).selectByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Product with barcode '" + barcode + "' not found");
        }
        return product;
    }

    public List<ProductPojo> getByBarcodeLike(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        return dao.selectByFieldLike("barcode", barcode);
    }

    public List<ProductPojo> getByClientId(Integer clientId) {
        if (Objects.isNull(clientId)) {
            throw new ApiException("Client ID cannot be null");
        }
        return ((org.example.dao.ProductDao) dao).selectByClientId(clientId);
    }

    public List<ProductPojo> getByClientName(String clientName) {
        if (Objects.isNull(clientName) || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        // Get client ID from client name first
        try {
            var client = clientApi.getByName(clientName);
            if (client == null) {
                throw new ApiException("Client with name '" + clientName + "' not found");
            }
            return getByClientId(client.getId());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error getting client: " + e.getMessage());
        }
    }

    public boolean hasProductsByClientId(Integer clientId) {
        if (Objects.isNull(clientId)) {
            return false;
        }
        return ((org.example.dao.ProductDao) dao).hasProductsByClientId(clientId);
    }

    public boolean hasProductsByClientName(String clientName) {
        if (Objects.isNull(clientName) || clientName.trim().isEmpty()) {
            return false;
        }
        try {
            var client = clientApi.getByName(clientName);
            if (client == null) {
                return false;
            }
            return hasProductsByClientId(client.getId());
        } catch (Exception e) {
            return false;
        }
    }

    public org.example.model.data.PaginationResponse<ProductPojo> getByNameLikePaginated(String name, org.example.model.form.PaginationRequest request) {
        if (Objects.isNull(name) || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
        return dao.selectByFieldLikePaginated("name", name, request);
    }

    public org.example.model.data.PaginationResponse<ProductPojo> getByClientIdPaginated(Integer clientId, org.example.model.form.PaginationRequest request) {
        if (Objects.isNull(clientId)) {
            throw new ApiException("Client ID cannot be null");
        }
        return ((org.example.dao.ProductDao) dao).selectByClientIdPaginated(clientId, request);
    }

    public org.example.model.data.PaginationResponse<ProductPojo> getByClientNamePaginated(String clientName, org.example.model.form.PaginationRequest request) {
        if (Objects.isNull(clientName) || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        // Get client ID from client name first
        try {
            var client = clientApi.getByName(clientName);
            if (client == null) {
                throw new ApiException("Client with name '" + clientName + "' not found");
            }
            return getByClientIdPaginated(client.getId(), request);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error getting client: " + e.getMessage());
        }
    }
} 