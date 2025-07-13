package org.example.orderitem.integration;

import org.example.dto.ClientDto;
import org.example.dto.ProductDto;
import org.example.dto.InventoryDto;
import org.example.dto.OrderDto;
import org.example.dto.OrderItemDto;
import org.example.model.ClientForm;
import org.example.model.ProductForm;
import org.example.model.InventoryForm;
import org.example.model.OrderForm;
import org.example.model.OrderItemForm;
import org.example.model.OrderItemData;
import org.example.model.OrderData;
import org.example.model.ProductData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {org.example.config.SpringConfig.class})
@WebAppConfiguration
@Transactional
public class OrderItemIntegrationTest {

    @Autowired
    private ClientDto clientDto;
    @Autowired
    private ProductDto productDto;
    @Autowired
    private InventoryDto inventoryDto;
    @Autowired
    private OrderDto orderDto;
    @Autowired
    private OrderItemDto orderItemDto;

    @Test
    void testAddOrderItem() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("OrderItemTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("OrderItemTestClient");
        productForm.setName("Order Item Test Product");
        productForm.setBarcode("ORDERITEM123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("ORDERITEM123");
        inventoryForm.setQuantity(50);
        inventoryDto.add(inventoryForm);

        // Create an order first
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setUserId("testuser@example.com");
        OrderData orderData = orderDto.add(orderForm);

        // Create order item
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setOrderId(orderData.getId());
        orderItemForm.setBarcode("ORDERITEM123");
        orderItemForm.setQuantity(2);
        orderItemForm.setSellingPrice(90.0);

        OrderItemData orderItemData = orderItemDto.add(orderItemForm);
        assertNotNull(orderItemData.getId());
        assertEquals(200.0, orderItemData.getAmount()); // 2 items * 100.0 MRP
        assertEquals("ORDERITEM123", orderItemData.getBarcode());
        assertEquals("Order Item Test Product", orderItemData.getProductName());
    }

    @Test
    void testGetOrderItem() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("GetOrderItemTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("GetOrderItemTestClient");
        productForm.setName("Get Order Item Test Product");
        productForm.setBarcode("GETORDERITEM123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("GETORDERITEM123");
        inventoryForm.setQuantity(50);
        inventoryDto.add(inventoryForm);

        // Create an order first
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setUserId("testuser@example.com");
        OrderData orderData = orderDto.add(orderForm);

        // Create order item
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setOrderId(orderData.getId());
        orderItemForm.setBarcode("GETORDERITEM123");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        OrderItemData createdOrderItem = orderItemDto.add(orderItemForm);
        OrderItemData fetchedOrderItem = orderItemDto.get(createdOrderItem.getId());
        
        assertEquals(createdOrderItem.getId(), fetchedOrderItem.getId());
        assertEquals(100.0, fetchedOrderItem.getAmount()); // 1 item * 100.0 MRP
        assertEquals("GETORDERITEM123", fetchedOrderItem.getBarcode());
    }

    @Test
    void testGetOrderItemsByOrderId() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("GetByOrderIdTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("GetByOrderIdTestClient");
        productForm.setName("Get By Order ID Test Product");
        productForm.setBarcode("GETBYORDERID123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("GETBYORDERID123");
        inventoryForm.setQuantity(50);
        inventoryDto.add(inventoryForm);

        // Create an order first
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setUserId("testuser@example.com");
        OrderData orderData = orderDto.add(orderForm);

        // Create multiple order items
        OrderItemForm orderItemForm1 = new OrderItemForm();
        orderItemForm1.setOrderId(orderData.getId());
        orderItemForm1.setBarcode("GETBYORDERID123");
        orderItemForm1.setQuantity(1);
        orderItemForm1.setSellingPrice(90.0);
        orderItemDto.add(orderItemForm1);

        OrderItemForm orderItemForm2 = new OrderItemForm();
        orderItemForm2.setOrderId(orderData.getId());
        orderItemForm2.setBarcode("GETBYORDERID123");
        orderItemForm2.setQuantity(2);
        orderItemForm2.setSellingPrice(90.0);
        orderItemDto.add(orderItemForm2);

        // Get order items by order ID
        List<OrderItemData> orderItems = orderItemDto.getByOrderId(orderData.getId());
        assertEquals(2, orderItems.size());
        
        // Check that both items have the correct order ID
        for (OrderItemData item : orderItems) {
            assertEquals(orderData.getId(), item.getOrderId());
            assertEquals("GETBYORDERID123", item.getBarcode());
        }
    }

    @Test
    void testAddOrderItemWithNonExistentProduct() {
        // Create an order first
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setUserId("testuser@example.com");
        OrderData orderData = orderDto.add(orderForm);

        // Try to create order item with non-existent product
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setOrderId(orderData.getId());
        orderItemForm.setBarcode("NONEXISTENT123");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        boolean failed = false;
        try {
            orderItemDto.add(orderItemForm);
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed);
    }

    @Test
    void testAddOrderItemWithNullOrderId() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("NullOrderIdTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("NullOrderIdTestClient");
        productForm.setName("Null Order ID Test Product");
        productForm.setBarcode("NULLORDERID123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Try to create order item with null order ID
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setOrderId(null); // This should cause failure
        orderItemForm.setBarcode("NULLORDERID123");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        boolean failed = false;
        try {
            orderItemDto.add(orderItemForm);
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed);
    }

    @Test
    void testGetNonExistentOrderItem() {
        boolean failed = false;
        try {
            orderItemDto.get(99999);
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed);
    }

    @Test
    void testUpdateOrderItem() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("UpdateOrderItemTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("UpdateOrderItemTestClient");
        productForm.setName("Update Order Item Test Product");
        productForm.setBarcode("UPDATEORDERITEM123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("UPDATEORDERITEM123");
        inventoryForm.setQuantity(50);
        inventoryDto.add(inventoryForm);

        // Create an order first
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setUserId("testuser@example.com");
        OrderData orderData = orderDto.add(orderForm);

        // Create order item
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setOrderId(orderData.getId());
        orderItemForm.setBarcode("UPDATEORDERITEM123");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        OrderItemData createdOrderItem = orderItemDto.add(orderItemForm);

        // Update order item
        OrderItemForm updateForm = new OrderItemForm();
        updateForm.setOrderId(orderData.getId());
        updateForm.setBarcode("UPDATEORDERITEM123");
        updateForm.setQuantity(3); // Changed quantity
        updateForm.setSellingPrice(95.0); // Changed selling price

        OrderItemData updatedOrderItem = orderItemDto.update(createdOrderItem.getId(), updateForm);
        assertEquals(300.0, updatedOrderItem.getAmount()); // 3 items * 100.0 MRP
        assertEquals(3, updatedOrderItem.getQuantity());
    }

    @Test
    void testUpdateNonExistentOrderItem() {
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setOrderId(99999);
        orderItemForm.setBarcode("UPDATE123");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        boolean failed = false;
        try {
            orderItemDto.update(99999, orderItemForm);
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed);
    }

    @Test
    void testAddOrderItemWithProductName() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("ProductNameTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("ProductNameTestClient");
        productForm.setName("Product Name Test Product");
        productForm.setBarcode("PRODUCTNAME123");
        productForm.setMrp(100.0);
        productDto.add(productForm);

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("PRODUCTNAME123");
        inventoryForm.setQuantity(50);
        inventoryDto.add(inventoryForm);

        // Create an order first
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setUserId("testuser@example.com");
        OrderData orderData = orderDto.add(orderForm);

        // Create order item using product name instead of barcode
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setOrderId(orderData.getId());
        orderItemForm.setProductName("Product Name Test Product"); // Using product name
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        OrderItemData orderItemData = orderItemDto.add(orderItemForm);
        assertNotNull(orderItemData.getId());
        assertEquals(100.0, orderItemData.getAmount()); // 1 item * 100.0 MRP
        assertEquals("PRODUCTNAME123", orderItemData.getBarcode());
        assertEquals("Product Name Test Product", orderItemData.getProductName());
    }

    @Test
    void testAddOrderItemWithProductId() {
        // Create a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("ProductIdTestClient");
        clientDto.add(clientForm);

        // Create a product
        ProductForm productForm = new ProductForm();
        productForm.setClientName("ProductIdTestClient");
        productForm.setName("Product ID Test Product");
        productForm.setBarcode("PRODUCTID123");
        productForm.setMrp(100.0);
        ProductData productData = productDto.add(productForm);

        // Add inventory
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("PRODUCTID123");
        inventoryForm.setQuantity(50);
        inventoryDto.add(inventoryForm);

        // Create an order first
        OrderForm orderForm = new OrderForm();
        orderForm.setDate(LocalDateTime.now());
        orderForm.setUserId("testuser@example.com");
        OrderData orderData = orderDto.add(orderForm);

        // Create order item using product ID
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setOrderId(orderData.getId());
        orderItemForm.setProductId(productData.getId()); // Using product ID
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(90.0);

        OrderItemData orderItemData = orderItemDto.add(orderItemForm);
        assertNotNull(orderItemData.getId());
        assertEquals(100.0, orderItemData.getAmount()); // 1 item * 100.0 MRP
        assertEquals("PRODUCTID123", orderItemData.getBarcode());
        assertEquals("Product ID Test Product", orderItemData.getProductName());
    }
} 