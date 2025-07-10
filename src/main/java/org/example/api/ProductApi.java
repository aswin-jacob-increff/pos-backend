package org.example.api;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.dao.ProductDao;
import org.example.pojo.ProductPojo;
import org.example.dao.InventoryDao;

@Service
public class ProductApi extends AbstractApi<ProductPojo> {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private InventoryDao inventoryDao;

    @Override
    protected String getEntityName() {
        return "Product";
    }

    public ProductPojo getByName(String name) {
        return getByField("name", name);
    }

    public ProductPojo getByBarcode(String barcode) {
        return getByField("barcode", barcode);
    }
} 