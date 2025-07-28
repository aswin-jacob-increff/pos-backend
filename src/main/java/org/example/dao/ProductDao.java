package org.example.dao;

import org.springframework.stereotype.Repository;
import org.example.pojo.ProductPojo;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import java.util.List;

@Repository
public class ProductDao extends AbstractDao<ProductPojo> {

    public ProductDao() {
        super(ProductPojo.class);
    }

    public ProductPojo selectByBarcode(String barcode) {
        return selectByField("barcode", barcode);
    }

    public ProductPojo selectByName(String name) {
        return selectByField("name", name);
    }

    public List<ProductPojo> selectByClientId(Integer clientId) {
        return selectByFields(new String[]{"clientId"}, new Object[]{clientId});
    }

    public boolean hasProductsByClientId(Integer clientId) {
        List<ProductPojo> products = selectByClientId(clientId);
        return products != null && !products.isEmpty();
    }

    public PaginationResponse<ProductPojo> selectByClientIdPaginated(Integer clientId, PaginationRequest request) {
        return getPaginated(PaginationQuery.byField("clientId", clientId, request));
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
