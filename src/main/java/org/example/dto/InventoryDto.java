package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.data.InventoryData;
import org.example.model.form.InventoryForm;
import org.example.model.data.TsvUploadResult;
import org.example.pojo.InventoryPojo;
import org.example.api.ProductApi;
import org.example.util.FileValidationUtil;
import org.example.util.InventoryTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;

@Component
public class InventoryDto extends AbstractDto<InventoryPojo, InventoryForm, InventoryData> {

    @Autowired
    private ProductApi productApi;

    @Override
    protected String getEntityName() {
        return "Inventory";
    }

    @Override
    protected InventoryPojo convertFormToEntity(InventoryForm form) {
        InventoryPojo pojo = new InventoryPojo();
        pojo.setProductId(form.getProductId());
        pojo.setQuantity(form.getQuantity());
        return pojo;
    }

    @Override
    protected InventoryData convertEntityToData(InventoryPojo inventoryPojo) {
        InventoryData inventoryData = new InventoryData();
        inventoryData.setId(inventoryPojo.getId());
        inventoryData.setProductId(inventoryPojo.getProductId());
        inventoryData.setQuantity(inventoryPojo.getQuantity());
        
        // Get product information if productId is present
        if (inventoryPojo.getProductId() != null && inventoryPojo.getProductId() > 0) {
            try {
                var product = productApi.get(inventoryPojo.getProductId());
                inventoryData.setProductName(product.getName());
                inventoryData.setBarcode(product.getBarcode());
            } catch (Exception e) {
                inventoryData.setProductName("Unknown");
                inventoryData.setBarcode("Unknown");
            }
        }
        
        return inventoryData;
    }

    @Override
    protected void preprocess(InventoryForm inventoryForm) {
        if (inventoryForm == null) {
            throw new ApiException("Inventory form cannot be null");
        }
        if (inventoryForm.getProductId() == null) {
            throw new ApiException("Product ID is required");
        }
        if (inventoryForm.getQuantity() == null || inventoryForm.getQuantity() < 0) {
            throw new ApiException("Quantity must be non-negative");
        }
    }

    // Custom methods that don't fit the generic pattern

    public InventoryData getByProductId(Integer productId) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        InventoryPojo inventory = ((org.example.api.InventoryApi) api).getByProductId(productId);
        if (inventory == null) {
            throw new ApiException("No inventory found for product ID: " + productId);
        }
        return convertEntityToData(inventory);
    }

    public InventoryData addStock(Integer productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ApiException("Quantity must be positive");
        }
        ((org.example.api.InventoryApi) api).addStock(productId, quantity);
        return getByProductId(productId);
    }

    public InventoryData removeStock(Integer productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ApiException("Quantity must be positive");
        }
        ((org.example.api.InventoryApi) api).removeStock(productId, quantity);
        return getByProductId(productId);
    }

    public InventoryData setStock(Integer productId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new ApiException("Quantity must be non-negative");
        }
        ((org.example.api.InventoryApi) api).setStock(productId, quantity);
        return getByProductId(productId);
    }



    public PaginationResponse<InventoryData> getByProductIdPaginated(Integer productId, PaginationRequest request) {
        PaginationResponse<InventoryPojo> paginatedEntities = ((org.example.api.InventoryApi) api).getByProductIdPaginated(productId, request);
        
        List<InventoryData> dataList = paginatedEntities.getContent().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
        
        return new PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
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
                System.out.println("InventoryDto.uploadInventoryFromTsv - Adding inventory for product: " + form.getProductId());
                // Use the flow directly since validation is already done
                InventoryPojo entity = convertFormToEntity(form);
                api.add(entity);
                result.incrementSuccessful();
                System.out.println("InventoryDto.uploadInventoryFromTsv - Successfully added inventory for product: " + form.getProductId());
            } catch (Exception e) {
                System.out.println("InventoryDto.uploadInventoryFromTsv - Unexpected error adding inventory for product '" + form.getProductId() + "': " + e.getMessage());
                result.addError("Unexpected error adding inventory for product '" + form.getProductId() + "': " + e.getMessage());
                result.incrementFailed();
            }
        }
        
        System.out.println("InventoryDto.uploadInventoryFromTsv - Final result: " + result.getSummary());
        return result;
    }
}
