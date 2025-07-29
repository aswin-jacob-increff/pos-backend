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
        List<ProductPojo> results = getByParams("barcode", barcode);
        return results.isEmpty() ? null : results.get(0);
    }

    public ProductPojo selectByName(String name) {
        List<ProductPojo> results = getByParams("name", name);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<ProductPojo> selectByClientId(Integer clientId) {
        return getByParams(new String[]{"clientId"}, new Object[]{clientId});
    }

    public boolean hasProductsByClientId(Integer clientId) {
        return existsByField("clientId", clientId);
    }

    public PaginationResponse<ProductPojo> selectByClientIdPaginated(Integer clientId, PaginationRequest request) {
        return getByParamsPaginated("clientId", clientId, request);
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
