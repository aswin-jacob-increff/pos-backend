package org.example.orderitem.unit;

import org.example.api.OrderItemApi;
import org.example.api.InventoryApi;
import org.example.dao.OrderItemDao;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.InventoryPojo;
import org.example.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemApiTest {

    @Mock
    private OrderItemDao orderItemDao;

    @Mock
    private InventoryApi inventoryApi;

    @InjectMocks
    private OrderItemApi orderItemApi;

    private OrderItemPojo testOrderItem;
    private InventoryPojo testInventory;

    @BeforeEach
    void setUp() throws Exception {
        testOrderItem = new OrderItemPojo();
        testOrderItem.setId(1);
        testOrderItem.setOrderId(1);
        testOrderItem.setProductBarcode("TEST123");
        testOrderItem.setProductName("Test Product");
        testOrderItem.setQuantity(2);
        testOrderItem.setSellingPrice(50.0);
        testOrderItem.setAmount(100.0);

        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductBarcode("TEST123");
        testInventory.setQuantity(10);

        // Inject the dao field using reflection
        Field daoField = orderItemApi.getClass().getSuperclass().getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(orderItemApi, orderItemDao);

        // Inject the inventoryApi field
        Field inventoryApiField = orderItemApi.getClass().getDeclaredField("inventoryApi");
        inventoryApiField.setAccessible(true);
        inventoryApiField.set(orderItemApi, inventoryApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        when(inventoryApi.getByProductBarcode("TEST123")).thenReturn(testInventory);
        doNothing().when(inventoryApi).removeStock("TEST123", 2);
        doNothing().when(orderItemDao).insert(any(OrderItemPojo.class));

        // Act
        orderItemApi.add(testOrderItem);

        // Assert
        verify(inventoryApi).getByProductBarcode("TEST123");
        verify(inventoryApi).removeStock("TEST123", 2);
        verify(orderItemDao).insert(testOrderItem);
        assertEquals(100.0, testOrderItem.getAmount());
    }

    @Test
    void testAdd_NullOrderItem() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.add(null));
        verify(orderItemDao, never()).insert(any());
    }

    @Test
    void testAdd_NullBarcode() {
        // Arrange
        testOrderItem.setProductBarcode(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.add(testOrderItem));
        verify(orderItemDao, never()).insert(any());
    }

    @Test
    void testAdd_EmptyBarcode() {
        // Arrange
        testOrderItem.setProductBarcode("");

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.add(testOrderItem));
        verify(orderItemDao, never()).insert(any());
    }

    @Test
    void testAdd_InventoryNotFound() {
        // Arrange
        when(inventoryApi.getByProductBarcode("TEST123")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.add(testOrderItem));
        verify(inventoryApi).getByProductBarcode("TEST123");
        verify(orderItemDao, never()).insert(any());
    }

    @Test
    void testAdd_InsufficientStock() {
        // Arrange
        testInventory.setQuantity(1);
        when(inventoryApi.getByProductBarcode("TEST123")).thenReturn(testInventory);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.add(testOrderItem));
        verify(inventoryApi).getByProductBarcode("TEST123");
        verify(orderItemDao, never()).insert(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(orderItemDao.select(1)).thenReturn(testOrderItem);

        // Act
        OrderItemPojo result = orderItemApi.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderItem.getId(), result.getId());
        verify(orderItemDao).select(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.get(null));
        verify(orderItemDao, never()).select(any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<OrderItemPojo> orderItems = Arrays.asList(testOrderItem, testOrderItem);
        when(orderItemDao.selectAll()).thenReturn(orderItems);

        // Act
        List<OrderItemPojo> result = orderItemApi.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderItemDao).selectAll();
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        OrderItemPojo existingOrderItem = new OrderItemPojo();
        existingOrderItem.setId(1);
        existingOrderItem.setQuantity(1);
        existingOrderItem.setProductBarcode("TEST123");

        when(orderItemDao.select(1)).thenReturn(existingOrderItem);
        when(inventoryApi.getByProductBarcode("TEST123")).thenReturn(testInventory);
        doNothing().when(inventoryApi).addStock("TEST123", 1);
        doNothing().when(inventoryApi).removeStock("TEST123", 2);
        doNothing().when(orderItemDao).update(1, existingOrderItem);

        // Act
        orderItemApi.update(1, testOrderItem);

        // Assert
        verify(orderItemDao).select(1);
        verify(inventoryApi).getByProductBarcode("TEST123");
        verify(inventoryApi).addStock("TEST123", 1);
        verify(inventoryApi).removeStock("TEST123", 2);
        verify(orderItemDao).update(1, existingOrderItem);
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.update(null, testOrderItem));
        verify(orderItemDao, never()).update(anyInt(), any());
    }

    @Test
    void testUpdate_NullOrderItem() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.update(1, null));
        verify(orderItemDao, never()).update(anyInt(), any());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(orderItemDao.select(999)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.update(999, testOrderItem));
        verify(orderItemDao).select(999);
        verify(orderItemDao, never()).update(anyInt(), any());
    }

    @Test
    void testUpdate_InventoryNotFound() {
        // Arrange
        OrderItemPojo existingOrderItem = new OrderItemPojo();
        existingOrderItem.setId(1);
        existingOrderItem.setQuantity(1);
        existingOrderItem.setProductBarcode("TEST123");

        when(orderItemDao.select(1)).thenReturn(existingOrderItem);
        when(inventoryApi.getByProductBarcode("TEST123")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.update(1, testOrderItem));
        verify(orderItemDao).select(1);
        verify(inventoryApi).getByProductBarcode("TEST123");
        verify(orderItemDao, never()).update(anyInt(), any());
    }

    @Test
    void testUpdate_InsufficientStock() {
        // Arrange
        OrderItemPojo existingOrderItem = new OrderItemPojo();
        existingOrderItem.setId(1);
        existingOrderItem.setQuantity(1);
        existingOrderItem.setProductBarcode("TEST123");

        testInventory.setQuantity(0);
        when(orderItemDao.select(1)).thenReturn(existingOrderItem);
        when(inventoryApi.getByProductBarcode("TEST123")).thenReturn(testInventory);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.update(1, testOrderItem));
        verify(orderItemDao).select(1);
        verify(inventoryApi).getByProductBarcode("TEST123");
        verify(orderItemDao, never()).update(anyInt(), any());
    }

    @Test
    void testGetByOrderId_Success() {
        // Arrange
        List<OrderItemPojo> orderItems = Arrays.asList(testOrderItem);
        when(orderItemDao.selectByOrderId(1)).thenReturn(orderItems);

        // Act
        List<OrderItemPojo> result = orderItemApi.getByOrderId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderItemDao).selectByOrderId(1);
    }

    @Test
    void testGetByOrderId_NullOrderId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.getByOrderId(null));
        verify(orderItemDao, never()).selectByOrderId(any());
    }

    @Test
    void testGetByProductBarcode_Success() {
        // Arrange
        List<OrderItemPojo> orderItems = Arrays.asList(testOrderItem);
        when(orderItemDao.selectByProductBarcode("TEST123")).thenReturn(orderItems);

        // Act
        List<OrderItemPojo> result = orderItemApi.getByProductBarcode("TEST123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderItemDao).selectByProductBarcode("TEST123");
    }

    @Test
    void testGetByProductBarcode_NullBarcode() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.getByProductBarcode(null));
        verify(orderItemDao, never()).selectByProductBarcode(any());
    }

    @Test
    void testGetByProductBarcode_EmptyBarcode() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemApi.getByProductBarcode(""));
        verify(orderItemDao, never()).selectByProductBarcode(any());
    }
} 