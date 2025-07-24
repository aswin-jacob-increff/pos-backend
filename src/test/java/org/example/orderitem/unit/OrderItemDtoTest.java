package org.example.orderitem.unit;

import org.example.dto.OrderItemDto;
import org.example.flow.OrderItemFlow;
import org.example.api.OrderItemApi;
import org.example.api.ProductApi;
import org.example.model.form.OrderItemForm;
import org.example.model.data.OrderItemData;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.ProductPojo;
import org.example.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemDtoTest {

    @Mock
    private OrderItemFlow orderItemFlow;

    @Mock
    private OrderItemApi orderItemApi;

    @Mock
    private ProductApi productApi;

    @InjectMocks
    private OrderItemDto orderItemDto;

    private OrderItemPojo testPojo;
    private OrderItemForm testForm;
    private ProductPojo testProduct;

    @BeforeEach
    void setUp() {
        testForm = new OrderItemForm();
        testForm.setOrderId(1);
        testForm.setProductId(1);
        testForm.setQuantity(5);
        testForm.setSellingPrice(100.0);

        testPojo = new OrderItemPojo();
        testPojo.setId(1);
        testPojo.setOrderId(1);
        testPojo.setProductId(1);
        testPojo.setQuantity(5);
        testPojo.setSellingPrice(100.0);
        testPojo.setAmount(500.0);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setMrp(100.0);
    }

    @Test
    void testBasicSetup() {
        assertNotNull(orderItemDto);
        assertNotNull(testPojo);
        assertNotNull(testForm);
        assertNotNull(testProduct);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(orderItemApi).add(any(OrderItemPojo.class));

        // Act
        OrderItemData result = orderItemDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        verify(orderItemApi).add(any(OrderItemPojo.class));
    }

    @Test
    void testAdd_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemDto.add(null));
        verify(orderItemApi, never()).add(any());
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(orderItemApi).update(any(Integer.class), any(OrderItemPojo.class));

        // Act
        OrderItemData result = orderItemDto.update(1, testForm);

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        verify(orderItemApi).update(1, any(OrderItemPojo.class));
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemDto.update(null, testForm));
        verify(orderItemApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderItemDto.update(1, null));
        verify(orderItemApi, never()).update(any(), any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(orderItemApi.get(1)).thenReturn(testPojo);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        OrderItemData result = orderItemDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
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
        List<OrderItemPojo> orderItems = Arrays.asList(testPojo);
        when(orderItemApi.getAll()).thenReturn(orderItems);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        List<OrderItemData> result = orderItemDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPojo.getId(), result.get(0).getId());
        verify(orderItemApi).getAll();
    }

    @Test
    void testGetByOrderId_Success() {
        // Arrange
        List<OrderItemPojo> orderItems = Arrays.asList(testPojo);
        when(orderItemFlow.getByOrderId(1)).thenReturn(orderItems);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        List<OrderItemData> result = orderItemDto.getByOrderId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPojo.getId(), result.get(0).getId());
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