package org.example.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.exception.ApiException;

import java.util.List;
import org.example.dao.ProductDao;
import org.example.pojo.ProductPojo;
import org.example.dao.InventoryDao;

@Service
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
        ProductPojo product = productDao.select(id);
        if (product == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }
        return product;
    }

    public ProductPojo getByName(String name) {
        ProductPojo product = productDao.selectByName(name);
        if (product == null) {
            throw new ApiException("Product with name '" + name + "' not found");
        }
        return product;
    }

    public List<ProductPojo> getAll() {
        return productDao.selectAll();
    }

    public ProductPojo getByBarcode(String barcode) {
        ProductPojo product = productDao.selectByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Product with barcode '" + barcode + "' not found");
        }
        return product;
    }

    public void update(Integer id, ProductPojo updatedProduct) {
        ProductPojo existingProduct = productDao.select(id);
        if (existingProduct == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }
        productDao.update(id, updatedProduct);
    }

    public void delete(Integer id) {
        ProductPojo product = productDao.select(id);
        if (product == null) {
            throw new ApiException("Product with ID " + id + " not found");
        }
        productDao.delete(id);
    }
}
