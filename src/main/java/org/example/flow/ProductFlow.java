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

@Service
@Transactional
public class ProductFlow {

    @Autowired
    private ProductApi api;

    @Autowired
    private InventoryApi inventoryApi;
    
    @Autowired
    private OrderItemApi orderItemApi;

    public ProductPojo add(ProductPojo productPojo) {
        productPojo = api.add(productPojo);
//        InventoryPojo inventoryPojo = new InventoryPojo();
//        inventoryPojo.setProduct(productPojo);
//        inventoryPojo.setQuantity(0);
//        inventoryApi.add(inventoryPojo);
        return productPojo;
    }

    public ProductPojo get(Integer id) {
        return api.get(id);
    }

    public ProductPojo getByBarcode(String barcode) {
        return api.getByBarcode(barcode);
    }

    public ProductPojo getByName(String name) {
        return api.getByName(name);
    }

    public List<ProductPojo> getAll() {
        return api.getAll();
    }

    public ProductPojo update(Integer id, ProductPojo productPojo) {
        // Update the product
        api.update(id, productPojo);
        
        // Note: Inventory product reference doesn't need to be updated
        // as it's a foreign key relationship that should remain stable
        // The inventory will automatically reference the updated product
        
        return api.get(id);
    }

    public void delete(Integer id) {
        // Check if product is used in any active orders
        List<OrderItemPojo> orderItems = orderItemApi.getByProductId(id);
        if (!orderItems.isEmpty()) {
            throw new ApiException("Cannot delete product with ID " + id + " as it is used in " + orderItems.size() + " order items");
        }
        
        // Delete inventory first (due to foreign key constraint)
        InventoryPojo inventoryPojo = inventoryApi.getByProductId(id);
        if (inventoryPojo != null) {
            inventoryApi.delete(inventoryPojo.getId());
        }
        
        // Then delete the product
        api.delete(id);
    }

    public void deleteByName(String name) {
        ProductPojo productPojo = api.getByName(name);
        if (productPojo == null) {
            throw new ApiException("Product with name '" + name + "' not found");
        }
        delete(productPojo.getId());
    }

    public void deleteByBarcode(String barcode) {
        ProductPojo productPojo = api.getByBarcode(barcode);
        if (productPojo == null) {
            throw new ApiException("Product with barcode '" + barcode + "' not found");
        }
        delete(productPojo.getId());
    }
    
    /**
     * Check if a product can be safely deleted
     */
    public boolean canDeleteProduct(Integer productId) {
        List<OrderItemPojo> orderItems = orderItemApi.getByProductId(productId);
        return orderItems.isEmpty();
    }
    
    /**
     * Get the number of order items using this product
     */
    public int getOrderItemCountForProduct(Integer productId) {
        List<OrderItemPojo> orderItems = orderItemApi.getByProductId(productId);
        return orderItems.size();
    }
}