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
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.Objects;
import jakarta.validation.Valid;

@Component
public class ProductDto {

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ClientApi clientApi;

    public ProductData add(@Valid ProductForm productForm) {
        // Expect clientName, not clientId
        if (productForm.getClientName() == null || productForm.getClientName().trim().isEmpty()) {
            throw new ApiException("Client name is required");
        }
        preprocess(productForm);
        ProductPojo productPojo = productFlow.add(convert(productForm));
        return convert(productPojo);
    }

    public ProductData get(int id) {
        return convert(productFlow.get(id));
    }

    public ProductData getByBarcode(String barcode) {
        return convert(productFlow.getByBarcode(barcode));
    }

    public ProductData getByName(String name) {
        return convert(productFlow.getByName(name));
    }

    public List<ProductData> getAll() {
        List<ProductPojo> productPojoList = productFlow.getAll();
        List<ProductData> productDataList = new ArrayList<>();
        for(ProductPojo productPojo : productPojoList) {
            productDataList.add(convert(productPojo));
        }
        return productDataList;
    }

    public ProductData update(int id, @Valid ProductForm productForm) {
        // Expect clientName, not clientId
        if (productForm.getClientName() == null || productForm.getClientName().trim().isEmpty()) {
            throw new ApiException("Client name is required");
        }
        preprocess(productForm);
        productFlow.update(id, convert(productForm));
        return get(id);
    }

    public void delete(int id) {
        productFlow.delete(id);
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

    private void preprocess(ProductForm productForm) {
        // Lookup clientId from clientName
        if (productForm.getClientName() == null || productForm.getClientName().trim().isEmpty()) {
            throw new ApiException("Client name is required");
        }
        productForm.setClientId(clientApi.getByName(productForm.getClientName()).getId());
        // Validate image URL if provided
        if (productForm.getImage() != null && !productForm.getImage().trim().isEmpty()) {
            String imageUrl = productForm.getImage().trim();
            if (!isValidUrl(imageUrl)) {
                throw new ApiException("Image must be a valid URL");
            }
        }
    }

    private ProductPojo convert(ProductForm productForm) {
        ProductPojo productPojo = new ProductPojo();
        productPojo.setClient(clientApi.get(productForm.getClientId()));
        productPojo.setName(productForm.getName());
        productPojo.setMrp(productForm.getMrp());
        productPojo.setBarcode(productForm.getBarcode());
        
        // Store image URL in imageUrl field
        productPojo.setImageUrl(productForm.getImage());
        
        return productPojo;
    }

    private ProductData convert(ProductPojo productPojo) {
        ProductData productData = new ProductData();
        productData.setId(productPojo.getId());
        productData.setName(productPojo.getName());
        productData.setClientName(productPojo.getClient().getClientName());
        productData.setClientId(productPojo.getClient().getId());
        productData.setBarcode(productPojo.getBarcode());
        productData.setMrp(productPojo.getMrp());
        
        // Set imageUrl directly from stored URL
        if (productPojo.getImageUrl() != null && !productPojo.getImageUrl().trim().isEmpty()) {
            productData.setImageUrl(productPojo.getImageUrl());
        }
        
        return productData;
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