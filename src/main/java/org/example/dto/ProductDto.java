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
        
        // Handle client ID conversion
        if (form.getClientId() != null) {
            // If clientId is provided directly, use it
            pojo.setClientId(form.getClientId());
        } else if (form.getClientName() != null && !form.getClientName().trim().isEmpty()) {
            // If clientName is provided, convert it to clientId
            try {
                var client = clientApi.getByName(form.getClientName().trim().toLowerCase());
                if (client != null) {
                    pojo.setClientId(client.getId());
                } else {
                    throw new ApiException("Client not found with name: " + form.getClientName());
                }
            } catch (Exception e) {
                throw new ApiException("Failed to find client with name '" + form.getClientName() + "': " + e.getMessage());
            }
        } else {
            throw new ApiException("Either clientId or clientName must be provided");
        }
        
        return pojo;
    }

    @Override
    protected ProductData convertEntityToData(ProductPojo productPojo) {
        if (productPojo == null) {
            return null;
        }
        
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

    // ========== CUSTOM METHODS ==========

    public ProductData getByBarcode(String barcode) {
        return getByField("barcode", barcode);
    }

    public List<ProductData> getByClientName(String clientName) {
        validateFieldValue("clientName", clientName);
        List<ProductPojo> products = ((org.example.api.ProductApi) api).getByClientName(clientName);
        return convertEntitiesToData(products);
    }

    public List<ProductData> getByBarcodeLike(String barcode) {
        return getByFieldLike("barcode", barcode);
    }

    public List<ProductData> getByNameLike(String name) {
        return getByFieldLike("name", name);
    }

    public List<ProductData> getByClientId(Integer clientId) {
        return getByFields(new String[]{"clientId"}, new Object[]{clientId});
    }

    public PaginationResponse<ProductData> getByNameLikePaginated(String name, PaginationRequest request) {
        return getByFieldLikePaginated("name", name, request);
    }

    public PaginationResponse<ProductData> getByClientIdPaginated(Integer clientId, PaginationRequest request) {
        return getByFieldPaginated("clientId", clientId, request);
    }

    public PaginationResponse<ProductData> getByClientNamePaginated(String clientName, PaginationRequest request) {
        validateFieldValue("clientName", clientName);
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
        validateId(productId);
        ProductPojo product = api.get(productId);
        return product.getImageUrl();
    }

    public TsvUploadResult uploadProductsFromTsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("File cannot be null or empty");
        }

        FileValidationUtil.validateTsvFile(file);

        try {
            TsvUploadResult result = ProductTsvParser.parseWithDuplicateDetection(file.getInputStream());
            
            // Process the parsed forms
            List<ProductForm> forms = result.getParsedForms();
            if (forms != null) {
                for (ProductForm form : forms) {
                    try {
                        add(form);
                        result.incrementSuccessful();
                    } catch (Exception e) {
                        result.addError("Failed to add product '" + form.getName() + "': " + e.getMessage());
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