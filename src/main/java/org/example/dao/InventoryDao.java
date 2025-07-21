package org.example.dao;

import org.springframework.stereotype.Repository;
import org.example.pojo.InventoryPojo;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import java.util.List;

@Repository
public class InventoryDao extends AbstractDao<InventoryPojo> {
    public InventoryDao() {
        super(InventoryPojo.class);
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        return selectByField("productBarcode", barcode);
    }

    public List<InventoryPojo> getByProductBarcodeLike(String barcode) {
        return selectByFieldLike("productBarcode", barcode);
    }

    public InventoryPojo getByProductName(String name) {
        return selectByField("productName", name.trim().toLowerCase());
    }

    public List<InventoryPojo> getByProductNameLike(String name) {
        return selectByFieldLike("productName", name);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all inventory items with pagination support.
     */
    public PaginationResponse<InventoryPojo> getAllPaginated(PaginationRequest request) {
        return selectAllPaginated(request);
    }

    /**
     * Get inventory by product barcode with pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductBarcodePaginated(String barcode, PaginationRequest request) {
        return selectByFieldPaginated("productBarcode", barcode, request);
    }

    /**
     * Get inventory by product barcode with partial matching and pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductBarcodeLikePaginated(String barcode, PaginationRequest request) {
        return selectByFieldLikePaginated("productBarcode", barcode, request);
    }

    /**
     * Get inventory by product name with pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductNamePaginated(String name, PaginationRequest request) {
        return selectByFieldPaginated("productName", name.trim().toLowerCase(), request);
    }

    /**
     * Get inventory by product name with partial matching and pagination support.
     */
    public PaginationResponse<InventoryPojo> getByProductNameLikePaginated(String name, PaginationRequest request) {
        return selectByFieldLikePaginated("productName", name, request);
    }

    // ========== COUNT METHODS ==========

    /**
     * Count inventory items by product barcode.
     */
    public long countByProductBarcode(String barcode) {
        return countByField("productBarcode", barcode);
    }

    /**
     * Count inventory items by product barcode with partial matching.
     */
    public long countByProductBarcodeLike(String barcode) {
        return countByFieldLike("productBarcode", barcode);
    }

    /**
     * Count inventory items by product name.
     */
    public long countByProductName(String name) {
        return countByField("productName", name.trim().toLowerCase());
    }

    /**
     * Count inventory items by product name with partial matching.
     */
    public long countByProductNameLike(String name) {
        return countByFieldLike("productName", name);
    }

    @Override
    protected void updateEntity(InventoryPojo existing, InventoryPojo updated) {
        existing.setQuantity(updated.getQuantity());
        existing.setProductBarcode(updated.getProductBarcode());
        existing.setProductName(updated.getProductName());
        existing.setClientName(updated.getClientName());
        existing.setProductMrp(updated.getProductMrp());
        existing.setProductImageUrl(updated.getProductImageUrl());
    }
}
