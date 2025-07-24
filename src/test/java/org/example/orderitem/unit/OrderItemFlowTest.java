package org.example.orderitem.unit;

import org.example.flow.OrderItemFlow;
import org.example.api.OrderItemApi;
import org.example.pojo.OrderItemPojo;
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
class OrderItemFlowTest {

    @Mock
    private OrderItemApi api;

    @InjectMocks
    private OrderItemFlow orderItemFlow;

    private OrderItemPojo testPojo;

    @BeforeEach
    void setUp() {
        testPojo = new OrderItemPojo();
        testPojo.setId(1);
        testPojo.setOrderId(1);
        testPojo.setProductId(1);
        testPojo.setQuantity(5);
        testPojo.setSellingPrice(100.0);
        testPojo.setAmount(500.0);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(api).add(any(OrderItemPojo.class));

        // Act
        OrderItemPojo result = orderItemFlow.add(testPojo);

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(500.0, result.getAmount()); // Amount should be calculated
        verify(api).add(testPojo);
    }

    @Test
    void testAdd_NullOrderItem() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemFlow.add(null));
        verify(api, never()).add(any());
    }

    @Test
    void testAdd_NullSellingPrice() {
        // Arrange
        testPojo.setSellingPrice(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemFlow.add(testPojo));
        verify(api, never()).add(any());
    }

    @Test
    void testAdd_NullQuantity() {
        // Arrange
        testPojo.setQuantity(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemFlow.add(testPojo));
        verify(api, never()).add(any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<OrderItemPojo> orderItems = Arrays.asList(testPojo, testPojo);
        when(api.getAll()).thenReturn(orderItems);

        // Act
        List<OrderItemPojo> result = orderItemFlow.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(api).getAll();
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        OrderItemPojo orderItem = new OrderItemPojo();
        orderItem.setId(1);
        orderItem.setOrderId(1);
        orderItem.setProductId(1);
        orderItem.setQuantity(5);
        orderItem.setSellingPrice(10.0);

        doNothing().when(api).update(1, orderItem);

        // Act
        orderItemFlow.update(1, orderItem);

        // Assert
        verify(api).update(1, orderItem);
    }

    @Test
    void testUpdate_NullId() {
        // Arrange
        OrderItemPojo orderItem = new OrderItemPojo();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemFlow.update(null, orderItem));
        verify(api, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullEntity() {
        // Arrange
        OrderItemPojo orderItem = new OrderItemPojo();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemFlow.update(1, null));
        verify(api, never()).update(any(), any());
    }

    @Test
    void testGetByOrderId_Success() {
        // Arrange
        List<OrderItemPojo> orderItems = Arrays.asList(testPojo);
        when(api.getByOrderId(1)).thenReturn(orderItems);

        // Act
        List<OrderItemPojo> result = orderItemFlow.getByOrderId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(api).getByOrderId(1);
    }

    @Test
    void testGetByOrderId_NullOrderId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemFlow.getByOrderId(null));
        verify(api, never()).getByOrderId(any());
    }
} 