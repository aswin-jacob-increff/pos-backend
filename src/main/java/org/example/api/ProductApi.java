package org.example.api;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.dao.ProductDao;
import org.example.pojo.ProductPojo;
import org.example.dao.InventoryDao;
import org.example.exception.ApiException;
import java.util.List;

@Service
public class ProductApi extends AbstractApi<ProductPojo> {

    @Autowired
    private ProductDao productDao;

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
        // Check if the client is active before adding a product
        if (product.getClientName() != null) {
            try {
                var client = clientApi.getByName(product.getClientName());
                if (client == null) {
                    throw new ApiException("Client '" + product.getClientName() + "' not found");
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
    protected void validateUpdate(ProductPojo existing, ProductPojo updated) {
        // Check if the client is active before updating a product
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

    public ProductPojo getByName(String name) {
        return getByField("name", name);
    }

    public ProductPojo getByBarcode(String barcode) {
        return getByField("barcode", barcode);
    }

    public List<ProductPojo> getByClientName(String clientName) {
        return productDao.selectByClientName(clientName);
    }

    public boolean hasProductsByClientName(String clientName) {
        return productDao.hasProductsByClientName(clientName);
    }
} 