package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.data.ProductData;
import org.example.model.form.ProductForm;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.data.TsvUploadResult;
import org.example.pojo.ProductPojo;
import org.example.api.ClientApi;
import org.example.util.FileValidationUtil;
import org.example.util.ProductTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

@Component
public class ProductDto extends AbstractDto<ProductPojo, ProductForm, ProductData> {

    @Autowired
    private ClientApi clientApi;

    @Override
    protected String getEntityName() {
        return "Product";
    }

    @Override
    protected ProductPojo convertFormToEntity(ProductForm form) {
        ProductPojo pojo = new ProductPojo();
        pojo.setName(form.getName());
        pojo.setBarcode(form.getBarcode());
        pojo.setMrp(form.getMrp());
        pojo.setImageUrl(form.getImage());
        pojo.setClientId(form.getClientId());
        return pojo;
    }

    @Override
    protected ProductData convertEntityToData(ProductPojo productPojo) {
        ProductData productData = new ProductData();
        productData.setId(productPojo.getId());
        productData.setName(productPojo.getName());
        productData.setBarcode(productPojo.getBarcode());
        productData.setMrp(productPojo.getMrp());
        productData.setImageUrl(productPojo.getImageUrl());
        productData.setClientId(productPojo.getClientId());
        
        // Get client name if clientId is present
        if (productPojo.getClientId() != null && productPojo.getClientId() > 0) {
            try {
                var client = clientApi.get(productPojo.getClientId());
                productData.setClientName(client.getClientName());
            } catch (Exception e) {
                productData.setClientName("Unknown");
            }
        }
        
        return productData;
    }

    @Override
    protected void preprocess(ProductForm productForm) {
        if (productForm == null) {
            throw new ApiException("Product form cannot be null");
        }
        if (productForm.getName() == null || productForm.getName().trim().isEmpty()) {
            throw new ApiException("Product name is required");
        }
        if (productForm.getBarcode() == null || productForm.getBarcode().trim().isEmpty()) {
            throw new ApiException("Product barcode is required");
        }
        if (productForm.getMrp() == null || productForm.getMrp() <= 0) {
            throw new ApiException("Product MRP must be positive");
        }
    }

    // Custom methods that don't fit the generic pattern

    public ProductData getByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        ProductPojo product = ((org.example.api.ProductApi) api).getByBarcode(barcode);
        return convertEntityToData(product);
    }

    public List<ProductData> getByClientName(String clientName) {
        if (clientName == null || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        List<ProductPojo> products = ((org.example.api.ProductApi) api).getByClientName(clientName);
        return products.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    public List<ProductData> getByBarcodeLike(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new ApiException("Barcode cannot be null or empty");
        }
        List<ProductPojo> products = ((org.example.api.ProductApi) api).getByBarcodeLike(barcode);
        return products.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    public List<ProductData> getByNameLike(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Name cannot be null or empty");
        }
        List<ProductPojo> products = ((org.example.api.ProductApi) api).getByNameLike(name);
        return products.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    public List<ProductData> getByClientId(Integer clientId) {
        if (clientId == null) {
            throw new ApiException("Client ID cannot be null");
        }
        List<ProductPojo> products = ((org.example.api.ProductApi) api).getByClientId(clientId);
        return products.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }



    public PaginationResponse<ProductData> getByNameLikePaginated(String name, PaginationRequest request) {
        PaginationResponse<ProductPojo> paginatedEntities = ((org.example.api.ProductApi) api).getByNameLikePaginated(name, request);
        
        List<ProductData> dataList = paginatedEntities.getContent().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
        
        return new PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
    }

    public PaginationResponse<ProductData> getByClientIdPaginated(Integer clientId, PaginationRequest request) {
        PaginationResponse<ProductPojo> paginatedEntities = ((org.example.api.ProductApi) api).getByClientIdPaginated(clientId, request);
        
        List<ProductData> dataList = paginatedEntities.getContent().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
        
        return new PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
    }

    public PaginationResponse<ProductData> getByClientNamePaginated(String clientName, PaginationRequest request) {
        PaginationResponse<ProductPojo> paginatedEntities = ((org.example.api.ProductApi) api).getByClientNamePaginated(clientName, request);
        
        List<ProductData> dataList = paginatedEntities.getContent().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
        
        return new PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
    }

    public String getProductImageUrl(Integer productId) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        ProductPojo product = api.get(productId);
        return product.getImageUrl();
    }

    public TsvUploadResult uploadProductsFromTsv(MultipartFile file) {
        System.out.println("ProductDto.uploadProductsFromTsv - Starting");
        // Validate file
        FileValidationUtil.validateTsvFile(file);
        System.out.println("ProductDto.uploadProductsFromTsv - File validation passed");
        
        TsvUploadResult result;
        try {
            System.out.println("ProductDto.uploadProductsFromTsv - Starting parse with complete validation");
            result = ProductTsvParser.parseWithCompleteValidation(file.getInputStream(), (org.example.api.ProductApi) api, clientApi);
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
                System.out.println("ProductDto.uploadProductsFromTsv - Adding product: " + form.getName());
                // Use the flow directly since validation is already done
                ProductPojo entity = convertFormToEntity(form);
                api.add(entity);
                result.incrementSuccessful();
                System.out.println("ProductDto.uploadProductsFromTsv - Successfully added product: " + form.getName());
            } catch (Exception e) {
                System.out.println("ProductDto.uploadProductsFromTsv - Unexpected error adding product '" + form.getName() + "': " + e.getMessage());
                result.addError("Unexpected error adding product '" + form.getName() + "': " + e.getMessage());
                result.incrementFailed();
            }
        }
        
        System.out.println("ProductDto.uploadProductsFromTsv - Final result: " + result.getSummary());
        return result;
    }
}