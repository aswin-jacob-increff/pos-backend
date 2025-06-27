package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.model.InventoryData;
import org.example.model.InventoryForm;
import org.example.dto.InventoryDto;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @PostMapping
    public InventoryData add(@RequestBody InventoryForm form) {
        return inventoryDto.add(form);
    }

    @GetMapping("/{id}")
    public InventoryData get(@PathVariable Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Inventory ID cannot be null");
        }
        return inventoryDto.get(id);
    }

    @GetMapping
    public List<InventoryData> getAll() {
        return inventoryDto.getAll();
    }

    @GetMapping("/byProduct")
    public InventoryData getInventoryByAny(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String name
    ) {
        InventoryData inventoryData = new InventoryData();

        if (id != null) {
            inventoryData = inventoryDto.getByProductId(id);
        } else if (barcode != null && !barcode.trim().isEmpty()) {
            inventoryData = inventoryDto.getByProductBarcode(barcode.trim());
        } else if (name != null && !name.trim().isEmpty()) {
            inventoryData = inventoryDto.getByProductName(name.trim());
        } else {
            throw new RuntimeException("Requires either of product name, product id or product barcode");
        }

        if (inventoryData == null) {
            throw new RuntimeException("Inventory not found for the provided input.");
        }

        return inventoryData;
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Integer id, @RequestBody InventoryForm form) {
        if (id == null) {
            throw new IllegalArgumentException("Inventory ID cannot be null");
        }
        inventoryDto.update(id, form);
    }
}
