package org.example.flow;

import org.example.pojo.InventoryPojo;
import org.example.pojo.ProductPojo;
import org.example.service.ProductService;
import org.example.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductFlow {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    public ProductPojo add(ProductPojo productPojo) {
        productPojo = productService.add(productPojo);
        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProduct(productPojo);
        inventoryPojo.setQuantity(0);
        inventoryService.add(inventoryPojo);
        return productPojo;
    }

    public ProductPojo get(Integer id) {
        return productService.get(id);
    }

    public ProductPojo getByBarcode(String barcode) {
        return productService.getByBarcode(barcode);
    }

    public ProductPojo getByName(String name) {
        return productService.getByName(name);
    }

    public List<ProductPojo> getAll() {
        return productService.getAll();
    }

    public ProductPojo update(Integer id, ProductPojo productPojo) {
        productService.update(id, productPojo);
        InventoryPojo inventoryPojo = inventoryService.getByProductId(id);
        inventoryPojo.setProduct(productPojo);
        inventoryService.update(inventoryPojo.getId(), inventoryPojo);
        return productService.get(id);
    }

    public void delete(Integer id) {
        InventoryPojo inventoryPojo = inventoryService.getByProductId(id);
        inventoryService.delete(inventoryPojo.getId());
        productService.delete(id);
    }

    public void deleteByName(String name) {
        ProductPojo productPojo = productService.getByName(name);
        InventoryPojo inventoryPojo = inventoryService.getByProductId(productPojo.getId());
        inventoryService.delete(inventoryPojo.getId());
        productService.delete(productPojo.getId());
    }

    public void deleteByBarcode(String barcode) {
        ProductPojo productPojo = productService.getByBarcode(barcode);
        InventoryPojo inventoryPojo = inventoryService.getByProductId(productPojo.getId());
        inventoryService.delete(inventoryPojo.getId());
        productService.delete(productPojo.getId());
    }

}