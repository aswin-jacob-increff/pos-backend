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
public class ProductFlow extends AbstractFlow<ProductPojo> {

    @Autowired
    private ProductApi api;

    @Autowired
    private InventoryApi inventoryApi;
    
    @Autowired
    private OrderItemApi orderItemApi;

    @Override
    protected Integer getEntityId(ProductPojo entity) {
        return entity.getId();
    }

    @Override
    protected String getEntityName() {
        return "Product";
    }

    public ProductPojo getByBarcode(String barcode) {
        return api.getByBarcode(barcode);
    }

    public ProductPojo getByName(String name) {
        return api.getByName(name);
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
        ProductPojo productPojo = api.get(productId);
        if (productPojo == null) {
            return 0;
        }
        List<OrderItemPojo> orderItems = orderItemApi.getByProductBarcode(productPojo.getBarcode());
        return orderItems.size();
    }

    @Override
    public void delete(Integer id) {
        // Get product details first
        ProductPojo productPojo = api.get(id);
        if (productPojo == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }
        // Check if product is used in any active orders
        List<OrderItemPojo> orderItems = orderItemApi.getByProductBarcode(productPojo.getBarcode());
        if (!orderItems.isEmpty()) {
            throw new ApiException("Cannot delete product with ID " + id + " as it is used in " + orderItems.size() + " order items");
        }
        // Delete inventory first (due to foreign key constraint)
        InventoryPojo inventoryPojo = inventoryApi.getByProductBarcode(productPojo.getBarcode());
        if (inventoryPojo != null) {
            inventoryApi.delete(inventoryPojo.getId());
        }
        // Then delete the product
        api.delete(id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ProductPojo update(Integer id, ProductPojo updatedProduct) {
        // Get the existing product to check if barcode is changing
        ProductPojo existingProduct = api.get(id);
        if (existingProduct == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }

        // Check if barcode is being changed
        boolean barcodeChanged = !existingProduct.getBarcode().equals(updatedProduct.getBarcode());
        
        System.out.println("Updating product: " + existingProduct.getBarcode() + " -> " + updatedProduct.getBarcode());
        
        // Update the product first
        api.update(id, updatedProduct);
        
        // Now update the corresponding inventory
        try {
            InventoryPojo inventory = inventoryApi.getByProductBarcode(existingProduct.getBarcode());
            if (inventory != null) {
                // Create updated inventory with new product details
                InventoryPojo updatedInventory = new InventoryPojo();
                updatedInventory.setProductBarcode(updatedProduct.getBarcode());
                updatedInventory.setProductName(updatedProduct.getName());
                updatedInventory.setClientName(updatedProduct.getClientName());
                updatedInventory.setProductMrp(updatedProduct.getMrp());
                updatedInventory.setProductImageUrl(updatedProduct.getImageUrl());
                updatedInventory.setQuantity(inventory.getQuantity()); // Preserve existing quantity
                
                // If barcode changed, we need to handle it carefully
                if (barcodeChanged) {
                    // Check if new barcode already has inventory
                    InventoryPojo existingInventoryWithNewBarcode = inventoryApi.getByProductBarcode(updatedProduct.getBarcode());
                    if (existingInventoryWithNewBarcode != null && !existingInventoryWithNewBarcode.getId().equals(inventory.getId())) {
                        throw new ApiException("Cannot update product barcode: inventory already exists for barcode '" + updatedProduct.getBarcode() + "'");
                    }
                }
                
                // Update the inventory
                inventoryApi.update(inventory.getId(), updatedInventory);
                System.out.println("Updated inventory for product: " + updatedProduct.getBarcode());
            } else {
                System.out.println("No inventory found for product: " + existingProduct.getBarcode() + ", creating new inventory");
                // Create inventory for the product if it doesn't exist
                InventoryPojo newInventory = new InventoryPojo();
                newInventory.setProductBarcode(updatedProduct.getBarcode());
                newInventory.setProductName(updatedProduct.getName());
                newInventory.setClientName(updatedProduct.getClientName());
                newInventory.setProductMrp(updatedProduct.getMrp());
                newInventory.setProductImageUrl(updatedProduct.getImageUrl());
                newInventory.setQuantity(0); // Start with 0 quantity
                
                inventoryApi.add(newInventory);
            }
        } catch (Exception e) {
            // If inventory update fails, we should rollback the product update
            throw new ApiException("Failed to update inventory for product: " + e.getMessage());
        }
        
        return api.get(id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ProductPojo add(ProductPojo productPojo) {
        // Add the product first
        api.add(productPojo);
        
        // Check if inventory exists for this product
        try {
            InventoryPojo existingInventory = inventoryApi.getByProductBarcode(productPojo.getBarcode());
            if (existingInventory == null) {
                // Create inventory for the new product
                InventoryPojo newInventory = new InventoryPojo();
                newInventory.setProductBarcode(productPojo.getBarcode());
                newInventory.setProductName(productPojo.getName());
                newInventory.setClientName(productPojo.getClientName());
                newInventory.setProductMrp(productPojo.getMrp());
                newInventory.setProductImageUrl(productPojo.getImageUrl());
                newInventory.setQuantity(0); // Start with 0 quantity
                
                inventoryApi.add(newInventory);
                System.out.println("Created inventory for new product: " + productPojo.getBarcode());
            } else {
                System.out.println("Inventory already exists for product: " + productPojo.getBarcode());
            }
        } catch (Exception e) {
            // If inventory creation fails, we should rollback the product creation
            throw new ApiException("Failed to create inventory for product: " + e.getMessage());
        }
        
        return productPojo;
    }
}