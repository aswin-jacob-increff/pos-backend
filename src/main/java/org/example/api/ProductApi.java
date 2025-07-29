package org.example.api;

import jakarta.transaction.Transactional;
import org.example.exception.ApiException;
import org.example.pojo.ProductPojo;
import org.example.dao.ProductDao;
import org.example.dao.InventoryDao;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ProductApi extends AbstractApi<ProductPojo> {

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private ClientApi clientApi;

    public ProductApi() {
        super(ProductPojo.class);
    }

    public ProductPojo getByName(String name) {
        return super.getByName(name); // Uses abstract method
    }

    public List<ProductPojo> getByNameLike(String name) {
        return super.getByNameLike(name); // Uses abstract method
    }

    public ProductPojo getByBarcode(String barcode) {
        validateString(barcode, "Barcode");
        return getByField("barcode", barcode);
    }

    public List<ProductPojo> getByBarcodeLike(String barcode) {
        validateString(barcode, "Barcode");
        return dao.selectByFieldLike("barcode", barcode);
    }

    public List<ProductPojo> getByClientId(Integer clientId) {
        if (Objects.isNull(clientId)) {
            throw new ApiException("Client ID cannot be null");
        }
        return ((ProductDao) dao).selectByClientId(clientId);
    }

    public List<ProductPojo> getByClientName(String clientName) {
        validateString(clientName, "Client name");
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
        return ((ProductDao) dao).hasProductsByClientId(clientId);
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

    public PaginationResponse<ProductPojo> getByNameLikePaginated(String name, PaginationRequest request) {
        return getByFieldLikePaginatedWithValidation("name", name, request, "Product name");
    }

    public PaginationResponse<ProductPojo> getByClientIdPaginated(Integer clientId, PaginationRequest request) {
        if (Objects.isNull(clientId)) {
            throw new ApiException("Client ID cannot be null");
        }
        return ((ProductDao) dao).selectByClientIdPaginated(clientId, request);
    }

    public PaginationResponse<ProductPojo> getByClientNamePaginated(String clientName, PaginationRequest request) {
        validateString(clientName, "Client name");
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