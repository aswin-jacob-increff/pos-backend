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
        
        // Get client ID from client name
        String clientName = StringUtil.format(productForm.getClientName());
        Integer clientId = getClientIdByName(clientName);
        if (clientId == null) {
            throw new ApiException("Client with name '" + clientName + "' not found");
        }
        productPojo.setClientId(clientId);
        
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
        
        // Get client information from clientId
        Integer clientId = productPojo.getClientId();
        String clientName = null;
        
        if (clientId != null && clientId > 0) {
            // Always use clientApi to get client name to avoid lazy loading issues
            try {
                ClientPojo client = clientApi.get(clientId);
                if (client != null) {
                    clientName = client.getClientName();
                }
            } catch (Exception e) {
                // Client not found or invalid, continue with null clientName
                System.out.println("Warning: Client with ID " + clientId + " not found for product " + productPojo.getId());
            }
        }
        
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

    public List<ProductData> getByBarcodeLike(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        List<ProductPojo> products = productFlow.getByBarcodeLike(barcode);
        return products.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    public ProductData getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
        ProductPojo product = productFlow.getByName(name);
        if (product == null) {
            throw new ApiException("Product with name '" + name + "' not found");
        }
        return convertEntityToData(product);
    }

    public List<ProductData> getByNameLike(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Product name cannot be null or empty");
        }
        List<ProductPojo> products = productFlow.getByNameLike(name);
        return products.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }
    
    /**
     * Get products by client ID
     */
    public List<ProductData> getByClientId(Integer clientId) {
        if (clientId == null) {
            throw new ApiException("Client ID cannot be null");
        }
        
        return productFlow.getAll().stream()
                .filter(product -> clientId.equals(product.getClientId()))
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }
    
    /**
     * Get products by client name
     */
    public List<ProductData> getByClientName(String clientName) {
        if (clientName == null || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        
        // Get client ID from client name
        Integer clientId = getClientIdByName(clientName);
        if (clientId == null) {
            throw new ApiException("Client with name '" + clientName + "' not found");
        }
        
        return getByClientId(clientId);
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
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all products with pagination support.
     */
    public org.example.model.data.PaginationResponse<ProductData> getAllPaginated(org.example.model.form.PaginationRequest request) {
        org.example.model.data.PaginationResponse<ProductPojo> paginatedEntities = productFlow.getAllPaginated(request);
        
        List<ProductData> dataList = paginatedEntities.getContent().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
        
        return new org.example.model.data.PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
    }

    /**
     * Get products by name with partial matching and pagination support.
     */
    public org.example.model.data.PaginationResponse<ProductData> getByNameLikePaginated(String name, org.example.model.form.PaginationRequest request) {
        org.example.model.data.PaginationResponse<ProductPojo> paginatedEntities = productFlow.getByNameLikePaginated(name, request);
        
        List<ProductData> dataList = paginatedEntities.getContent().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
        
        return new org.example.model.data.PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
    }

    /**
     * Get products by client ID with pagination support.
     */
    public org.example.model.data.PaginationResponse<ProductData> getByClientIdPaginated(Integer clientId, org.example.model.form.PaginationRequest request) {
        org.example.model.data.PaginationResponse<ProductPojo> paginatedEntities = productFlow.getByClientIdPaginated(clientId, request);
        
        List<ProductData> dataList = paginatedEntities.getContent().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
        
        return new org.example.model.data.PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
    }

    /**
     * Get products by client name with pagination support.
     */
    public org.example.model.data.PaginationResponse<ProductData> getByClientNamePaginated(String clientName, org.example.model.form.PaginationRequest request) {
        // Get client ID from client name first
        Integer clientId = getClientIdByName(clientName);
        if (clientId == null) {
            throw new ApiException("Client with name '" + clientName + "' not found");
        }
        
        return getByClientIdPaginated(clientId, request);
    }
}