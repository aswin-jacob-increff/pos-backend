package org.example.order.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.config.SpringConfig;
import org.example.model.*;
import org.example.dto.ClientDto;
import org.example.dto.InventoryDto;
import org.example.dto.OrderDto;
import org.example.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringConfig.class})
@WebAppConfiguration
@Transactional
public class OrderIntegrationTest {

    @Autowired
    private ClientDto clientDto;
    @Autowired
    private ProductDto productDto;
    @Autowired
    private InventoryDto inventoryDto;
    @Autowired
    private OrderDto orderDto;

    @Test
    void testAddOrder() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("OrderTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("OrderTestClient");
        productForm.setName("Order Test Product");
        productForm.setBarcode("ORDERPROD123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("ORDERPROD123");
        inventoryForm.setQuantity(50);
        inventoryDto.add(inventoryForm);

        // Create order item
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setBarcode("ORDERPROD123");
        orderItemForm.setQuantity(2);
        orderItemForm.setSellingPrice(90.0);

        // Create order
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setOrderItemFormList(Arrays.asList(orderItemForm));
        orderForm.setTotal(180.0);
        orderForm.setUserId("testuser@example.com");

        OrderData orderData = orderDto.add(orderForm);
        assert orderData.getId() != null;
        assert orderData.getTotal() == 200.0; // 2 items * 100.0 MRP
        assert orderData.getUserId().equals("testuser@example.com");
    }

    @Test
    void testGetOrder() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("GetOrderTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("GetOrderTestClient");
        productForm.setName("Get Order Test Product");
        productForm.setBarcode("GETORDER123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("GETORDER123");
        inventoryForm.setQuantity(50);
        inventoryDto.add(inventoryForm);

        // Create order item
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setBarcode("GETORDER123");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        // Create order
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setOrderItemFormList(Arrays.asList(orderItemForm));
        orderForm.setTotal(90.0);
        orderForm.setUserId("testuser@example.com");

        OrderData createdOrder = orderDto.add(orderForm);
        OrderData fetchedOrder = orderDto.get(createdOrder.getId());
        assert fetchedOrder.getId().equals(createdOrder.getId());
        assert fetchedOrder.getTotal() == 100.0; // 1 item * 100.0 MRP
        assert fetchedOrder.getUserId().equals("testuser@example.com");
    }

    @Test
    void testAddOrderWithInsufficientInventory() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("InsufficientInventoryClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("InsufficientInventoryClient");
        productForm.setName("Insufficient Inventory Product");
        productForm.setBarcode("INSUFFICIENT123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Add limited inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("INSUFFICIENT123");
        inventoryForm.setQuantity(5);
        inventoryDto.add(inventoryForm);

        // Try to create order with more quantity than available
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setBarcode("INSUFFICIENT123");
        orderItemForm.setQuantity(10); // More than available (5)
        orderItemForm.setSellingPrice(90.0);

        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setOrderItemFormList(Arrays.asList(orderItemForm));
        orderForm.setTotal(900.0);
        orderForm.setUserId("testuser@example.com");

        boolean failed = false;
        try {
            orderDto.add(orderForm);
        } catch (Exception e) {
            failed = true;
        }
        assert failed;
    }

    @Test
    void testAddOrderWithNonExistentProduct() {
        // Try to create order with non-existent product
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setBarcode("NONEXISTENT123");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setOrderItemFormList(Arrays.asList(orderItemForm));
        orderForm.setTotal(90.0);
        orderForm.setUserId("testuser@example.com");

        boolean failed = false;
        try {
            orderDto.add(orderForm);
        } catch (Exception e) {
            failed = true;
        }
        assert failed;
    }

    @Test
    void testGetNonExistentOrder() {
        boolean failed = false;
        try {
            orderDto.get(99999);
        } catch (Exception e) {
            failed = true;
        }
        assert failed;
    }

    @Test
    void testUpdateNonExistentOrder() {
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setBarcode("UPDATE123");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setOrderItemFormList(Arrays.asList(orderItemForm));
        orderForm.setTotal(90.0);
        orderForm.setUserId("testuser@example.com");

        boolean failed = false;
        try {
            orderDto.update(99999, orderForm);
        } catch (Exception e) {
            failed = true;
        }
        assert failed;
    }

    @Test
    void testGetOrdersByDateRange() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("DateRangeTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("DateRangeTestClient");
        productForm.setName("Date Range Test Product");
        productForm.setBarcode("DATERANGE123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("DATERANGE123");
        inventoryForm.setQuantity(50);
        inventoryDto.add(inventoryForm);

        // Create order item
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setBarcode("DATERANGE123");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        // Create order
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setOrderItemFormList(Arrays.asList(orderItemForm));
        orderForm.setTotal(90.0);
        orderForm.setUserId("testuser@example.com");

        orderDto.add(orderForm);

        // Get orders by date range
        java.time.LocalDate today = java.time.LocalDate.now();
        var orders = orderDto.getOrdersByDateRange(today, today);
        assert orders.size() > 0;
        boolean found = orders.stream().anyMatch(o -> o.getTotal() == 100.0 && o.getUserId().equals("testuser@example.com"));
        assert found;
    }

    @Test
    void testGetOrdersByUserId() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("UserOrdersTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("UserOrdersTestClient");
        productForm.setName("User Orders Test Product");
        productForm.setBarcode("USERORDERS123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("USERORDERS123");
        inventoryForm.setQuantity(50);
        inventoryDto.add(inventoryForm);

        // Create order item
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setBarcode("USERORDERS123");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        // Create order
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setOrderItemFormList(Arrays.asList(orderItemForm));
        orderForm.setTotal(90.0);
        orderForm.setUserId("specificuser@example.com");

        orderDto.add(orderForm);

        // Get orders by user ID
        var orders = orderDto.getOrdersByUserId("specificuser@example.com");
        assert orders.size() > 0;
        assert orders.get(0).getUserId().equals("specificuser@example.com");
    }
} 