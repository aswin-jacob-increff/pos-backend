package org.example.dao;

import org.springframework.stereotype.Repository;
import org.example.pojo.ProductPojo;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import java.util.List;

@Repository
public class ProductDao extends AbstractDao<ProductPojo> {

    public ProductDao() {
        super(ProductPojo.class);
    }

    public ProductPojo selectByBarcode(String barcode) {
        return selectByField("barcode", barcode);
    }

    public List<ProductPojo> selectByBarcodeLike(String barcode) {
        return selectByFieldLike("barcode", barcode);
    }

    public ProductPojo selectByName(String name) {
        return selectByField("name", name);
    }

    public List<ProductPojo> selectByNameLike(String name) {
        return selectByFieldLike("name", name);
    }

    public List<ProductPojo> selectByClientId(Integer clientId) {
        return selectByFields(new String[]{"clientId"}, new Object[]{clientId});
    }

    public boolean hasProductsByClientId(Integer clientId) {
        List<ProductPojo> products = selectByClientId(clientId);
        return products != null && !products.isEmpty();
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all products with pagination support.
     */
    public PaginationResponse<ProductPojo> getAllPaginated(PaginationRequest request) {
        return selectAllPaginated(request);
    }

    /**
     * Get products by barcode with pagination support.
     */
    public PaginationResponse<ProductPojo> selectByBarcodePaginated(String barcode, PaginationRequest request) {
        return selectByFieldPaginated("barcode", barcode, request);
    }

    /**
     * Get products by barcode with partial matching and pagination support.
     */
    public PaginationResponse<ProductPojo> selectByBarcodeLikePaginated(String barcode, PaginationRequest request) {
        return selectByFieldLikePaginated("barcode", barcode, request);
    }

    /**
     * Get products by name with pagination support.
     */
    public PaginationResponse<ProductPojo> selectByNamePaginated(String name, PaginationRequest request) {
        return selectByFieldPaginated("name", name, request);
    }

    /**
     * Get products by name with partial matching and pagination support.
     */
    public PaginationResponse<ProductPojo> selectByNameLikePaginated(String name, PaginationRequest request) {
        return selectByFieldLikePaginated("name", name, request);
    }

    /**
     * Get products by client ID with pagination support.
     */
    public PaginationResponse<ProductPojo> selectByClientIdPaginated(Integer clientId, PaginationRequest request) {
        return selectByFieldPaginated("clientId", clientId, request);
    }

    // ========== COUNT METHODS ==========

    /**
     * Count products by barcode.
     */
    public long countByBarcode(String barcode) {
        return countByField("barcode", barcode);
    }

    /**
     * Count products by barcode with partial matching.
     */
    public long countByBarcodeLike(String barcode) {
        return countByFieldLike("barcode", barcode);
    }

    /**
     * Count products by name.
     */
    public long countByName(String name) {
        return countByField("name", name);
    }

    /**
     * Count products by name with partial matching.
     */
    public long countByNameLike(String name) {
        return countByFieldLike("name", name);
    }

    /**
     * Count products by client ID.
     */
    public long countByClientId(Integer clientId) {
        return countByField("clientId", clientId);
    }

    @Override
    protected void updateEntity(ProductPojo existing, ProductPojo updated) {
        existing.setName(updated.getName());
        existing.setBarcode(updated.getBarcode());
        existing.setMrp(updated.getMrp());
        existing.setImageUrl(updated.getImageUrl());
        existing.setClientId(updated.getClientId());
    }
}
