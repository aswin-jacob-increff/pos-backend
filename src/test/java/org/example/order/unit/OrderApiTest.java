package org.example.order.unit;

import org.example.api.OrderApi;
import org.example.api.InventoryApi;
import org.example.api.InvoiceApi;
import org.example.dao.OrderDao;
import org.example.dao.OrderItemDao;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderItemPojo;
import org.example.model.enums.OrderStatus;
import org.example.exception.ApiException;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.model.data.PaginationResponse;
import org.example.pojo.InventoryPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApiTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderItemDao orderItemDao;

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private InvoiceApi invoiceApi;

    @InjectMocks
    private OrderApi orderApi;

    private OrderPojo testOrder;
    private OrderItemPojo testOrderItem;
    private InventoryPojo testInventory;

    @BeforeEach
    void setUp() throws Exception {
        testOrder = new OrderPojo();
        testOrder.setId(1);
        testOrder.setUserId("user123");
        testOrder.setStatus(OrderStatus.CREATED);
        testOrder.setDate(Instant.now());
        testOrder.setTotal(100.0);

        testOrderItem = new OrderItemPojo();
        testOrderItem.setId(1);
        testOrderItem.setOrderId(1);
        testOrderItem.setProductId(1);
        testOrderItem.setQuantity(2);
        testOrderItem.setSellingPrice(50.0);
        testOrderItem.setAmount(100.0);

        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1);
        testInventory.setQuantity(10);

        // Manually inject dependencies using reflection
        Field orderItemDaoField = orderApi.getClass().getDeclaredField("orderItemDao");
        orderItemDaoField.setAccessible(true);
        orderItemDaoField.set(orderApi, orderItemDao);

        Field inventoryApiField = orderApi.getClass().getDeclaredField("inventoryApi");
        inventoryApiField.setAccessible(true);
        inventoryApiField.set(orderApi, inventoryApi);

        Field invoiceApiField = orderApi.getClass().getDeclaredField("invoiceApi");
        invoiceApiField.setAccessible(true);
        invoiceApiField.set(orderApi, invoiceApi);

        // Inject the dao field from AbstractApi
        Field abstractDaoField = orderApi.getClass().getSuperclass().getDeclaredField("dao");
        abstractDaoField.setAccessible(true);
        abstractDaoField.set(orderApi, orderDao);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(orderDao).insert(any(OrderPojo.class));

        // Act
        orderApi.add(testOrder);

        // Assert
        verify(orderDao).insert(testOrder);
        assertEquals(OrderStatus.CREATED, testOrder.getStatus());
        assertNotNull(testOrder.getDate());
    }

    @Test
    void testAdd_NullOrder() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.add(null));
        verify(orderDao, never()).insert(any());
    }

    @Test
    void testAdd_WithExistingDate() {
        // Arrange
        Instant existingDate = Instant.now().minusSeconds(3600); // 1 hour ago
        testOrder.setDate(existingDate);
        doNothing().when(orderDao).insert(any(OrderPojo.class));

        // Act
        orderApi.add(testOrder);

        // Assert
        verify(orderDao).insert(testOrder);
        assertEquals(existingDate, testOrder.getDate()); // Date should not be changed
        assertEquals(OrderStatus.CREATED, testOrder.getStatus());
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
        assertEquals(testOrder.getTotal(), result.getTotal());
        assertEquals(testOrder.getStatus(), result.getStatus());
        verify(orderDao).select(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.get(null));
        verify(orderDao, never()).select(any());
    }

    @Test
    void testGet_NotFound() {
        // Arrange
        when(orderDao.select(999)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.get(999));
        verify(orderDao).select(999);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        OrderPojo updatedOrder = new OrderPojo();
        updatedOrder.setId(1);
        updatedOrder.setTotal(150.0);
        updatedOrder.setStatus(OrderStatus.INVOICED);

        doNothing().when(orderDao).update(eq(1), any(OrderPojo.class));

        // Act
        orderApi.update(1, updatedOrder);

        // Assert
        verify(orderDao).update(1, updatedOrder);
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.update(null, testOrder));
        verify(orderDao, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullOrder() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.update(1, null));
        verify(orderDao, never()).update(any(), any());
    }



    @Test
    void testGenerateInvoice_ThrowsException() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.generateInvoice(1));
    }

    @Test
    void testUpdateStatus_Success() {
        // Arrange
        when(orderDao.select(1)).thenReturn(testOrder);
        doNothing().when(orderDao).update(any(Integer.class), any(OrderPojo.class));

        // Act
        orderApi.updateStatus(1, OrderStatus.INVOICED);

        // Assert
        verify(orderDao).select(1);
        verify(orderDao).update(eq(1), argThat(order -> order.getStatus() == OrderStatus.INVOICED));
    }

    @Test
    void testUpdateStatus_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.updateStatus(null, OrderStatus.INVOICED));
        verify(orderDao, never()).select(any());
    }

    @Test
    void testUpdateStatus_NullStatus() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.updateStatus(1, null));
        verify(orderDao, never()).select(any());
    }

    @Test
    void testUpdateStatus_OrderNotFound() {
        // Arrange
        when(orderDao.select(999)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.updateStatus(999, OrderStatus.INVOICED));
        verify(orderDao).select(999);
        verify(orderDao, never()).update(any(), any());
    }

    @Test
    void testGetOrdersByDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<OrderPojo> orders = Arrays.asList(testOrder);
        when(orderDao.findOrdersByDateRange(startDate, endDate)).thenReturn(orders);

        // Act
        List<OrderPojo> result = orderApi.getOrdersByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
        verify(orderDao).findOrdersByDateRange(startDate, endDate);
    }

    @Test
    void testGetOrdersByDateRange_NullStartDate() {
        // Arrange
        LocalDate endDate = LocalDate.now();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.getOrdersByDateRange(null, endDate));
        verify(orderDao, never()).findOrdersByDateRange(any(), any());
    }

    @Test
    void testGetOrdersByDateRange_NullEndDate() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.getOrdersByDateRange(startDate, null));
        verify(orderDao, never()).findOrdersByDateRange(any(), any());
    }

    @Test
    void testGetOrdersByDateRange_EndDateBeforeStartDate() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(7);

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.getOrdersByDateRange(startDate, endDate));
        verify(orderDao, never()).findOrdersByDateRange(any(), any());
    }

    @Test
    void testFindByUserId_Success() {
        // Arrange
        List<OrderPojo> orders = Arrays.asList(testOrder);
        when(orderDao.findByUserId("user123")).thenReturn(orders);

        // Act
        List<OrderPojo> result = orderApi.findByUserId("user123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
        verify(orderDao).findByUserId("user123");
    }

    @Test
    void testFindOrdersBySubstringId_Success() {
        // Arrange
        List<OrderPojo> orders = Arrays.asList(testOrder);
        when(orderDao.findOrdersBySubstringId("1", 10)).thenReturn(orders);

        // Act
        List<OrderPojo> result = orderApi.findOrdersBySubstringId("1", 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
        verify(orderDao).findOrdersBySubstringId("1", 10);
    }

    @Test
    void testFindOrdersBySubstringId_NullSearchId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.findOrdersBySubstringId(null, 10));
        verify(orderDao, never()).findOrdersBySubstringId(any(), anyInt());
    }

    @Test
    void testFindOrdersBySubstringId_EmptySearchId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.findOrdersBySubstringId("", 10));
        verify(orderDao, never()).findOrdersBySubstringId(any(), anyInt());
    }

    @Test
    void testFindOrdersBySubstringId_InvalidMaxResults() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.findOrdersBySubstringId("1", 0));
        assertThrows(ApiException.class, () -> orderApi.findOrdersBySubstringId("1", -1));
        verify(orderDao, never()).findOrdersBySubstringId(any(), anyInt());
    }

    @Test
    void testFindOrdersBySubstringIdPaginated_Success() {
        // Arrange
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<OrderPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testOrder));
        expectedResponse.setTotalElements(1L);
        
        when(orderDao.findOrdersBySubstringIdPaginated("1", request)).thenReturn(expectedResponse);

        // Act
        PaginationResponse<OrderPojo> result = orderApi.findOrdersBySubstringIdPaginated("1", request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        verify(orderDao).findOrdersBySubstringIdPaginated("1", request);
    }

    @Test
    void testFindOrdersBySubstringIdPaginated_NullSearchId() {
        // Arrange
        PaginationRequest request = new PaginationRequest();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.findOrdersBySubstringIdPaginated(null, request));
        verify(orderDao, never()).findOrdersBySubstringIdPaginated(any(), any());
    }

    @Test
    void testFindOrdersBySubstringIdPaginated_EmptySearchId() {
        // Arrange
        PaginationRequest request = new PaginationRequest();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.findOrdersBySubstringIdPaginated("", request));
        verify(orderDao, never()).findOrdersBySubstringIdPaginated(any(), any());
    }

    @Test
    void testFindOrdersBySubstringIdPaginated_NullRequest() {
        // Arrange
        PaginationResponse<OrderPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testOrder));
        expectedResponse.setTotalElements(1L);
        when(orderDao.findOrdersBySubstringIdPaginated(eq("1"), any(PaginationRequest.class))).thenReturn(expectedResponse);

        // Act
        PaginationResponse<OrderPojo> result = orderApi.findOrdersBySubstringIdPaginated("1", null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        verify(orderDao).findOrdersBySubstringIdPaginated(eq("1"), any(PaginationRequest.class));
    }

    @Test
    void testCountOrdersBySubstringId_Success() {
        // Arrange
        when(orderDao.countOrdersBySubstringId("1")).thenReturn(5L);

        // Act
        long result = orderApi.countOrdersBySubstringId("1");

        // Assert
        assertEquals(5L, result);
        verify(orderDao).countOrdersBySubstringId("1");
    }

    @Test
    void testCountOrdersBySubstringId_NullSearchId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.countOrdersBySubstringId(null));
        verify(orderDao, never()).countOrdersBySubstringId(any());
    }

    @Test
    void testCountOrdersBySubstringId_EmptySearchId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.countOrdersBySubstringId(""));
        verify(orderDao, never()).countOrdersBySubstringId(any());
    }

    // ========== PAGINATION TESTS ==========

    @Test
    void testGetAllPaginated_Success() {
        // Arrange
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<OrderPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testOrder));
        expectedResponse.setTotalElements(1L);
        
        when(orderDao.getPaginated(any(PaginationQuery.class))).thenReturn(expectedResponse);

        // Act
        PaginationResponse<OrderPojo> result = orderApi.getAllPaginated(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        verify(orderDao).getPaginated(any(PaginationQuery.class));
    }

    @Test
    void testGetByUserIdPaginated_Success() {
        // Arrange
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<OrderPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testOrder));
        expectedResponse.setTotalElements(1L);
        
        when(orderDao.getByUserIdPaginated("user123", request)).thenReturn(expectedResponse);

        // Act
        PaginationResponse<OrderPojo> result = orderApi.getByUserIdPaginated("user123", request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        verify(orderDao).getByUserIdPaginated("user123", request);
    }

    @Test
    void testGetByUserIdPaginated_NullUserId() {
        // Arrange
        PaginationRequest request = new PaginationRequest();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.getByUserIdPaginated(null, request));
        verify(orderDao, never()).getByUserIdPaginated(any(), any());
    }

    @Test
    void testGetByUserIdPaginated_EmptyUserId() {
        // Arrange
        PaginationRequest request = new PaginationRequest();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.getByUserIdPaginated("", request));
        verify(orderDao, never()).getByUserIdPaginated(any(), any());
    }

    @Test
    void testGetByDateRangePaginated_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<OrderPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testOrder));
        expectedResponse.setTotalElements(1L);
        
        when(orderDao.getByDateRangePaginated(startDate, endDate, request)).thenReturn(expectedResponse);

        // Act
        PaginationResponse<OrderPojo> result = orderApi.getByDateRangePaginated(startDate, endDate, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        verify(orderDao).getByDateRangePaginated(startDate, endDate, request);
    }

    @Test
    void testGetByDateRangePaginated_NullStartDate() {
        // Arrange
        LocalDate endDate = LocalDate.now();
        PaginationRequest request = new PaginationRequest();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.getByDateRangePaginated(null, endDate, request));
        verify(orderDao, never()).getByDateRangePaginated(any(), any(), any());
    }

    @Test
    void testGetByDateRangePaginated_NullEndDate() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        PaginationRequest request = new PaginationRequest();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.getByDateRangePaginated(startDate, null, request));
        verify(orderDao, never()).getByDateRangePaginated(any(), any(), any());
    }

    @Test
    void testGetByDateRangePaginated_EndDateBeforeStartDate() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(7);
        PaginationRequest request = new PaginationRequest();

        // Act & Assert
        assertThrows(ApiException.class, () -> orderApi.getByDateRangePaginated(startDate, endDate, request));
        verify(orderDao, never()).getByDateRangePaginated(any(), any(), any());
    }
} 