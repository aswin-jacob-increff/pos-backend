package org.example.order.unit;

import org.example.api.OrderApi;
import org.example.api.OrderItemApi;
import org.example.api.InventoryApi;
import org.example.dao.OrderDao;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderItemPojo;
import org.example.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApiTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderItemApi orderItemApi;

    @Mock
    private InventoryApi inventoryApi;

    @InjectMocks
    private OrderApi orderApi;

    private OrderPojo testOrder;
    private OrderItemPojo testOrderItem;

    @BeforeEach
    void setUp() throws Exception {
        testOrder = new OrderPojo();
        testOrder.setId(1);
        testOrder.setDate(Instant.now());
        testOrder.setTotal(100.0);
        testOrder.setStatus(org.example.pojo.OrderStatus.CREATED);

        testOrderItem = new OrderItemPojo();
        testOrderItem.setId(1);
        testOrderItem.setOrderId(1);
        testOrderItem.setProductBarcode("TEST123");
        testOrderItem.setQuantity(2);

        // Inject the dao field using reflection
        Field daoField = orderApi.getClass().getSuperclass().getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(orderApi, orderDao);

        // Inject the orderItemApi field
        Field orderItemApiField = orderApi.getClass().getDeclaredField("orderItemApi");
        orderItemApiField.setAccessible(true);
        orderItemApiField.set(orderApi, orderItemApi);

        // Inject the inventoryApi field
        Field inventoryApiField = orderApi.getClass().getDeclaredField("inventoryApi");
        inventoryApiField.setAccessible(true);
        inventoryApiField.set(orderApi, inventoryApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(orderDao).insert(any(OrderPojo.class));

        // Act
        orderApi.add(testOrder);

        // Assert
        verify(orderDao).insert(testOrder);
        assertEquals(org.example.pojo.OrderStatus.CREATED, testOrder.getStatus());
        assertNotNull(testOrder.getDate());
    }

    @Test
    void testAdd_NullOrder() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.add(null));
        verify(orderDao, never()).insert(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(orderDao.select(1)).thenReturn(testOrder);

        // Act
        OrderPojo result = orderApi.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        verify(orderDao).select(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.get(null));
        verify(orderDao, never()).select(any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<OrderPojo> orders = Arrays.asList(testOrder, testOrder);
        when(orderDao.selectAll()).thenReturn(orders);

        // Act
        List<OrderPojo> result = orderApi.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderDao).selectAll();
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(orderDao.select(1)).thenReturn(testOrder);
        doNothing().when(orderDao).update(1, testOrder);

        // Act
        orderApi.update(1, testOrder);

        // Assert
        verify(orderDao).select(1);
        verify(orderDao).update(1, testOrder);
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.update(null, testOrder));
        verify(orderDao, never()).update(anyInt(), any());
    }

    @Test
    void testUpdate_NullOrder() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.update(1, null));
        verify(orderDao, never()).update(anyInt(), any());
    }

    @Test
    void testCancelOrder_Success() {
        // Given
        when(orderDao.select(1)).thenReturn(testOrder);
        List<OrderItemPojo> orderItems = Arrays.asList(testOrderItem);
        when(orderItemApi.getByOrderId(1)).thenReturn(orderItems);
        doNothing().when(inventoryApi).addStock("TEST123", 2);
        doNothing().when(orderDao).update(1, testOrder);

        // When
        orderApi.cancelOrder(1);

        // Then
        verify(orderDao).select(1);
        verify(orderItemApi).getByOrderId(1);
        verify(inventoryApi).addStock("TEST123", 2);
        verify(orderDao).update(1, testOrder);
        assertEquals(org.example.pojo.OrderStatus.CANCELLED, testOrder.getStatus());
    }

    @Test
    void testCancelOrder_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> orderApi.cancelOrder(null));
        verify(orderDao, never()).select(any());
        verify(orderDao, never()).update(any(), any());
    }

    @Test
    void testCancelOrder_NotFound() {
        // Given
        when(orderDao.select(999)).thenReturn(null);

        // When & Then
        assertThrows(ApiException.class, () -> orderApi.cancelOrder(999));
        verify(orderDao).select(999);
        verify(orderDao, never()).update(any(), any());
    }

    @Test
    void testUpdateStatus_Success() {
        // Arrange
        when(orderDao.select(1)).thenReturn(testOrder);
        doNothing().when(orderDao).update(1, testOrder);

        // Act
        orderApi.updateStatus(1, org.example.pojo.OrderStatus.CREATED);

        // Assert
        verify(orderDao).select(1);
        verify(orderDao).update(1, testOrder);
        assertEquals(org.example.pojo.OrderStatus.CREATED, testOrder.getStatus());
    }

    @Test
    void testUpdateStatus_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.updateStatus(null, org.example.pojo.OrderStatus.CREATED));
        verify(orderDao, never()).select(any());
        verify(orderDao, never()).update(anyInt(), any());
    }

    @Test
    void testUpdateStatus_NullStatus() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.updateStatus(1, null));
        verify(orderDao, never()).select(any());
        verify(orderDao, never()).update(anyInt(), any());
    }

    @Test
    void testUpdateStatus_NotFound() {
        // Arrange
        when(orderDao.select(999)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.updateStatus(999, org.example.pojo.OrderStatus.CREATED));
        verify(orderDao).select(999);
        verify(orderDao, never()).update(anyInt(), any());
    }
} 