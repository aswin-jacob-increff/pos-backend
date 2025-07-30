package org.example.order.unit;

import org.example.dto.OrderDto;
import org.example.flow.OrderFlow;
import org.example.model.data.OrderData;
import org.example.model.form.OrderForm;
import org.example.model.data.OrderItemData;
import org.example.model.form.OrderItemForm;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.exception.ApiException;
import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.api.InvoiceApi;
import org.example.api.InventoryApi;
import org.example.api.OrderApi;
import org.example.dao.OrderItemDao;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
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
    private InventoryApi inventoryApi;

    @Mock
    private OrderApi orderApi;

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
        testForm = new OrderForm();
        testForm.setUserId("testuser@example.com");

        testOrder = new OrderPojo();
        testOrder.setId(1); // Ensure ID is set
        testOrder.setDate(ZonedDateTime.now());
        testOrder.setTotal(100.0);
        testOrder.setUserId("testuser@example.com");
        testOrder.setStatus(OrderStatus.CREATED);

        testOrderItemForm = new OrderItemForm();
        testOrderItemForm.setOrderId(1);
        testOrderItemForm.setProductId(1);
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

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("123456789");
        testProduct.setMrp(100.0);
        testProduct.setClientId(1);

        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("Test Client");
        testClient.setStatus(true);

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

        // Inject the inventoryApi field
        Field inventoryApiField = orderDto.getClass().getDeclaredField("inventoryApi");
        inventoryApiField.setAccessible(true);
        inventoryApiField.set(orderDto, inventoryApi);

        // Inject the InvoiceClientApi field
        Field invoiceClientApiField = orderDto.getClass().getDeclaredField("invoiceClientApi");
        invoiceClientApiField.setAccessible(true);
        invoiceClientApiField.set(orderDto, mock(org.example.api.InvoiceClientApi.class));

        // Inject the api field from AbstractDto
        Field apiField = orderDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(orderDto, orderApi);
    }



    @Test
    void testAdd_Success() {
        // Arrange
        testForm.setOrderItemFormList(Arrays.asList(testOrderItemForm));
        OrderPojo createdOrder = new OrderPojo();
        createdOrder.setId(1);
        createdOrder.setTotal(100.0);
        createdOrder.setUserId("testuser@example.com");
        createdOrder.setStatus(OrderStatus.CREATED);
        
        when(productApi.get(1)).thenReturn(testProduct);
        when(orderFlow.createOrderWithItems(any(OrderPojo.class), anyList())).thenReturn(createdOrder);

        // Act
        OrderData result = orderDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(createdOrder.getId(), result.getId());
        assertEquals(createdOrder.getUserId(), result.getUserId());
        verify(orderFlow).createOrderWithItems(any(OrderPojo.class), any(List.class));
    }

    @Test
    void testAdd_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(null));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
    }

    @Test
    void testAdd_NullUserId() {
        // Arrange
        testForm.setUserId(null);
        testForm.setOrderItemFormList(Arrays.asList(testOrderItemForm));

        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(testForm));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
    }

    @Test
    void testAdd_EmptyUserId() {
        // Arrange
        testForm.setUserId("");
        testForm.setOrderItemFormList(Arrays.asList(testOrderItemForm));

        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(testForm));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
    }

    @Test
    void testAdd_NullOrderItemList() {
        // Arrange
        testForm.setOrderItemFormList(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(testForm));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
    }

    @Test
    void testAdd_EmptyOrderItemList() {
        // Arrange
        testForm.setOrderItemFormList(Arrays.asList());

        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(testForm));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
    }

    @Test
    void testAdd_NullOrderItem() {
        // Arrange
        testForm.setOrderItemFormList(Arrays.asList((OrderItemForm) null));

        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(testForm));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
    }

    @Test
    void testAdd_NullProductId() {
        // Arrange
        testOrderItemForm.setProductId(null);
        testForm.setOrderItemFormList(Arrays.asList(testOrderItemForm));

        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(testForm));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
    }

    @Test
    void testAdd_NullQuantity() {
        // Arrange
        testOrderItemForm.setQuantity(null);
        testForm.setOrderItemFormList(Arrays.asList(testOrderItemForm));

        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(testForm));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
    }

    @Test
    void testAdd_ZeroQuantity() {
        // Arrange
        testOrderItemForm.setQuantity(0);
        testForm.setOrderItemFormList(Arrays.asList(testOrderItemForm));

        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(testForm));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
    }

    @Test
    void testAdd_NullSellingPrice() {
        // Arrange
        testOrderItemForm.setSellingPrice(null);
        testForm.setOrderItemFormList(Arrays.asList(testOrderItemForm));

        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(testForm));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
    }

    @Test
    void testAdd_ZeroSellingPrice() {
        // Arrange
        testOrderItemForm.setSellingPrice(0.0);
        testForm.setOrderItemFormList(Arrays.asList(testOrderItemForm));

        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.add(testForm));
        verify(orderFlow, never()).createOrderWithItems(any(), any());
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
        OrderPojo order1 = new OrderPojo();
        order1.setId(1);
        order1.setUserId("user1");
        
        OrderPojo order2 = new OrderPojo();
        order2.setId(2);
        order2.setUserId("user2");
        
        List<OrderPojo> orders = Arrays.asList(order1, order2);
        when(orderApi.getAll()).thenReturn(orders);
        when(orderApi.getOrderItemsByOrderId(1)).thenReturn(Arrays.asList());
        when(orderApi.getOrderItemsByOrderId(2)).thenReturn(Arrays.asList());

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

    // ========== ORDER ITEM MANAGEMENT TESTS ==========

    @Test
    void testGetOrderItemsByOrderId_Success() {
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
    void testGetOrderItemsByOrderId_NullOrderId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.getOrderItemsByOrderId(null));
        verify(orderApi, never()).getOrderItemsByOrderId(any());
    }

    @Test
    void testGetOrderItemsByOrderId_InvalidOrderId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.getOrderItemsByOrderId(0));
        verify(orderApi, never()).getOrderItemsByOrderId(any());
    }



    @Test
    void testGetOrderItem_Success() {
        // Arrange
        OrderItemPojo orderItemPojo = new OrderItemPojo();
        orderItemPojo.setId(1);
        orderItemPojo.setOrderId(1);
        orderItemPojo.setProductId(1);
        orderItemPojo.setQuantity(2);
        orderItemPojo.setSellingPrice(50.0);
        orderItemPojo.setAmount(100.0);
        
        when(orderApi.getOrderItem(1)).thenReturn(orderItemPojo);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        when(orderApi.get(1)).thenReturn(testOrder);

        // Act
        OrderItemData result = orderDto.getOrderItem(1);

        // Assert
        assertNotNull(result);
        assertEquals(orderItemPojo.getId(), result.getId());
        verify(orderApi).getOrderItem(1);
    }

    @Test
    void testGetOrderItem_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.getOrderItem(null));
        verify(orderApi, never()).getOrderItem(any());
    }

    // ========== PAGINATION TESTS ==========

    @Test
    void testGetOrdersByUserIdPaginated_Success() {
        // Arrange
        String userId = "testuser@example.com";
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<OrderPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testOrder));
        expectedResponse.setTotalElements(1);
        
        when(orderFlow.getByUserIdPaginated(userId, request)).thenReturn(expectedResponse);
        when(orderApi.getOrderItemsByOrderId(1)).thenReturn(Arrays.asList());

        // Act
        PaginationResponse<OrderData> result = orderDto.getOrdersByUserIdPaginated(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(orderFlow).getByUserIdPaginated(userId, request);
    }

    @Test
    void testGetOrdersByDateRangePaginated_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(1);
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<OrderPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testOrder));
        expectedResponse.setTotalElements(1);
        
        when(orderFlow.getByDateRangePaginated(startDate, endDate, request)).thenReturn(expectedResponse);
        when(orderApi.getOrderItemsByOrderId(1)).thenReturn(Arrays.asList());

        // Act
        PaginationResponse<OrderData> result = orderDto.getOrdersByDateRangePaginated(startDate, endDate, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(orderFlow).getByDateRangePaginated(startDate, endDate, request);
    }

    // ========== SUBSTRING SEARCH TESTS ==========

    @Test
    void testFindOrdersBySubstringId_Success() {
        // Arrange
        String searchId = "123";
        int maxResults = 10;
        List<OrderPojo> orders = Arrays.asList(testOrder);
        when(orderFlow.findOrdersBySubstringId(searchId, maxResults)).thenReturn(orders);
        when(orderApi.getOrderItemsByOrderId(1)).thenReturn(Arrays.asList());

        // Act
        List<OrderData> result = orderDto.findOrdersBySubstringId(searchId, maxResults);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
        verify(orderFlow).findOrdersBySubstringId(searchId, maxResults);
    }

    @Test
    void testFindOrdersBySubstringId_NullSearchId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.findOrdersBySubstringId(null, 10));
        verify(orderFlow, never()).findOrdersBySubstringId(any(), anyInt());
    }

    @Test
    void testFindOrdersBySubstringId_EmptySearchId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.findOrdersBySubstringId("", 10));
        verify(orderFlow, never()).findOrdersBySubstringId(any(), anyInt());
    }

    @Test
    void testFindOrdersBySubstringId_InvalidMaxResults() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.findOrdersBySubstringId("123", 0));
        verify(orderFlow, never()).findOrdersBySubstringId(any(), anyInt());
    }

    @Test
    void testFindOrdersBySubstringIdPaginated_Success() {
        // Arrange
        String searchId = "123";
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<OrderPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testOrder));
        expectedResponse.setTotalElements(1);
        
        when(orderFlow.findOrdersBySubstringIdPaginated(searchId, request)).thenReturn(expectedResponse);
        when(orderApi.getOrderItemsByOrderId(1)).thenReturn(Arrays.asList());

        // Act
        PaginationResponse<OrderData> result = orderDto.findOrdersBySubstringIdPaginated(searchId, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(orderFlow).findOrdersBySubstringIdPaginated(searchId, request);
    }

    @Test
    void testFindOrdersBySubstringIdPaginated_NullSearchId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.findOrdersBySubstringIdPaginated(null, new PaginationRequest()));
        verify(orderFlow, never()).findOrdersBySubstringIdPaginated(any(), any());
    }

    @Test
    void testFindOrdersBySubstringIdPaginated_EmptySearchId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.findOrdersBySubstringIdPaginated("", new PaginationRequest()));
        verify(orderFlow, never()).findOrdersBySubstringIdPaginated(any(), any());
    }
} 