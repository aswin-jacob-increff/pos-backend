package org.example.dto;

import org.example.model.ProductForm;
import org.example.model.ProductData;
import org.example.pojo.ProductPojo;
import org.example.flow.ProductFlow;
import org.example.api.ClientApi;
import org.example.exception.ApiException;
import org.example.util.FileValidationUtil;
import org.example.util.ProductTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
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
        productPojo.setClientName(productForm.getClientName());
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
        productData.setClientId(null); // No longer have client ID reference
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
        // Validate clientName is provided
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
    public ProductData update(Integer id, @Valid ProductForm form) {
        preprocess(form);
        ProductPojo updatedProduct = convertFormToEntity(form);
        ProductPojo result = productFlow.update(id, updatedProduct);
        return convertEntityToData(result);
    }

    // Custom methods that don't fit the generic pattern
    public ProductData getByBarcode(String barcode) {
        return convertEntityToData(productFlow.getByBarcode(barcode));
    }

    public ProductData getByName(String name) {
        return convertEntityToData(productFlow.getByName(name));
    }

    public void deleteByName(String name) {
        productFlow.deleteByName(name);
    }

    public void deleteByBarcode(String barcode) {
        productFlow.deleteByBarcode(barcode);
    }

    public void deleteProduct(Integer id, String name, String barcode) {
        if (id != null) {
            delete(id);
        } else if (name != null) {
            deleteByName(name);
        } else if (barcode != null) {
            deleteByBarcode(barcode);
        } else {
            throw new ApiException("You must provide either 'id', 'name', or 'barcode' to delete a product.");
        }
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
    
    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }
}