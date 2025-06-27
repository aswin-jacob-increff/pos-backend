package org.example.dto;

import org.example.model.InventoryForm;
import org.example.model.InventoryData;
import org.example.pojo.InventoryPojo;
import org.example.flow.InventoryFlow;
import org.example.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    @Autowired
    private ProductService productService;

    public InventoryData add(InventoryForm inventoryForm) {
        validate(inventoryForm);
        InventoryPojo inventoryPojo = convert(inventoryForm);
        inventoryFlow.add(inventoryPojo);
        return convert(inventoryPojo);
    }

    public InventoryData get(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Inventory ID cannot be null");
        }
        return convert(inventoryFlow.get(id));
    }

    public List<InventoryData> getAll() {
        List<InventoryPojo> inventoryPojoList = inventoryFlow.getAll();
        List<InventoryData> inventoryDataList = new ArrayList<>();
        for(InventoryPojo inventoryPojo : inventoryPojoList) {
            inventoryDataList.add(convert(inventoryPojo));
        }
        return inventoryDataList;
    }

    public InventoryData update(Integer  id, InventoryForm inventoryForm) {
        if (id == null) {
            throw new IllegalArgumentException("Inventory ID cannot be null");
        }
        validate(inventoryForm);
        inventoryFlow.update(id, convert(inventoryForm));
        return get(id);
    }

    public InventoryData getByProductId(Integer productId) {
        return convert(inventoryFlow.getByProductId(productId));
    }

    public InventoryData getByProductName(String productName) {
        return convert(inventoryFlow.getByProductName(productName));
    }

    public InventoryData getByProductBarcode(String barcode) {
        return convert(inventoryFlow.getByProductBarcode(barcode));
    }

    public void delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Inventory ID cannot be null");
        }
        inventoryFlow.delete(id);
    }

    private void validate(InventoryForm inventoryForm) {
        if(inventoryForm.getProductId() == null) {
            if(inventoryForm.getProductBarcode().trim().isEmpty()) {
                if(inventoryForm.getProductName().trim().isEmpty()) {
                    throw new IllegalArgumentException("One of Id, name or barcode is relevant");
                }
                else {
                    inventoryForm.setProductId(productService.getByName(inventoryForm.getProductName()).getId());
                    inventoryForm.setProductBarcode(productService.getByName(inventoryForm.getProductName()).getBarcode());
                }
            }
            else {
                inventoryForm.setProductId(productService.getByBarcode(inventoryForm.getProductBarcode()).getId());
                inventoryForm.setProductName(productService.getByBarcode(inventoryForm.getProductBarcode()).getName());
            }
        }
        else {
            inventoryForm.setProductName(productService.get(inventoryForm.getProductId()).getName());
            inventoryForm.setProductBarcode(productService.get(inventoryForm.getProductId()).getBarcode());
        }
        inventoryForm.setClientId(productService.get(inventoryForm.getProductId()).getClient().getId());
        inventoryForm.setClientName(productService.get(inventoryForm.getProductId()).getClient().getClientName());
        if (inventoryForm.getQuantity() == null) {
            throw new RuntimeException("Please enter Quantity");
        }
    }

    private InventoryPojo convert(InventoryForm inventoryForm) {
        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProduct(productService.get(inventoryForm.getProductId()));
        inventoryPojo.setQuantity(inventoryForm.getQuantity());
        return inventoryPojo;
    }

    private InventoryData convert(InventoryPojo inventoryPojo) {
        InventoryData inventoryData = new InventoryData();
        inventoryData.setId(inventoryPojo.getId());
        inventoryData.setProductName(inventoryPojo.getProduct().getName());
        inventoryData.setProductBarcode(inventoryPojo.getProduct().getBarcode());
        inventoryData.setProductName(inventoryPojo.getProduct().getName());
        inventoryData.setClientId(inventoryPojo.getProduct().getClient().getId());
        inventoryData.setClientName(inventoryPojo.getProduct().getClient().getClientName());
        inventoryData.setQuantity(inventoryPojo.getQuantity());
        return inventoryData;
    }
}
