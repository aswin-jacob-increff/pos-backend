package org.example.flow;

import org.example.pojo.InventoryPojo;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.ProductPojo;
import org.example.service.ProductService;
import org.example.service.InventoryService;
import org.example.service.OrderItemService;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class ProductFlow {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private OrderItemService orderItemService;

    public ProductPojo add(ProductPojo productPojo) {
        productPojo = productService.add(productPojo);
        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProduct(productPojo);
        inventoryPojo.setQuantity(0);
        inventoryService.add(inventoryPojo);
        return productPojo;
    }

    public ProductPojo get(Integer id) {
        return productService.get(id);
    }

    public ProductPojo getByBarcode(String barcode) {
        return productService.getByBarcode(barcode);
    }

    public ProductPojo getByName(String name) {
        return productService.getByName(name);
    }

    public List<ProductPojo> getAll() {
        return productService.getAll();
    }

    public ProductPojo update(Integer id, ProductPojo productPojo) {
        // Update the product
        productService.update(id, productPojo);
        
        // Note: Inventory product reference doesn't need to be updated
        // as it's a foreign key relationship that should remain stable
        // The inventory will automatically reference the updated product
        
        return productService.get(id);
    }

    public void delete(Integer id) {
        // Check if product is used in any active orders
        List<OrderItemPojo> orderItems = orderItemService.getByProductId(id);
        if (!orderItems.isEmpty()) {
            throw new ApiException("Cannot delete product with ID " + id + " as it is used in " + orderItems.size() + " order items");
        }
        
        // Delete inventory first (due to foreign key constraint)
        InventoryPojo inventoryPojo = inventoryService.getByProductId(id);
        if (inventoryPojo != null) {
            inventoryService.delete(inventoryPojo.getId());
        }
        
        // Then delete the product
        productService.delete(id);
    }

    public void deleteByName(String name) {
        ProductPojo productPojo = productService.getByName(name);
        if (productPojo == null) {
            throw new ApiException("Product with name '" + name + "' not found");
        }
        delete(productPojo.getId());
    }

    public void deleteByBarcode(String barcode) {
        ProductPojo productPojo = productService.getByBarcode(barcode);
        if (productPojo == null) {
            throw new ApiException("Product with barcode '" + barcode + "' not found");
        }
        delete(productPojo.getId());
    }
    
    /**
     * Check if a product can be safely deleted
     */
    public boolean canDeleteProduct(Integer productId) {
        List<OrderItemPojo> orderItems = orderItemService.getByProductId(productId);
        return orderItems.isEmpty();
    }
    
    /**
     * Get the number of order items using this product
     */
    public int getOrderItemCountForProduct(Integer productId) {
        List<OrderItemPojo> orderItems = orderItemService.getByProductId(productId);
        return orderItems.size();
    }
}