package org.example.order.unit;

import org.example.flow.OrderFlow;
import org.example.api.OrderApi;
import org.example.api.InventoryApi;

import org.example.model.enums.OrderStatus;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderItemPojo;
import org.example.model.form.OrderItemForm;
import org.example.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFlowTest {

    @Mock
    private OrderApi api;

    @Mock
    private InventoryApi inventoryApi;



    @InjectMocks
    private OrderFlow orderFlow;

    private OrderPojo testOrder;
    private OrderItemPojo testOrderItem;

    @BeforeEach
    void setUp() throws Exception {
        testOrder = new OrderPojo();
        testOrder.setId(1);
        testOrder.setDate(ZonedDateTime.now());
        testOrder.setTotal(100.0);
        testOrder.setStatus(OrderStatus.CREATED);

        testOrderItem = new OrderItemPojo();
        testOrderItem.setId(1);
        testOrderItem.setOrderId(1);
        testOrderItem.setProductId(1);
        testOrderItem.setQuantity(2);
        testOrderItem.setSellingPrice(50.0);
        testOrderItem.setAmount(100.0);

        // Inject AbstractFlow's api field
        Field abstractApiField = orderFlow.getClass().getSuperclass().getDeclaredField("api");
        abstractApiField.setAccessible(true);
        abstractApiField.set(orderFlow, api);
        
        // Inject OrderFlow's own api field
        Field apiField = orderFlow.getClass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(orderFlow, api);
        
        // Inject inventoryApi field
        Field inventoryApiField = orderFlow.getClass().getDeclaredField("inventoryApi");
        inventoryApiField.setAccessible(true);
        inventoryApiField.set(orderFlow, inventoryApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(api).add(any(OrderPojo.class));

        // Act
        OrderPojo result = orderFlow.add(testOrder);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        verify(api).add(testOrder);
    }

    @Test
    void testAdd_NullOrder() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderFlow.add(null));
        verify(api, never()).add(any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<OrderPojo> orders = Arrays.asList(testOrder, testOrder);
        when(api.getAll()).thenReturn(orders);

        // Act
        List<OrderPojo> result = orderFlow.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(api).getAll();
    }

    @Test
    void testUpdate_Success() {
        // Given
        OrderPojo order = new OrderPojo();
        order.setId(1);
        order.setDate(ZonedDateTime.now());
        order.setTotal(100.0);
        order.setStatus(OrderStatus.CREATED);
        order.setUserId("user123");

        doNothing().when(api).update(1, order);

        // When
        orderFlow.update(1, order);

        // Then
        verify(api).update(1, order);
    }

    @Test
    void testUpdate_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> orderFlow.update(null, new OrderPojo()));
        verify(api, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullEntity() {
        // When & Then
        assertThrows(ApiException.class, () -> orderFlow.update(1, null));
        verify(api, never()).update(any(), any());
    }





    @Test
    void testCreateOrderWithItems_Success() {
        // Arrange
        OrderPojo order = new OrderPojo();
        order.setId(1);
        order.setUserId("testuser@example.com");
        order.setDate(ZonedDateTime.now());
        
        OrderItemForm itemForm = new OrderItemForm();
        itemForm.setProductId(1);
        itemForm.setQuantity(2);
        itemForm.setSellingPrice(50.0);
        
        List<OrderItemForm> itemForms = Arrays.asList(itemForm);
        
        doNothing().when(api).add(any(OrderPojo.class));
        doNothing().when(api).addOrderItem(any(OrderItemPojo.class));
        doNothing().when(api).update(anyInt(), any(OrderPojo.class));
        doNothing().when(inventoryApi).removeStock(anyInt(), anyInt());

        // Act
        OrderPojo result = orderFlow.createOrderWithItems(order, itemForms);

        // Assert
        assertNotNull(result);
        assertEquals(100.0, result.getTotal()); // 2 * 50.0
        verify(api).add(order);
        verify(api).addOrderItem(any(OrderItemPojo.class));
        verify(api).update(eq(1), any(OrderPojo.class));
        verify(inventoryApi).removeStock(1, 2);
    }

    @Test
    void testCreateOrderWithItems_NullOrder() {
        // Arrange
        OrderItemForm itemForm = new OrderItemForm();
        List<OrderItemForm> itemForms = Arrays.asList(itemForm);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderFlow.createOrderWithItems(null, itemForms));
        verify(api, never()).add(any());
    }

    @Test
    void testCreateOrderWithItems_NullItemList() {
        // Arrange
        OrderPojo order = new OrderPojo();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderFlow.createOrderWithItems(order, null));
        verify(api, never()).add(any());
    }

    @Test
    void testCreateOrderWithItems_EmptyItemList() {
        // Arrange
        OrderPojo order = new OrderPojo();
        List<OrderItemForm> itemForms = Arrays.asList();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderFlow.createOrderWithItems(order, itemForms));
        verify(api, never()).add(any());
    }
} 