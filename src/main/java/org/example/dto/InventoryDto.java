package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.data.InventoryData;
import org.example.model.form.InventoryForm;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
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
        
        // Handle product ID conversion
        if (form.getProductId() != null) {
            // If productId is provided directly, use it
            pojo.setProductId(form.getProductId());
        } else if (form.getBarcode() != null && !form.getBarcode().trim().isEmpty()) {
            // If barcode is provided, convert it to productId
                var product = productApi.getByBarcode(form.getBarcode().trim().toLowerCase());
                if (product != null) {
                    pojo.setProductId(product.getId());
                } else {
                    throw new ApiException("Product not found with barcode: " + form.getBarcode());
                }

        } else {
            throw new ApiException("Either productId or barcode must be provided");
        }
        
        pojo.setQuantity(form.getQuantity());
        return pojo;
    }

    @Override
    protected InventoryData convertEntityToData(InventoryPojo inventoryPojo) {
        if (inventoryPojo == null) {
            return null;
        }
        
        InventoryData inventoryData = new InventoryData();
        inventoryData.setId(inventoryPojo.getId());
        inventoryData.setProductId(inventoryPojo.getProductId());
        inventoryData.setQuantity(inventoryPojo.getQuantity());
        
        // Get product details if productId is present
        if (inventoryPojo.getProductId() != null && inventoryPojo.getProductId() > 0) {
            try {
                var product = productApi.get(inventoryPojo.getProductId());
                inventoryData.setProductName(product.getName());
                inventoryData.setBarcode(product.getBarcode());
                inventoryData.setMrp(product.getMrp());
            } catch (Exception e) {
                inventoryData.setProductName("Unknown");
                inventoryData.setBarcode("Unknown");
                inventoryData.setMrp(0.0);
            }
        }
        
        return inventoryData;
    }

    @Override
    protected void preprocess(InventoryForm inventoryForm) {
        if (inventoryForm == null) {
            throw new ApiException("Inventory form cannot be null");
        }
        if ((inventoryForm.getProductId() == null || inventoryForm.getProductId() <= 0) && 
            (inventoryForm.getBarcode() == null || inventoryForm.getBarcode().trim().isEmpty())) {
            throw new ApiException("Either Product ID or Barcode is required");
        }
        if (inventoryForm.getQuantity() == null || inventoryForm.getQuantity() < 0) {
            throw new ApiException("Quantity must be non-negative");
        }
    }

    // ========== CUSTOM METHODS ==========

    public InventoryData getByProductId(Integer productId) {
        return getByField("productId", productId);
    }

    public PaginationResponse<InventoryData> getByProductIdPaginated(Integer productId, PaginationRequest request) {
        return getByFieldPaginated("productId", productId, request);
    }

    public void updateQuantity(Integer productId, Integer newQuantity) {
        if (productId == null || productId <= 0) {
            throw new ApiException("Product ID must be positive");
        }
        if (newQuantity == null || newQuantity < 0) {
            throw new ApiException("Quantity must be non-negative");
        }

        InventoryPojo inventory = ((org.example.api.InventoryApi) api).getByProductId(productId);
        if (inventory == null) {
            throw new ApiException("Inventory not found for product ID: " + productId);
        }

        inventory.setQuantity(newQuantity);
        api.update(inventory.getId(), inventory);
    }

    public void addStock(Integer productId, Integer quantityToAdd) {
        if (productId == null || productId <= 0) {
            throw new ApiException("Product ID must be positive");
        }
        if (quantityToAdd == null || quantityToAdd <= 0) {
            throw new ApiException("Quantity to add must be positive");
        }

        InventoryPojo inventory = ((org.example.api.InventoryApi) api).getByProductId(productId);
        if (inventory == null) {
            throw new ApiException("Inventory not found for product ID: " + productId);
        }

        inventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        api.update(inventory.getId(), inventory);
    }

    public void removeStock(Integer productId, Integer quantityToRemove) {
        if (productId == null || productId <= 0) {
            throw new ApiException("Product ID must be positive");
        }
        if (quantityToRemove == null || quantityToRemove <= 0) {
            throw new ApiException("Quantity to remove must be positive");
        }

        InventoryPojo inventory = ((org.example.api.InventoryApi) api).getByProductId(productId);
        if (inventory == null) {
            throw new ApiException("Inventory not found for product ID: " + productId);
        }

        if (inventory.getQuantity() < quantityToRemove) {
            throw new ApiException("Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: " + quantityToRemove);
        }

        inventory.setQuantity(inventory.getQuantity() - quantityToRemove);
        api.update(inventory.getId(), inventory);
    }

    // ========== COMPATIBILITY METHODS FOR TESTS ==========

    public InventoryData addStockAndReturn(Integer productId, Integer quantity) {
        addStock(productId, quantity);
        return getByProductId(productId);
    }

    public InventoryData removeStockAndReturn(Integer productId, Integer quantity) {
        removeStock(productId, quantity);
        return getByProductId(productId);
    }

    public InventoryData setStock(Integer productId, Integer quantity) {
        updateQuantity(productId, quantity);
        return getByProductId(productId);
    }

    public TsvUploadResult uploadInventoryFromTsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("File cannot be null or empty");
        }

        FileValidationUtil.validateTsvFile(file);

        try {
            TsvUploadResult result = InventoryTsvParser.parseWithDuplicateDetection(file.getInputStream());
            
            // Process the parsed forms
            List<InventoryForm> forms = result.getParsedForms();
            if (forms != null) {
                for (InventoryForm form : forms) {
                    try {
                        add(form);
                        result.incrementSuccessful();
                    } catch (Exception e) {
                        result.addError("Failed to add inventory for product ID '" + form.getProductId() + "': " + e.getMessage());
                        result.incrementFailed();
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            throw new ApiException("Failed to process TSV file: " + e.getMessage());
        }
    }
}
