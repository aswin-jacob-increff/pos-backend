package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.form.ProductForm;
import org.example.model.data.ProductData;
import org.example.model.data.TsvUploadResult;
import org.example.pojo.ProductPojo;
import org.example.flow.ProductFlow;
import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.util.StringUtil;
import org.example.util.FileValidationUtil;
import org.example.util.ProductTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.example.pojo.ClientPojo;
import jakarta.validation.Valid;

@Component
public class ProductDto extends AbstractDto<ProductPojo, ProductForm, ProductData> {

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ClientApi clientApi;

    @Override
    protected String getEntityName() {
        return "Product";
    }

    @Override
    protected ProductPojo convertFormToEntity(ProductForm productForm) {
        ProductPojo productPojo = new ProductPojo();
        productPojo.setClientName(StringUtil.format(productForm.getClientName()));
        productPojo.setName(productForm.getName());
        productPojo.setMrp(productForm.getMrp());
        productPojo.setBarcode(productForm.getBarcode());
        
        // Store image URL in imageUrl field
        productPojo.setImageUrl(productForm.getImage());
        
        return productPojo;
    }

    @Override
    protected ProductData convertEntityToData(ProductPojo productPojo) {
        ProductData productData = new ProductData();
        productData.setId(productPojo.getId());
        productData.setName(productPojo.getName());
        
        // Debug client ID lookup
        String clientName = productPojo.getClientName();
        Integer clientId = getClientIdByName(clientName);
        System.out.println("Product " + productPojo.getId() + " - Client name: '" + clientName + "', Client ID: " + clientId);
        
        productData.setClientId(clientId);
        productData.setClientName(clientName);
        productData.setBarcode(productPojo.getBarcode());
        productData.setMrp(productPojo.getMrp());
        
        // Set imageUrl directly from stored URL
        if (productPojo.getImageUrl() != null && !productPojo.getImageUrl().trim().isEmpty()) {
            productData.setImageUrl(productPojo.getImageUrl());
        }
        
        return productData;
    }

    @Override
    protected void preprocess(ProductForm productForm) {
        if (productForm == null) {
            throw new ApiException("Product form cannot be null");
        }
        if (productForm.getClientName() == null || productForm.getClientName().trim().isEmpty()) {
            throw new ApiException("Client name is required");
        }
        // Validate image URL if provided
        if (productForm.getImage() != null && !productForm.getImage().trim().isEmpty()) {
            String imageUrl = productForm.getImage().trim();
            if (!isValidUrl(imageUrl)) {
                throw new ApiException("Image must be a valid URL");
            }
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ProductData add(@Valid ProductForm form) {
        if (Objects.isNull(form)) {
            throw new ApiException("Product form cannot be null");
        }
        return super.add(form);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ProductData update(Integer id, @Valid ProductForm form) {
        validateId(id);
        preprocess(form);
        ProductPojo entity = convertFormToEntity(form);
        
        // Use the flow instead of the API to ensure inventory is updated
        productFlow.update(id, entity);
        
        return convertEntityToData(api.get(id));
    }

    @Override
    public List<ProductData> getAll() {
        return productFlow.getAll().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    public TsvUploadResult uploadProductsFromTsv(MultipartFile file) {
        System.out.println("ProductDto.uploadProductsFromTsv - Starting");
        // Validate file
        FileValidationUtil.validateTsvFile(file);
        System.out.println("ProductDto.uploadProductsFromTsv - File validation passed");
        
        TsvUploadResult result;
        try {
            System.out.println("ProductDto.uploadProductsFromTsv - Starting parse with complete validation");
            result = ProductTsvParser.parseWithCompleteValidation(file.getInputStream(), (ProductApi) api, clientApi);
            System.out.println("ProductDto.uploadProductsFromTsv - Parse completed. Total: " + result.getTotalRows() + ", Successful: " + result.getSuccessfulRows() + ", Failed: " + result.getFailedRows());
        } catch (Exception e) {
            System.out.println("ProductDto.uploadProductsFromTsv - Parse failed: " + e.getMessage());
            e.printStackTrace();
            result = new TsvUploadResult();
            result.addError("Failed to parse file: " + e.getMessage());
            return result;
        }
        
        // Check if we have any forms to process
        if (result.getSuccessfulRows() == 0) {
            System.out.println("ProductDto.uploadProductsFromTsv - No successful rows to process");
            return result;
        }
        
        // Validate file size
        try {
            FileValidationUtil.validateFileSize(result.getSuccessfulRows());
            System.out.println("ProductDto.uploadProductsFromTsv - File size validation passed");
        } catch (ApiException e) {
            System.out.println("ProductDto.uploadProductsFromTsv - File size validation failed: " + e.getMessage());
            result.addError("File size validation failed: " + e.getMessage());
            return result;
        }
        
        // Get the parsed forms from the result
        List<ProductForm> forms = result.getParsedForms();
        if (forms == null || forms.isEmpty()) {
            System.out.println("ProductDto.uploadProductsFromTsv - No valid forms found to process");
            result.addError("No valid forms found to process");
            return result;
        }
        
        System.out.println("ProductDto.uploadProductsFromTsv - Processing " + forms.size() + " forms");
        
        // Reset counters for actual processing
        result.setSuccessfulRows(0);
        
        // Process only the valid forms (already validated by parser)
        for (ProductForm form : forms) {
            try {
                System.out.println("ProductDto.uploadProductsFromTsv - Adding product: " + form.getBarcode());
                // Use the flow directly since validation is already done
                ProductPojo entity = convertFormToEntity(form);
                productFlow.add(entity);
                result.incrementSuccessful();
                System.out.println("ProductDto.uploadProductsFromTsv - Successfully added product: " + form.getBarcode());
            } catch (Exception e) {
                System.out.println("ProductDto.uploadProductsFromTsv - Unexpected error adding product '" + form.getBarcode() + "': " + e.getMessage());
                result.addError("Unexpected error adding product '" + form.getBarcode() + "': " + e.getMessage());
                result.incrementFailed();
            }
        }
        
        System.out.println("ProductDto.uploadProductsFromTsv - Final result: " + result.getSummary());
        return result;
    }



    public String getProductImageUrl(Integer id) {
        ProductData product = get(id);
        if (product.getImageUrl() == null || product.getImageUrl().trim().isEmpty()) {
            throw new ApiException("Product image not found");
        }
        return product.getImageUrl();
    }

    public ProductData getByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        ProductPojo product = productFlow.getByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Product with barcode '" + barcode + "' not found");
        }
        return convertEntityToData(product);
    }
    
    /**
     * Get products by client ID
     */
    public List<ProductData> getByClientId(Integer clientId) {
        if (clientId == null) {
            throw new ApiException("Client ID cannot be null");
        }
        
        try {
            // Get client by ID first to get the client name
            ClientPojo client = clientApi.get(clientId);
            return getByClientName(client.getClientName());
        } catch (Exception e) {
            throw new ApiException("Client with ID " + clientId + " not found");
        }
    }
    
    /**
     * Get products by client name
     */
    public List<ProductData> getByClientName(String clientName) {
        if (clientName == null || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        
        return productFlow.getAll().stream()
                .filter(product -> clientName.equalsIgnoreCase(product.getClientName()))
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }
    
    /**
     * Helper method to get client ID by client name
     */
    private Integer getClientIdByName(String clientName) {
        if (clientName == null || clientName.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Format the client name consistently
            String formattedClientName = StringUtil.format(clientName);
            ClientPojo client = clientApi.getByName(formattedClientName);
            if (client != null) {
                return client.getId();
            }
            
            // If not found with formatted name, try with original name
            client = clientApi.getByName(clientName);
            return client != null ? client.getId() : null;
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error getting client ID for name '" + clientName + "': " + e.getMessage());
            return null;
        }
    }
    
    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }
}