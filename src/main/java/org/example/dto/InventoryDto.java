package org.example.dto;

import org.example.model.InventoryForm;
import org.example.model.InventoryData;
import org.example.pojo.InventoryPojo;
import org.example.flow.InventoryFlow;
import org.example.api.ProductApi;
import org.example.exception.ApiException;
import org.example.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import jakarta.validation.Valid;
import java.util.ArrayList;

@Component
public class InventoryDto extends AbstractDto<InventoryPojo, InventoryForm, InventoryData> {

    @Autowired
    private InventoryFlow inventoryFlow;

    @Autowired
    private ProductApi productApi;

    @Override
    protected String getEntityName() {
        return "Inventory";
    }

    @Override
    protected InventoryPojo convertFormToEntity(InventoryForm inventoryForm) {
        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProductBarcode(inventoryForm.getBarcode());
        inventoryPojo.setQuantity(inventoryForm.getQuantity());
        
        // Get product details to populate null fields
        try {
            var product = productApi.getByBarcode(inventoryForm.getBarcode());
            inventoryPojo.setProductName(product.getName());
            inventoryPojo.setClientName(StringUtil.format(product.getClientName()));
            inventoryPojo.setProductMrp(product.getMrp());
            inventoryPojo.setProductImageUrl(product.getImageUrl());
        } catch (Exception e) {
            throw new ApiException("Product with barcode '" + inventoryForm.getBarcode() + "' not found");
        }
        
        return inventoryPojo;
    }

    @Override
    protected InventoryData convertEntityToData(InventoryPojo inventoryPojo) {
        InventoryData inventoryData = new InventoryData();
        inventoryData.setId(inventoryPojo.getId());
        inventoryData.setProductName(inventoryPojo.getProductName());
        inventoryData.setBarcode(inventoryPojo.getProductBarcode());
        
        // Try to get product ID, but don't fail if product not found
        // This avoids N+1 queries and handles cases where products might be deleted
        try {
            var product = productApi.getByBarcode(inventoryPojo.getProductBarcode());
            inventoryData.setProductId(product != null ? product.getId() : null);
        } catch (Exception e) {
            // If product not found, set to null - this is acceptable for denormalized data
            inventoryData.setProductId(null);
        }
        
        inventoryData.setQuantity(inventoryPojo.getQuantity());
        inventoryData.setMrp(inventoryPojo.getProductMrp());
        inventoryData.setImageUrl(inventoryPojo.getProductImageUrl());
        return inventoryData;
    }

    @Override
    protected void preprocess(InventoryForm inventoryForm) {
        // Validate barcode is provided
        if (inventoryForm.getBarcode() == null || inventoryForm.getBarcode().trim().isEmpty()) {
            throw new ApiException("Product barcode is required");
        }
        // Validate base64 image if provided
        if (inventoryForm.getImage() != null && !inventoryForm.getImage().trim().isEmpty()) {
            if (!isValidBase64(inventoryForm.getImage())) {
                throw new ApiException("Image must be a valid base64 string");
            }
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public InventoryData add(@Valid InventoryForm form) {
        if (form == null) {
            throw new ApiException("Inventory form cannot be null");
        }
        return super.add(form);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public InventoryData update(Integer id, @Valid InventoryForm form) {
        if (form == null) {
            throw new ApiException("Inventory form cannot be null");
        }
        return super.update(id, form);
    }

    // Custom methods for stock management
    @org.springframework.transaction.annotation.Transactional
    public InventoryData addStock(String barcode, Integer quantity) {
        validateBarcode(barcode);
        validateQuantity(quantity, "Quantity to add");
        inventoryFlow.addStock(barcode, quantity);
        return getByProductBarcode(barcode);
    }

    @org.springframework.transaction.annotation.Transactional
    public InventoryData removeStock(String barcode, Integer quantity) {
        validateBarcode(barcode);
        validateQuantity(quantity, "Quantity to remove");
        inventoryFlow.removeStock(barcode, quantity);
        return getByProductBarcode(barcode);
    }

    @org.springframework.transaction.annotation.Transactional
    public InventoryData setStock(String barcode, Integer quantity) {
        validateBarcode(barcode);
        validateSetStockQuantity(quantity);
        inventoryFlow.setStock(barcode, quantity);
        return getByProductBarcode(barcode);
    }

    public InventoryData getByProductBarcode(String barcode) {
        validateBarcode(barcode);
        return convertEntityToData(inventoryFlow.getByProductBarcode(barcode));
    }

    public InventoryData getByProductName(String productName) {
        validateProductName(productName);
        return convertEntityToData(inventoryFlow.getByProductName(productName));
    }

    @Override
    public List<InventoryData> getAll() {
        List<InventoryPojo> entities = api.getAll();
        List<InventoryData> dataList = new ArrayList<>();

        // Get all products in one query to avoid N+1 queries
        List<org.example.pojo.ProductPojo> allProducts = productApi.getAll();
        java.util.Map<String, Integer> barcodeToProductIdMap = new java.util.HashMap<>();

        // Create a map of barcode to product ID for efficient lookup
        for (org.example.pojo.ProductPojo product : allProducts) {
            if (product.getBarcode() != null) {
                barcodeToProductIdMap.put(product.getBarcode(), product.getId());
            }
        }

        // Process all inventory items with efficient product ID lookup
        for (InventoryPojo entity : entities) {
            InventoryData inventoryData = new InventoryData();
            inventoryData.setId(entity.getId());
            inventoryData.setProductName(entity.getProductName());
            inventoryData.setBarcode(entity.getProductBarcode());
            inventoryData.setQuantity(entity.getQuantity());
            inventoryData.setMrp(entity.getProductMrp());
            inventoryData.setImageUrl(entity.getProductImageUrl());

            // Look up product ID from the map
            Integer productId = barcodeToProductIdMap.get(entity.getProductBarcode());
            inventoryData.setProductId(productId);

            dataList.add(inventoryData);
        }

        return dataList;
    }

    // Validation helpers
    private void validateBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Product barcode cannot be null or empty");
        }
    }
    private void validateQuantity(Integer quantity, String fieldName) {
        if (Objects.isNull(quantity)) {
            throw new ApiException(fieldName + " cannot be null");
        }
        if (quantity <= 0) {
            throw new ApiException(fieldName + " must be positive");
        }
    }
    private void validateSetStockQuantity(Integer quantity) {
        if (Objects.isNull(quantity)) {
            throw new ApiException("Stock quantity cannot be null");
        }
        if (quantity < 0) {
            throw new ApiException("Stock quantity cannot be negative");
        }
    }
    private void validateProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
    }
    private boolean isValidBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
