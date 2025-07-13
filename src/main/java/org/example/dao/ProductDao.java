package org.example.dao;

import org.springframework.stereotype.Repository;
import org.example.pojo.ProductPojo;
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

    public List<ProductPojo> selectByClientName(String clientName) {
        return selectByFields(new String[]{"clientName"}, new Object[]{clientName});
    }

    public boolean hasProductsByClientName(String clientName) {
        List<ProductPojo> products = selectByClientName(clientName);
        return products != null && !products.isEmpty();
    }

    @Override
    protected void updateEntity(ProductPojo existing, ProductPojo updated) {
        existing.setName(updated.getName());
        existing.setBarcode(updated.getBarcode());
        existing.setMrp(updated.getMrp());
        existing.setImageUrl(updated.getImageUrl());
        existing.setClientName(updated.getClientName());
    }
}
