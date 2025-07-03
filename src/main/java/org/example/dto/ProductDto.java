package org.example.dto;

import org.example.model.ProductForm;
import org.example.model.ProductData;
import org.example.pojo.ProductPojo;
import org.example.flow.ProductFlow;
import org.example.service.ClientService;
import org.example.exception.ApiException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

@Component
public class ProductDto {

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ClientService clientService;

    public ProductData add(ProductForm productForm) {
        validate(productForm);
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

    public ProductData update(int id, ProductForm productForm) {
        validate(productForm);
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

    private void validate(ProductForm productForm) {
        if(productForm.getBarcode() == null || productForm.getBarcode().trim().isEmpty()) {
            throw new ApiException("Product barcode cannot be empty");
        } else if (productForm.getName() == null || productForm.getName().trim().isEmpty()) {
            throw new ApiException("Product name cannot be empty");
        } else if (productForm.getMrp() == null) {
            throw new ApiException("Product mrp cannot be empty");
        } else if (productForm.getClientId() == null) {
            if (productForm.getClientName() == null || productForm.getClientName().trim().isEmpty()) {
                throw new ApiException("Both client id and name cannot be null");
            }
            else {
                productForm.setClientId(clientService.getByName(productForm.getClientName()).getId());
            }
        }
        else {
            productForm.setClientName(clientService.get(productForm.getClientId()).getClientName());
        }
        
        // Validate image
        if (productForm.getImage() == null || productForm.getImage().trim().isEmpty()) {
            throw new ApiException("Product image is required");
        }
        
        // Validate base64 format
        if (!isValidBase64(productForm.getImage())) {
            throw new ApiException("Image must be a valid base64 string");
        }
    }

    private ProductPojo convert(ProductForm productForm) {
        ProductPojo productPojo = new ProductPojo();
        productPojo.setClient(clientService.get(productForm.getClientId()));
        productPojo.setName(productForm.getName());
        productPojo.setMrp(productForm.getMrp());
        productPojo.setBarcode(productForm.getBarcode());
        
        // Store base64 image string in imageUrl field
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
        
        // Set imageUrl as reference to image endpoint
        if (productPojo.getImageUrl() != null && !productPojo.getImageUrl().trim().isEmpty()) {
            productData.setImageUrl("/api/products/" + productPojo.getId() + "/image");
        }
        
        return productData;
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