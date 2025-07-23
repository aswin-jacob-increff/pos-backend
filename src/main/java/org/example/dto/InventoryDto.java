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

    @Autowired
    private ClientApi clientApi;

    @Override
    protected String getEntityName() {
        return "Inventory";
    }

    @Override
    protected InventoryPojo convertFormToEntity(InventoryForm inventoryForm) {
        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setQuantity(inventoryForm.getQuantity());
        
        // Get product ID from barcode
        try {
            var product = productApi.getByBarcode(inventoryForm.getBarcode());
            inventoryPojo.setProductId(product.getId());
        } catch (Exception e) {
            throw new ApiException("Product with barcode '" + inventoryForm.getBarcode() + "' not found");
        }
        
        return inventoryPojo;
    }

    @Override
    protected InventoryData convertEntityToData(InventoryPojo inventoryPojo) {
        InventoryData inventoryData = new InventoryData();
        inventoryData.setId(inventoryPojo.getId());
        inventoryData.setProductId(inventoryPojo.getProductId());
        inventoryData.setQuantity(inventoryPojo.getQuantity());
        
        // Get product details from productId
        try {
            var product = productApi.get(inventoryPojo.getProductId());
            inventoryData.setProductName(product.getName());
            inventoryData.setBarcode(product.getBarcode());
            inventoryData.setMrp(product.getMrp());
            inventoryData.setImageUrl(product.getImageUrl());
            

        } catch (Exception e) {
            // If product not found, set fields to null
            inventoryData.setProductName(null);
            inventoryData.setBarcode(null);
            inventoryData.setMrp(null);
            inventoryData.setImageUrl(null);
        }
        
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
    public InventoryData addStock(Integer productId, Integer quantity) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        validateQuantity(quantity, "Quantity to add");
        inventoryFlow.addStock(productId, quantity);
        return getByProductId(productId);
    }

    @org.springframework.transaction.annotation.Transactional
    public InventoryData removeStock(Integer productId, Integer quantity) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        validateQuantity(quantity, "Quantity to remove");
        inventoryFlow.removeStock(productId, quantity);
        return getByProductId(productId);
    }

    @org.springframework.transaction.annotation.Transactional
    public InventoryData setStock(Integer productId, Integer quantity) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        validateSetStockQuantity(quantity);
        inventoryFlow.setStock(productId, quantity);
        return getByProductId(productId);
    }

    public InventoryData getByProductId(Integer productId) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        return convertEntityToData(inventoryFlow.getByProductId(productId));
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
            inventoryData.setProductId(entity.getProductId());
            inventoryData.setQuantity(entity.getQuantity());

            // Get product details from productId
            try {
                var product = productApi.get(entity.getProductId());
                inventoryData.setProductName(product.getName());
                inventoryData.setBarcode(product.getBarcode());
                inventoryData.setMrp(product.getMrp());
                inventoryData.setImageUrl(product.getImageUrl());
            } catch (Exception e) {
                // If product not found, set fields to null
                inventoryData.setProductName(null);
                inventoryData.setBarcode(null);
                inventoryData.setMrp(null);
                inventoryData.setImageUrl(null);
            }

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
                
                // Get product ID from barcode
                Integer productId = null;
                try {
                    var product = productApi.getByBarcode(form.getBarcode());
                    productId = product.getId();
                } catch (Exception e) {
                    throw new ApiException("Product with barcode '" + form.getBarcode() + "' not found");
                }
                
                // Check if inventory already exists for this product
                InventoryPojo existingInventory = null;
                try {
                    existingInventory = inventoryFlow.getByProductId(productId);
                } catch (Exception e) {
                    // Inventory doesn't exist, which is fine - we'll create a new one
                    System.out.println("InventoryDto.uploadInventoryFromTsv - No existing inventory found for product ID: " + productId);
                }
                
                if (existingInventory != null) {
                    // Update existing inventory by adding the new quantity
                    System.out.println("InventoryDto.uploadInventoryFromTsv - Updating existing inventory for product ID: " + productId + " (current: " + existingInventory.getQuantity() + ", adding: " + form.getQuantity() + ")");
                    inventoryFlow.addStock(productId, form.getQuantity());
                    result.addWarning("Updated existing inventory for product '" + form.getBarcode() + "' (added " + form.getQuantity() + " to existing stock)");
                } else {
                    // Create new inventory record
                    System.out.println("InventoryDto.uploadInventoryFromTsv - Creating new inventory for product ID: " + productId);
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
        } catch (Exception e) {
            return false;
        }
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all inventory items with pagination support.
     */
    public org.example.model.data.PaginationResponse<InventoryData> getAllPaginated(org.example.model.form.PaginationRequest request) {
        org.example.model.data.PaginationResponse<InventoryPojo> paginatedEntities = inventoryFlow.getAllPaginated(request);
        
        // Get all products in one query to avoid N+1 queries
        List<org.example.pojo.ProductPojo> allProducts = productApi.getAll();
        java.util.Map<String, Integer> barcodeToProductIdMap = new java.util.HashMap<>();

        // Create a map of barcode to product ID for efficient lookup
        for (org.example.pojo.ProductPojo product : allProducts) {
            if (product.getBarcode() != null) {
                barcodeToProductIdMap.put(product.getBarcode(), product.getId());
            }
        }

        // Convert entities to data with efficient product ID lookup
        List<InventoryData> dataList = paginatedEntities.getContent().stream()
                .map(entity -> {
                    InventoryData inventoryData = new InventoryData();
                    inventoryData.setId(entity.getId());
                    inventoryData.setProductId(entity.getProductId());
                    inventoryData.setQuantity(entity.getQuantity());

                    // Get product details from productId
                    try {
                        var product = productApi.get(entity.getProductId());
                        inventoryData.setProductName(product.getName());
                        inventoryData.setBarcode(product.getBarcode());
                        inventoryData.setMrp(product.getMrp());
                        inventoryData.setImageUrl(product.getImageUrl());
                    } catch (Exception e) {
                        // If product not found, set fields to null
                        inventoryData.setProductName(null);
                        inventoryData.setBarcode(null);
                        inventoryData.setMrp(null);
                        inventoryData.setImageUrl(null);
                    }

                    return inventoryData;
                })
                .collect(Collectors.toList());
        
        return new org.example.model.data.PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
    }

    /**
     * Get inventory by product ID with pagination support.
     */
    public org.example.model.data.PaginationResponse<InventoryData> getByProductIdPaginated(Integer productId, org.example.model.form.PaginationRequest request) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        
        // Get the specific inventory for the product ID
        InventoryData inventory = getByProductId(productId);
        
        List<InventoryData> paginatedContent;
        if (inventory != null) {
            paginatedContent = List.of(inventory);
        } else {
            paginatedContent = List.of();
        }
        
        return new org.example.model.data.PaginationResponse<>(
            paginatedContent,
            paginatedContent.size(),
            request.getPageNumber(),
            request.getPageSize()
        );
    }
}
