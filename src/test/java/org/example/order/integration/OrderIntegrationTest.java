package org.example.order.integration;

import org.example.dto.OrderDto;
import org.example.flow.OrderFlow;
import org.example.api.OrderApi;
import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.api.InvoiceApi;
import org.example.api.InvoiceClientApi;
import org.example.model.data.OrderData;
import org.example.model.data.OrderItemData;
import org.example.model.form.OrderForm;
import org.example.model.form.OrderItemForm;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.pojo.InvoicePojo;
import org.example.exception.ApiException;
import org.example.model.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderIntegrationTest {

    @Mock
    private OrderFlow orderFlow;

    @Mock
    private OrderApi orderApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @Mock
    private InvoiceApi invoiceApi;

    @Mock
    private InvoiceClientApi invoiceClientApi;

    @InjectMocks
    private OrderDto orderDto;

    private OrderForm testForm;
    private OrderPojo testOrder;
    private OrderItemForm testOrderItemForm;
    private OrderItemData testOrderItemData;
    private ProductPojo testProduct;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("Test Client");
        testClient.setStatus(true);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("123456789");
        testProduct.setMrp(100.0);
        testProduct.setClientId(1);

        testForm = new OrderForm();
        testForm.setUserId("testuser@example.com");
        testForm.setDate(ZonedDateTime.now());

        testOrder = new OrderPojo();
        testOrder.setId(1);
        testOrder.setDate(ZonedDateTime.now());
        testOrder.setTotal(100.0);
        testOrder.setUserId("testuser@example.com");
        testOrder.setStatus(OrderStatus.CREATED);

        testOrderItemForm = new OrderItemForm();
        testOrderItemForm.setOrderId(1);
        testOrderItemForm.setProductId(1);
        testOrderItemForm.setQuantity(2);
        testOrderItemForm.setSellingPrice(50.0);

        testOrderItemData = new OrderItemData();
        testOrderItemData.setId(1);
        testOrderItemData.setOrderId(1);
        testOrderItemData.setProductId(1);
        testOrderItemData.setQuantity(2);
        testOrderItemData.setSellingPrice(50.0);
        testOrderItemData.setAmount(100.0);
        testOrderItemData.setProductName("Test Product");
        testOrderItemData.setBarcode("123456789");
        testOrderItemData.setClientName("Test Client");

        // Inject the api field from AbstractDto
        Field apiField = orderDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(orderDto, orderApi);
    }

    @Test
    void testAdd_Integration_Success() {
        // Arrange
        testForm.setOrderItemFormList(Arrays.asList(testOrderItemForm));
        when(orderFlow.createOrderWithItems(any(OrderPojo.class), anyList())).thenReturn(testOrder);

        // Act
        OrderData result = orderDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getUserId(), result.getUserId());
        verify(orderFlow).createOrderWithItems(any(OrderPojo.class), eq(Arrays.asList(testOrderItemForm)));
    }

    @Test
    void testGet_Integration_Success() {
        // Arrange
        when(orderApi.get(1)).thenReturn(testOrder);
        when(orderApi.getOrderItemsByOrderId(1)).thenReturn(Arrays.asList());

        // Act
        OrderData result = orderDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getUserId(), result.getUserId());
        verify(orderApi).get(1);
    }

    @Test
    void testGet_Integration_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.get(null));
        verify(orderApi, never()).get(any());
    }

    @Test
    void testGetAll_Integration_Success() {
        // Arrange
        List<OrderPojo> orders = Arrays.asList(testOrder);
        when(orderApi.getAll()).thenReturn(orders);
        when(orderApi.getOrderItemsByOrderId(1)).thenReturn(Arrays.asList());

        // Act
        List<OrderData> result = orderDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
        verify(orderApi).getAll();
    }

    @Test
    void testGetOrdersByDateRange_Integration_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(1);
        List<OrderPojo> orders = Arrays.asList(testOrder);
        when(orderFlow.getOrdersByDateRange(startDate, endDate)).thenReturn(orders);
        when(orderApi.getOrderItemsByOrderId(1)).thenReturn(Arrays.asList());

        // Act
        List<OrderData> result = orderDto.getOrdersByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
        verify(orderFlow).getOrdersByDateRange(startDate, endDate);
    }

    @Test
    void testGetOrdersByUserId_Integration_Success() {
        // Arrange
        String userId = "testuser@example.com";
        List<OrderPojo> orders = Arrays.asList(testOrder);
        when(orderFlow.getOrdersByUserId(userId)).thenReturn(orders);
        when(orderApi.getOrderItemsByOrderId(1)).thenReturn(Arrays.asList());

        // Act
        List<OrderData> result = orderDto.getOrdersByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
        verify(orderFlow).getOrdersByUserId(userId);
    }

    @Test
    void testGetOrderItemsByOrderId_Integration_Success() {
        // Arrange
        OrderItemPojo orderItemPojo = new OrderItemPojo();
        orderItemPojo.setId(1);
        orderItemPojo.setOrderId(1);
        orderItemPojo.setProductId(1);
        orderItemPojo.setQuantity(2);
        orderItemPojo.setSellingPrice(50.0);
        orderItemPojo.setAmount(100.0);
        
        when(orderApi.getOrderItemsByOrderId(1)).thenReturn(Arrays.asList(orderItemPojo));
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        when(orderApi.get(1)).thenReturn(testOrder);

        // Act
        List<OrderItemData> result = orderDto.getOrderItemsByOrderId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderItemPojo.getId(), result.get(0).getId());
        verify(orderApi).getOrderItemsByOrderId(1);
    }

    @Test
    void testAddOrderItem_Integration_Success() {
        // Arrange
        OrderItemPojo orderItemPojo = new OrderItemPojo();
        orderItemPojo.setId(1);
        orderItemPojo.setOrderId(1);
        orderItemPojo.setProductId(1);
        orderItemPojo.setQuantity(2);
        orderItemPojo.setSellingPrice(50.0);
        orderItemPojo.setAmount(100.0);
        
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(orderFlow).reduceInventoryForOrderItem(1, 2);
        doNothing().when(orderApi).addOrderItem(any(OrderItemPojo.class));
        when(clientApi.get(1)).thenReturn(testClient);
        when(orderApi.get(1)).thenReturn(testOrder);

        // Act
        OrderItemData result = orderDto.addOrderItem(testOrderItemForm);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderItemForm.getProductId(), result.getProductId());
        verify(productApi, times(3)).get(1);
        verify(orderFlow).reduceInventoryForOrderItem(1, 2);
        verify(orderApi).addOrderItem(any(OrderItemPojo.class));
    }
} 