package org.example.dto;

import org.example.model.InventoryForm;
import org.example.model.InventoryData;
import org.example.pojo.InventoryPojo;
import org.example.flow.InventoryFlow;
import org.example.api.ProductApi;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.Objects;
import jakarta.validation.Valid;

@Component
public class InventoryDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    @Autowired
    private ProductApi productApi;

    public InventoryData add(@Valid InventoryForm inventoryForm) {
        // Expect barcode, not productId
        if (inventoryForm.getBarcode() == null || inventoryForm.getBarcode().trim().isEmpty()) {
            throw new ApiException("Product barcode is required");
        }
        preprocess(inventoryForm);
        InventoryPojo inventoryPojo = convert(inventoryForm);
        inventoryFlow.add(inventoryPojo);
        return convert(inventoryPojo);
    }

    public InventoryData get(Integer id) {
        validateInventoryId(id);
        return convert(inventoryFlow.get(id));
    }

    public List<InventoryData> getAll() {
        List<InventoryPojo> inventoryPojoList = inventoryFlow.getAll();
        List<InventoryData> inventoryDataList = new ArrayList<>();
        for(InventoryPojo inventoryPojo : inventoryPojoList) {
            inventoryDataList.add(convert(inventoryPojo));
        }
        return inventoryDataList;
    }

    public InventoryData update(Integer id, @Valid InventoryForm inventoryForm) {
        // Expect barcode, not productId
        if (inventoryForm.getBarcode() == null || inventoryForm.getBarcode().trim().isEmpty()) {
            throw new ApiException("Product barcode is required");
        }
        preprocess(inventoryForm);
        inventoryFlow.update(id, convert(inventoryForm));
        return get(id);
    }

    public InventoryData getByProductId(Integer productId) {
        validateProductId(productId);
        return convert(inventoryFlow.getByProductId(productId));
    }

    public InventoryData getByProductName(String productName) {
        validateProductName(productName);
        return convert(inventoryFlow.getByProductName(productName));
    }

    public InventoryData getByProductBarcode(String barcode) {
        validateBarcode(barcode);
        return convert(inventoryFlow.getByProductBarcode(barcode));
    }

    public void delete(Integer id) {
        validateInventoryId(id);
        inventoryFlow.delete(id);
    }
    
    public InventoryData addStock(Integer productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity, "Quantity to add");
        
        inventoryFlow.addStock(productId, quantity);
        return getByProductId(productId);
    }
    
    public InventoryData removeStock(Integer productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity, "Quantity to remove");
        
        inventoryFlow.removeStock(productId, quantity);
        return getByProductId(productId);
    }
    
    public InventoryData setStock(Integer productId, Integer quantity) {
        validateProductId(productId);
        validateSetStockQuantity(quantity);
        
        inventoryFlow.setStock(productId, quantity);
        return getByProductId(productId);
    }
    
    private void validateInventoryId(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("Inventory ID cannot be null");
        }
        if (id <= 0) {
            throw new ApiException("Inventory ID must be positive");
        }
    }
    
    private void validateProductId(Integer productId) {
        if (Objects.isNull(productId)) {
            throw new ApiException("Product ID cannot be null");
        }
        if (productId <= 0) {
            throw new ApiException("Product ID must be positive");
        }
    }
    
    private void validateProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
    }
    
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

    private void preprocess(InventoryForm inventoryForm) {
        // Lookup productId from barcode
        if (inventoryForm.getBarcode() == null || inventoryForm.getBarcode().trim().isEmpty()) {
            throw new ApiException("Product barcode is required");
        }
        inventoryForm.setProductId(productApi.getByBarcode(inventoryForm.getBarcode()).getId());
        // Validate base64 image if provided
        if (inventoryForm.getImage() != null && !inventoryForm.getImage().trim().isEmpty()) {
            if (!isValidBase64(inventoryForm.getImage())) {
                throw new ApiException("Image must be a valid base64 string");
            }
        }
    }

    private InventoryPojo convert(InventoryForm inventoryForm) {
        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProduct(productApi.get(inventoryForm.getProductId()));
        inventoryPojo.setQuantity(inventoryForm.getQuantity());
        // No mrp field in InventoryPojo anymore
        // Handle base64 image if provided (this would update the product's image)
        if (inventoryForm.getImage() != null && !inventoryForm.getImage().trim().isEmpty()) {
            // For now, we'll just validate the image
            // In a real implementation, you might want to update the product's image
        }
        return inventoryPojo;
    }

    private InventoryData convert(InventoryPojo inventoryPojo) {
        InventoryData inventoryData = new InventoryData();
        inventoryData.setId(inventoryPojo.getId());
        inventoryData.setProductName(inventoryPojo.getProduct().getName());
        inventoryData.setBarcode(inventoryPojo.getProduct().getBarcode());
        inventoryData.setProductId(inventoryPojo.getProduct().getId());
        inventoryData.setQuantity(inventoryPojo.getQuantity());
        inventoryData.setMrp(inventoryPojo.getProduct().getMrp()); // Use product's mrp
        // Set imageUrl directly from product
        inventoryData.setImageUrl(inventoryPojo.getProduct().getImageUrl());
        return inventoryData;
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
