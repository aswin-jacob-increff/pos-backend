package org.example.orderitem.unit;

import org.example.dto.OrderItemDto;
import org.example.flow.OrderItemFlow;
import org.example.api.ProductApi;
import org.example.model.data.OrderItemData;
import org.example.model.form.OrderItemForm;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.ProductPojo;
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
class OrderItemDtoTest {

    @Mock
    private OrderItemFlow orderItemFlow;

    @Mock
    private ProductApi productApi;

    @Mock
    private org.example.api.OrderItemApi orderItemApi;

    @InjectMocks
    private OrderItemDto orderItemDto;

    private OrderItemForm testForm;
    private OrderItemPojo testOrderItem;
    private ProductPojo testProduct;

    @BeforeEach
    void setUp() throws Exception {
        testForm = new OrderItemForm();
        testForm.setOrderId(1);
        testForm.setBarcode("TEST123");
        testForm.setQuantity(2);
        testForm.setSellingPrice(50.0);

        testOrderItem = new OrderItemPojo();
        testOrderItem.setId(1);
        testOrderItem.setOrderId(1);
        testOrderItem.setProductBarcode("TEST123");
        testOrderItem.setProductName("Test Product");
        testOrderItem.setQuantity(2);
        testOrderItem.setSellingPrice(50.0);
        testOrderItem.setAmount(100.0);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setMrp(100.0);

        // Inject the orderItemFlow field
        Field orderItemFlowField = orderItemDto.getClass().getDeclaredField("orderItemFlow");
        orderItemFlowField.setAccessible(true);
        orderItemFlowField.set(orderItemDto, orderItemFlow);

        // Inject the productApi field
        Field productApiField = orderItemDto.getClass().getDeclaredField("productApi");
        productApiField.setAccessible(true);
        productApiField.set(orderItemDto, productApi);

        // Inject the api field from AbstractDto
        Field apiField = orderItemDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(orderItemDto, orderItemApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        when(productApi.get(anyInt())).thenReturn(testProduct);
        when(productApi.getByBarcode(anyString())).thenReturn(testProduct);
        doNothing().when(orderItemApi).add(any(OrderItemPojo.class));

        // Act
        OrderItemData result = orderItemDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderItem.getOrderId(), result.getOrderId());
        assertEquals(testOrderItem.getProductBarcode(), result.getBarcode());
        verify(productApi).get(anyInt());
        verify(productApi, atLeastOnce()).getByBarcode(anyString());
        verify(orderItemApi).add(any(OrderItemPojo.class));
    }

    @Test
    void testAdd_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemDto.add(null));
        verify(orderItemApi, never()).add(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(orderItemApi.get(1)).thenReturn(testOrderItem);

        // Act
        OrderItemData result = orderItemDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderItem.getId(), result.getId());
        assertEquals(testOrderItem.getOrderId(), result.getOrderId());
        verify(orderItemApi).get(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemDto.get(null));
        verify(orderItemApi, never()).get(any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<OrderItemPojo> orderItems = Arrays.asList(testOrderItem, testOrderItem);
        when(orderItemApi.getAll()).thenReturn(orderItems);

        // Act
        List<OrderItemData> result = orderItemDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderItemApi).getAll();
    }

    @Test
    void testUpdate_Success() {
        // Given
        OrderItemForm form = new OrderItemForm();
        form.setOrderId(1);
        form.setBarcode("UPD123");
        form.setQuantity(5);

        OrderItemPojo orderItem = new OrderItemPojo();
        orderItem.setId(1);
        orderItem.setProductBarcode("UPD123");
        orderItem.setQuantity(5);

        ProductPojo product = new ProductPojo();
        product.setId(1);
        product.setBarcode("UPD123");
        product.setName("Updated Product");
        product.setMrp(100.0);

        when(orderItemApi.get(1)).thenReturn(orderItem);
        when(productApi.getByBarcode("UPD123")).thenReturn(product);
        when(productApi.get(1)).thenReturn(product);
        lenient().doNothing().when(orderItemApi).update(anyInt(), any(OrderItemPojo.class));

        // When
        OrderItemData result = orderItemDto.update(1, form);

        // Then
        assertNotNull(result);
        assertEquals("UPD123", result.getBarcode());
        verify(orderItemApi).update(eq(1), any(OrderItemPojo.class));
    }

    @Test
    void testUpdate_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> orderItemDto.update(null, new OrderItemForm()));
        verify(orderItemApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullForm() {
        // When & Then
        assertThrows(ApiException.class, () -> orderItemDto.update(1, null));
        verify(orderItemApi, never()).update(any(), any());
    }

    @Test
    void testGetByOrderId_Success() {
        // Arrange
        List<OrderItemPojo> orderItems = Arrays.asList(testOrderItem);
        when(orderItemFlow.getByOrderId(1)).thenReturn(orderItems);

        // Act
        List<OrderItemData> result = orderItemDto.getByOrderId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrderItem.getId(), result.get(0).getId());
        verify(orderItemFlow).getByOrderId(1);
    }

    @Test
    void testGetByOrderId_NullOrderId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemDto.getByOrderId(null));
        verify(orderItemFlow, never()).getByOrderId(any());
    }

    @Test
    void testGetByOrderId_InvalidId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemDto.getByOrderId(0));
        verify(orderItemFlow, never()).getByOrderId(any());
    }
} 