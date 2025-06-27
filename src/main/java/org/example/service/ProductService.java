package org.example.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import org.example.dao.ProductDao;
import org.example.pojo.ProductPojo;
import org.example.dao.InventoryDao;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private InventoryDao inventoryDao;

    public ProductPojo add(ProductPojo productPojo) {
        productDao.insert(productPojo);
        return productPojo;
    }

    public ProductPojo get(Integer id) {
        return productDao.select(id);
    }

    public ProductPojo getByName(String name) {
        return productDao.selectByName(name);
    }

    public List<ProductPojo> getAll() {
        return productDao.selectAll();
    }

    public ProductPojo getByBarcode(String barcode) {
        return productDao.selectByBarcode(barcode);
    }

    public void update(Integer id, ProductPojo updatedProduct) {
        productDao.update(id, updatedProduct);
    }

    public void delete(Integer id) {
        productDao.delete(id);
    }
}
