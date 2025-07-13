package org.example.api;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.dao.ProductDao;
import org.example.pojo.ProductPojo;
import org.example.dao.InventoryDao;
import org.example.exception.ApiException;
import java.util.List;
import java.util.Objects;

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
    public void add(ProductPojo product) {
        if (Objects.isNull(product)) {
            throw new ApiException("Product cannot be null");
        }
        validateAdd(product);
        super.add(product);
    }

    public ProductPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Product ID cannot be null");
        }
        ProductPojo product = productDao.select(id);
        if (product == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }
        return product;
    }

    @Override
    public void update(Integer id, ProductPojo product) {
        if (Objects.isNull(id)) {
            throw new ApiException("Product ID cannot be null");
        }
        if (Objects.isNull(product)) {
            throw new ApiException("Product cannot be null");
        }
        ProductPojo existing = get(id);
        if (existing == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }
        validateUpdate(existing, product);
        super.update(id, product);
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
        
        // Check for duplicate barcode
        if (product.getBarcode() != null) {
            ProductPojo existingProduct = productDao.selectByBarcode(product.getBarcode());
            if (existingProduct != null) {
                throw new ApiException("Product with barcode '" + product.getBarcode() + "' already exists");
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
        
        // Check for duplicate barcode (only if barcode is being changed)
        if (updated.getBarcode() != null && !updated.getBarcode().equals(existing.getBarcode())) {
            ProductPojo existingProduct = productDao.selectByBarcode(updated.getBarcode());
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

    public ProductPojo getByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        ProductPojo product = productDao.selectByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Product with barcode '" + barcode + "' not found");
        }
        return product;
    }

    public List<ProductPojo> getByClientName(String clientName) {
        if (Objects.isNull(clientName) || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        return productDao.selectByClientName(clientName);
    }

    public boolean hasProductsByClientName(String clientName) {
        if (Objects.isNull(clientName) || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        return productDao.hasProductsByClientName(clientName);
    }
} 