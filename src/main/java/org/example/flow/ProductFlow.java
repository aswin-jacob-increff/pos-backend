package org.example.flow;

import org.example.pojo.InventoryPojo;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.ProductPojo;
import org.example.api.ProductApi;
import org.example.api.InventoryApi;
import org.example.api.OrderItemApi;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ProductFlow extends AbstractFlow<ProductPojo> {

    @Autowired
    private ProductApi api;

    @Autowired
    private InventoryApi inventoryApi;
    
    @Autowired
    private OrderItemApi orderItemApi;

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
        if (Objects.isNull(id)) {
            throw new ApiException("Product ID cannot be null");
        }
        if (Objects.isNull(productPojo)) {
            throw new ApiException("Product cannot be null");
        }
        
        // Get the existing product to compare changes
        ProductPojo existingProduct = api.get(id);
        if (existingProduct == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }
        
        // Check if any inventory-related fields have changed
        boolean inventoryNeedsUpdate = false;
        String oldBarcode = existingProduct.getBarcode();
        String newBarcode = productPojo.getBarcode();
        String oldName = existingProduct.getName();
        String newName = productPojo.getName();
        Double oldMrp = existingProduct.getMrp();
        Double newMrp = productPojo.getMrp();
        String oldClientName = existingProduct.getClientName();
        String newClientName = productPojo.getClientName();
        
        // Check if any inventory-related fields have changed
        if (!Objects.equals(oldBarcode, newBarcode) ||
            !Objects.equals(oldName, newName) ||
            !Objects.equals(oldMrp, newMrp) ||
            !Objects.equals(oldClientName.toLowerCase(), newClientName.toLowerCase())) {
            
            inventoryNeedsUpdate = true;
        }
        
        // If inventory needs to be updated, find the inventory record FIRST
        InventoryPojo inventoryToUpdate = null;
        if (inventoryNeedsUpdate) {
            inventoryToUpdate = inventoryApi.getByProductBarcode(oldBarcode);
        }
        
        // Update the product
        api.update(id, productPojo);
        
        // Now update the inventory if needed
        if (inventoryToUpdate != null) {
            // Update inventory fields to match the product changes
            if (!Objects.equals(oldBarcode, newBarcode)) {
                inventoryToUpdate.setProductBarcode(newBarcode);
            }
            if (!Objects.equals(oldName, newName)) {
                inventoryToUpdate.setProductName(newName);
            }
            if (!Objects.equals(oldMrp, newMrp)) {
                inventoryToUpdate.setProductMrp(newMrp);
            }
            if (!Objects.equals(oldClientName, newClientName)) {
                inventoryToUpdate.setClientName(newClientName);
            }
            
            // Update the inventory
            inventoryApi.update(inventoryToUpdate.getId(), inventoryToUpdate);
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
    
    /**
     * Check if a product can be safely deleted
     */
    public boolean canDeleteProduct(Integer productId) {
        if (Objects.isNull(productId)) {
            throw new ApiException("Product ID cannot be null");
        }
        ProductPojo productPojo = api.get(productId);
        if (productPojo == null) {
            return true; // Product doesn't exist, so it can be "deleted"
        }
        List<OrderItemPojo> orderItems = orderItemApi.getByProductBarcode(productPojo.getBarcode());
        return orderItems.isEmpty();
    }
    
    /**
     * Get the number of order items using this product
     */
    public int getOrderItemCountForProduct(Integer productId) {
        if (Objects.isNull(productId)) {
            throw new ApiException("Product ID cannot be null");
        }
        ProductPojo productPojo = api.get(productId);
        if (productPojo == null) {
            return 0;
        }
        List<OrderItemPojo> orderItems = orderItemApi.getByProductBarcode(productPojo.getBarcode());
        return orderItems.size();
    }
}