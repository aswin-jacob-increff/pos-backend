package org.example.dto;

import org.example.model.ProductForm;
import org.example.model.ProductData;
import org.example.pojo.ProductPojo;
import org.example.flow.ProductFlow;
import org.example.api.ClientApi;
import org.example.exception.ApiException;
import org.example.util.FileValidationUtil;
import org.example.util.ProductTsvParser;
import org.example.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.stream.Collectors;
import org.example.pojo.ClientPojo;

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
        productData.setClientName(productPojo.getClientName());
        
        // Look up client ID by name
        productData.setClientId(getClientIdByName(productPojo.getClientName()));
        
        productData.setBarcode(productPojo.getBarcode());
        productData.setMrp(productPojo.getMrp());
        
        // Set imageUrl directly from stored URL
        if (productPojo.getImageUrl() != null && !productPojo.getImageUrl().trim().isEmpty()) {
            productData.setImageUrl(productPojo.getImageUrl());
        }
        
        return productData;
    }

    protected ProductPojo convertDataToEntity(ProductData productData) {
        ProductPojo productPojo = new ProductPojo();
        productPojo.setId(productData.getId());
        productPojo.setName(productData.getName());
        productPojo.setClientName(productData.getClientName());
        productPojo.setBarcode(productData.getBarcode());
        productPojo.setMrp(productData.getMrp());
        productPojo.setImageUrl(productData.getImageUrl());
        return productPojo;
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
    public List<ProductData> getAll() {
        return productFlow.getAll().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    public String uploadProductsFromTsv(MultipartFile file) {
        // Validate file
        FileValidationUtil.validateTsvFile(file);
        try {
            List<ProductForm> productForms = ProductTsvParser.parse(file.getInputStream());
            FileValidationUtil.validateFileSize(productForms.size());
            // Only add if all are valid
            int count = 0;
            for (ProductForm form : productForms) {
                add(form);
                count++;
            }
            return "Successfully uploaded " + count + " products.";
        } catch (ApiException e) {
            // Propagate parser validation errors
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Error while processing file: " + e.getMessage());
        }
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
            ClientPojo client = clientApi.getByName(clientName);
            return client.getId();
        } catch (Exception e) {
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