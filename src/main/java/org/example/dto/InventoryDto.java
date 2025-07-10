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

    public InventoryData getByProductBarcode(String barcode) {
        validateBarcode(barcode);
        return convert(inventoryFlow.getByProductBarcode(barcode));
    }

    public InventoryData getByProductName(String productName) {
        validateProductName(productName);
        return convert(inventoryFlow.getByProductName(productName));
    }

    public void delete(Integer id) {
        validateInventoryId(id);
        inventoryFlow.delete(id);
    }
    
    public InventoryData addStock(String barcode, Integer quantity) {
        validateBarcode(barcode);
        validateQuantity(quantity, "Quantity to add");
        
        inventoryFlow.addStock(barcode, quantity);
        return getByProductBarcode(barcode);
    }
    
    public InventoryData removeStock(String barcode, Integer quantity) {
        validateBarcode(barcode);
        validateQuantity(quantity, "Quantity to remove");
        
        inventoryFlow.removeStock(barcode, quantity);
        return getByProductBarcode(barcode);
    }
    
    public InventoryData setStock(String barcode, Integer quantity) {
        validateBarcode(barcode);
        validateSetStockQuantity(quantity);
        
        inventoryFlow.setStock(barcode, quantity);
        return getByProductBarcode(barcode);
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
        // Validate barcode is provided
        if (inventoryForm.getBarcode() == null || inventoryForm.getBarcode().trim().isEmpty()) {
            throw new ApiException("Product barcode is required");
        }
        // Get product details to populate inventory fields
        try {
            var product = productApi.getByBarcode(inventoryForm.getBarcode());
            inventoryForm.setProductName(product.getName());
            inventoryForm.setClientName(product.getClientName());
            inventoryForm.setMrp(product.getMrp());
            inventoryForm.setImageUrl(product.getImageUrl());
        } catch (Exception e) {
            throw new ApiException("Product with barcode '" + inventoryForm.getBarcode() + "' not found");
        }
        // Validate base64 image if provided
        if (inventoryForm.getImage() != null && !inventoryForm.getImage().trim().isEmpty()) {
            if (!isValidBase64(inventoryForm.getImage())) {
                throw new ApiException("Image must be a valid base64 string");
            }
        }
    }

    private InventoryPojo convert(InventoryForm inventoryForm) {
        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProductBarcode(inventoryForm.getBarcode());
        inventoryPojo.setProductName(inventoryForm.getProductName());
        inventoryPojo.setClientName(inventoryForm.getClientName());
        inventoryPojo.setProductMrp(inventoryForm.getMrp());
        inventoryPojo.setProductImageUrl(inventoryForm.getImageUrl());
        inventoryPojo.setQuantity(inventoryForm.getQuantity());
        return inventoryPojo;
    }

    private InventoryData convert(InventoryPojo inventoryPojo) {
        InventoryData inventoryData = new InventoryData();
        inventoryData.setId(inventoryPojo.getId());
        inventoryData.setProductName(inventoryPojo.getProductName());
        inventoryData.setBarcode(inventoryPojo.getProductBarcode());
        inventoryData.setProductId(null); // No longer have product ID reference
        inventoryData.setQuantity(inventoryPojo.getQuantity());
        inventoryData.setMrp(inventoryPojo.getProductMrp());
        inventoryData.setImageUrl(inventoryPojo.getProductImageUrl());
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
