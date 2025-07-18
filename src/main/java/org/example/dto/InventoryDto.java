package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.form.InventoryForm;
import org.example.model.data.InventoryData;
import org.example.model.data.TsvUploadResult;
import org.example.pojo.InventoryPojo;
import org.example.flow.InventoryFlow;
import org.example.api.InventoryApi;
import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.util.StringUtil;
import org.example.util.Base64ToPdfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Base64;
import jakarta.validation.Valid;
import java.util.ArrayList;
import org.example.util.FileValidationUtil;
import org.example.util.InventoryTsvParser;

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

    public TsvUploadResult uploadInventoryFromTsv(MultipartFile file) {
        System.out.println("InventoryDto.uploadInventoryFromTsv - Starting");
        // Validate file
        FileValidationUtil.validateTsvFile(file);
        System.out.println("InventoryDto.uploadInventoryFromTsv - File validation passed");
        
        TsvUploadResult result;
        try {
            System.out.println("InventoryDto.uploadInventoryFromTsv - Starting parse with complete validation");
            result = InventoryTsvParser.parseWithCompleteValidation(file.getInputStream(), productApi);
            System.out.println("InventoryDto.uploadInventoryFromTsv - Parse completed. Total: " + result.getTotalRows() + ", Successful: " + result.getSuccessfulRows() + ", Failed: " + result.getFailedRows());
        } catch (Exception e) {
            System.out.println("InventoryDto.uploadInventoryFromTsv - Parse failed: " + e.getMessage());
            e.printStackTrace();
            result = new TsvUploadResult();
            result.addError("Failed to parse file: " + e.getMessage());
            return result;
        }
        
        // Check if we have any forms to process
        if (result.getSuccessfulRows() == 0) {
            System.out.println("InventoryDto.uploadInventoryFromTsv - No successful rows to process");
            return result;
        }
        
        // Validate file size
        try {
            FileValidationUtil.validateFileSize(result.getSuccessfulRows());
            System.out.println("InventoryDto.uploadInventoryFromTsv - File size validation passed");
        } catch (ApiException e) {
            System.out.println("InventoryDto.uploadInventoryFromTsv - File size validation failed: " + e.getMessage());
            result.addError("File size validation failed: " + e.getMessage());
            return result;
        }
        
        // Get the parsed forms from the result
        List<InventoryForm> forms = result.getParsedForms();
        if (forms == null || forms.isEmpty()) {
            System.out.println("InventoryDto.uploadInventoryFromTsv - No valid forms found to process");
            result.addError("No valid forms found to process");
            return result;
        }
        
        System.out.println("InventoryDto.uploadInventoryFromTsv - Processing " + forms.size() + " forms");
        
        // Reset counters for actual processing
        result.setSuccessfulRows(0);
        
        // Process only the valid forms (already validated by parser)
        for (InventoryForm form : forms) {
            try {
                System.out.println("InventoryDto.uploadInventoryFromTsv - Processing inventory: " + form.getBarcode());
                
                // Check if inventory already exists for this product
                InventoryPojo existingInventory = null;
                try {
                    existingInventory = inventoryFlow.getByProductBarcode(form.getBarcode());
                } catch (Exception e) {
                    // Inventory doesn't exist, which is fine - we'll create a new one
                    System.out.println("InventoryDto.uploadInventoryFromTsv - No existing inventory found for: " + form.getBarcode());
                }
                
                if (existingInventory != null) {
                    // Update existing inventory by adding the new quantity
                    System.out.println("InventoryDto.uploadInventoryFromTsv - Updating existing inventory for: " + form.getBarcode() + " (current: " + existingInventory.getQuantity() + ", adding: " + form.getQuantity() + ")");
                    inventoryFlow.addStock(form.getBarcode(), form.getQuantity());
                    result.addWarning("Updated existing inventory for product '" + form.getBarcode() + "' (added " + form.getQuantity() + " to existing stock)");
                } else {
                    // Create new inventory record
                    System.out.println("InventoryDto.uploadInventoryFromTsv - Creating new inventory for: " + form.getBarcode());
                    InventoryPojo entity = convertFormToEntity(form);
                    inventoryFlow.add(entity);
                }
                
                result.incrementSuccessful();
                System.out.println("InventoryDto.uploadInventoryFromTsv - Successfully processed inventory: " + form.getBarcode());
            } catch (Exception e) {
                System.out.println("InventoryDto.uploadInventoryFromTsv - Unexpected error processing inventory '" + form.getBarcode() + "': " + e.getMessage());
                result.addError("Unexpected error processing inventory '" + form.getBarcode() + "': " + e.getMessage());
                result.incrementFailed();
            }
        }
        
        System.out.println("InventoryDto.uploadInventoryFromTsv - Final result: " + result.getSummary());
        return result;
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
