package org.example.order.unit;

import org.example.dto.OrderDto;
import org.example.flow.OrderFlow;
import org.example.model.data.OrderData;
import org.example.model.form.OrderForm;
import org.example.model.data.OrderItemData;
import org.example.model.form.OrderItemForm;
import org.example.pojo.OrderPojo;
import org.example.exception.ApiException;
import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.api.InvoiceApi;
import org.example.api.OrderApi;
import org.example.dao.OrderItemDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.example.model.enums.OrderStatus;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDtoTest {

    @Mock
    private OrderFlow orderFlow;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @Mock
    private InvoiceApi invoiceApi;



    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OrderApi orderApi;

    @InjectMocks
    private OrderDto orderDto;

    private OrderForm testForm;
    private OrderPojo testOrder;
    private OrderItemForm testOrderItemForm;
    private OrderItemData testOrderItemData;

    @BeforeEach
    void setUp() throws Exception {
        testForm = new OrderForm();
        testForm.setUserId("testuser@example.com");

        testOrder = new OrderPojo();
        testOrder.setId(1); // Ensure ID is set
        testOrder.setDate(ZonedDateTime.now());
        testOrder.setTotal(100.0);
        testOrder.setUserId("testuser@example.com");
        testOrder.setStatus(OrderStatus.CREATED);

        testOrderItemForm = new OrderItemForm();
        testOrderItemForm.setBarcode("TEST123");
        testOrderItemForm.setQuantity(2);
        testOrderItemForm.setSellingPrice(50.0);

        testOrderItemData = new OrderItemData();
        testOrderItemData.setId(1);
        testOrderItemData.setOrderId(1);
        testOrderItemData.setBarcode("TEST123");
        testOrderItemData.setQuantity(2);
        testOrderItemData.setSellingPrice(50.0);
        testOrderItemData.setAmount(100.0);

        // Inject the orderFlow field
        Field orderFlowField = orderDto.getClass().getDeclaredField("orderFlow");
        orderFlowField.setAccessible(true);
        orderFlowField.set(orderDto, orderFlow);

        // Inject the productApi field
        Field productApiField = orderDto.getClass().getDeclaredField("productApi");
        productApiField.setAccessible(true);
        productApiField.set(orderDto, productApi);

        // Inject the clientApi field
        Field clientApiField = orderDto.getClass().getDeclaredField("clientApi");
        clientApiField.setAccessible(true);
        clientApiField.set(orderDto, clientApi);

        // Inject the invoiceApi field
        Field invoiceApiField = orderDto.getClass().getDeclaredField("invoiceApi");
        invoiceApiField.setAccessible(true);
        invoiceApiField.set(orderDto, invoiceApi);



        // Inject the restTemplate field
        Field restTemplateField = orderDto.getClass().getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(orderDto, restTemplate);

        // Inject the api field from AbstractDto
        Field apiField = orderDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(orderDto, orderApi);
    }



    @Test
    void testAdd_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(null));
        verify(orderFlow, never()).add(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(orderApi.get(1)).thenReturn(testOrder);

        // Act
        OrderData result = orderDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getUserId(), result.getUserId());
        verify(orderApi).get(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.get(null));
        verify(orderApi, never()).get(any());
    }

    @Test
    void testGetAll_Success() {
        // Given
        List<OrderPojo> orders = Arrays.asList(
            new OrderPojo(), new OrderPojo()
        );
        when(orderApi.getAll()).thenReturn(orders);

        // When
        List<OrderData> result = orderDto.getAll();

        // Then
        assertEquals(2, result.size());
        verify(orderApi).getAll();
    }

    @Test
    void testUpdate_Success() {
        // Given
        OrderForm form = new OrderForm();
        form.setDate(ZonedDateTime.now());
        form.setUserId("user123");

        OrderPojo order = new OrderPojo();
        order.setId(1);
        order.setDate(ZonedDateTime.now());
        order.setTotal(100.0);
        order.setStatus(OrderStatus.CREATED);
        order.setUserId("user123");

        when(orderApi.get(1)).thenReturn(order);
        lenient().doNothing().when(orderApi).update(anyInt(), any(OrderPojo.class));

        // When
        OrderData result = orderDto.update(1, form);

        // Then
        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        verify(orderApi).update(eq(1), any(OrderPojo.class));
    }

    @Test
    void testUpdate_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> orderDto.update(null, new OrderForm()));
        verify(orderApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullForm() {
        // When & Then
        assertThrows(ApiException.class, () -> orderDto.update(1, null));
        verify(orderApi, never()).update(any(), any());
    }



    @Test
    void testGetOrdersByDateRange_Success() {
        // Arrange
        List<OrderPojo> orders = Arrays.asList(testOrder);
        when(orderFlow.getOrdersByDateRange(any(), any())).thenReturn(orders);

        // Act
        List<OrderData> result = orderDto.getOrdersByDateRange(LocalDate.now(), LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderFlow).getOrdersByDateRange(any(), any());
    }

    @Test
    void testGetOrdersByDateRange_NullStartDate() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.getOrdersByDateRange(null, LocalDate.now()));
    }

    @Test
    void testGetOrdersByDateRange_NullEndDate() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.getOrdersByDateRange(LocalDate.now(), null));
    }

    @Test
    void testGetOrdersByUserId_Success() {
        // Arrange
        List<OrderPojo> orders = Arrays.asList(testOrder);
        when(orderFlow.getOrdersByUserId(any())).thenReturn(orders);

        // Act
        List<OrderData> result = orderDto.getOrdersByUserId("testuser@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderFlow).getOrdersByUserId(any());
    }

    @Test
    void testGetOrdersByUserId_NullUserId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.getOrdersByUserId(null));
        verify(orderFlow, never()).getAll();
    }

    @Test
    void testGetOrdersByUserId_EmptyUserId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.getOrdersByUserId(""));
        verify(orderFlow, never()).getAll();
    }

    @Test
    void testDownloadInvoice_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> orderDto.downloadInvoice(null));
        verify(orderApi, never()).get(any());
    }
} 