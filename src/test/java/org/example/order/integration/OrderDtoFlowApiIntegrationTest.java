package org.example.order.integration;

import org.example.dto.OrderDto;
import org.example.flow.OrderFlow;
import org.example.api.OrderApi;
import org.example.dao.OrderDao;
import org.example.dao.OrderItemDao;
import org.example.api.InventoryApi;
import org.example.api.InvoiceApi;
import org.example.model.form.OrderForm;
import org.example.model.data.OrderData;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderItemPojo;
import org.example.model.enums.OrderStatus;
import org.example.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {OrderDtoFlowApiIntegrationTest.TestConfig.class})
class OrderDtoFlowApiIntegrationTest {

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {
        
        @Bean
        public DataSource dataSource() {
            // Create an in-memory H2 database for testing
            org.h2.jdbcx.JdbcDataSource dataSource = new org.h2.jdbcx.JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:order_testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            dataSource.setUser("sa");
            dataSource.setPassword("");
            return dataSource;
        }
        
        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(dataSource());
            em.setPackagesToScan("org.example.pojo");
            
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setGenerateDdl(true);
            vendorAdapter.setShowSql(true);
            em.setJpaVendorAdapter(vendorAdapter);
            
            Properties properties = new Properties();
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            properties.setProperty("hibernate.show_sql", "true");
            em.setJpaProperties(properties);
            
            em.setEntityManagerFactoryInterface(jakarta.persistence.EntityManagerFactory.class);
            
            return em;
        }
        
        @Bean
        public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(emf);
            return transactionManager;
        }
        
        @Bean
        @Primary
        public OrderDao orderDao() {
            return mock(OrderDao.class);
        }
        
        @Bean
        @Primary
        public OrderItemDao orderItemDao() {
            return mock(OrderItemDao.class);
        }
        

    }

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderItemDao orderItemDao;

    @Mock
    private org.example.api.ProductApi productApi;

    @Mock
    private org.example.api.ClientApi clientApi;

    @Mock
    private org.springframework.web.client.RestTemplate restTemplate;

    private InventoryApi inventoryApi;
    private InvoiceApi invoiceApi;

    private OrderDto orderDto;
    private OrderFlow orderFlow;
    private OrderApi orderApi;

    private OrderPojo testOrder;
    private OrderItemPojo testOrderItem;
    private OrderForm testForm;

    @BeforeEach
    void setUp() {
        // Create test data
        testOrder = new OrderPojo();
        testOrder.setId(1);
        testOrder.setDate(Instant.now());
        testOrder.setTotal(100.0);
        testOrder.setStatus(OrderStatus.CREATED);
        testOrder.setUserId("user123");

        testOrderItem = new OrderItemPojo();
        testOrderItem.setId(1);
        testOrderItem.setOrderId(1);
        testOrderItem.setProductId(1);
        testOrderItem.setQuantity(2);
        testOrderItem.setSellingPrice(50.0);
        testOrderItem.setAmount(100.0);

        testForm = new OrderForm();
        testForm.setTotal(100.0);
        testForm.setUserId("user123");

        // Create mocked APIs
        inventoryApi = mock(InventoryApi.class);
        invoiceApi = mock(InvoiceApi.class);

        // Create all objects manually
        orderApi = new OrderApi();
        orderFlow = new OrderFlow();
        orderDto = new OrderDto();

        // Inject mocked dependencies using reflection
        injectMockDependencies();
    }

    private void injectMockDependencies() {
        try {
            // Inject orderItemDao into orderDto
            Field orderItemDaoField = orderDto.getClass().getDeclaredField("orderItemDao");
            orderItemDaoField.setAccessible(true);
            orderItemDaoField.set(orderDto, orderItemDao);

            // Inject the dao field from AbstractApi into orderApi
            Field abstractDaoField = orderApi.getClass().getSuperclass().getDeclaredField("dao");
            abstractDaoField.setAccessible(true);
            abstractDaoField.set(orderApi, orderDao);

            // Inject orderItemDao into orderApi
            Field orderItemDaoApiField = orderApi.getClass().getDeclaredField("orderItemDao");
            orderItemDaoApiField.setAccessible(true);
            orderItemDaoApiField.set(orderApi, orderItemDao);

            // Inject inventoryApi into orderApi
            Field inventoryApiField = orderApi.getClass().getDeclaredField("inventoryApi");
            inventoryApiField.setAccessible(true);
            inventoryApiField.set(orderApi, inventoryApi);

            // Inject invoiceApi into orderApi
            Field invoiceApiField = orderApi.getClass().getDeclaredField("invoiceApi");
            invoiceApiField.setAccessible(true);
            invoiceApiField.set(orderApi, invoiceApi);

            // Inject orderApi into orderDto
            Field orderApiField = orderDto.getClass().getSuperclass().getDeclaredField("api");
            orderApiField.setAccessible(true);
            orderApiField.set(orderDto, orderApi);

            // Inject orderFlow into orderDto
            Field orderFlowField = orderDto.getClass().getDeclaredField("orderFlow");
            orderFlowField.setAccessible(true);
            orderFlowField.set(orderDto, orderFlow);

            // Inject productApi into orderDto
            Field productApiField = orderDto.getClass().getDeclaredField("productApi");
            productApiField.setAccessible(true);
            productApiField.set(orderDto, productApi);

            // Inject clientApi into orderDto
            Field clientApiField = orderDto.getClass().getDeclaredField("clientApi");
            clientApiField.setAccessible(true);
            clientApiField.set(orderDto, clientApi);

            // Inject restTemplate into orderDto
            Field restTemplateField = orderDto.getClass().getDeclaredField("restTemplate");
            restTemplateField.setAccessible(true);
            restTemplateField.set(orderDto, restTemplate);

            // Inject orderApi into orderFlow
            Field orderApiFlowField = orderFlow.getClass().getDeclaredField("api");
            orderApiFlowField.setAccessible(true);
            orderApiFlowField.set(orderFlow, orderApi);

        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    @Test
    void testDtoToFlowToApi_AddOrder() {
        // Arrange
        doNothing().when(orderDao).insert(any(OrderPojo.class));

        // Act
        OrderData result = orderDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getTotal());
        assertEquals("user123", result.getUserId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        
        // Verify DAO was called
        verify(orderDao).insert(any(OrderPojo.class));
    }

    @Test
    void testDtoToFlowToApi_GetOrder() {
        // Arrange
        when(orderDao.select(1)).thenReturn(testOrder);

        // Act
        OrderData result = orderDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(100.0, result.getTotal());
        assertEquals("user123", result.getUserId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        
        // Verify DAO was called
        verify(orderDao).select(1);
    }

    @Test
    void testDtoToFlowToApi_GetAllOrders() {
        // Arrange
        OrderPojo order2 = new OrderPojo();
        order2.setId(2);
        order2.setDate(Instant.now());
        order2.setTotal(200.0);
        order2.setStatus(OrderStatus.INVOICED);
        order2.setUserId("user456");
        
        when(orderDao.selectAll()).thenReturn(Arrays.asList(testOrder, order2));

        // Act
        List<OrderData> results = orderDto.getAll();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(100.0, results.get(0).getTotal());
        assertEquals(200.0, results.get(1).getTotal());
        
        // Verify DAO was called
        verify(orderDao).selectAll();
    }

    @Test
    void testDtoToFlowToApi_UpdateOrder() {
        // Arrange
        OrderForm updateForm = new OrderForm();
        updateForm.setTotal(150.0);
        updateForm.setUserId("user123");

        when(orderDao.select(1)).thenReturn(testOrder);
        doAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            OrderPojo updatedOrder = invocation.getArgument(1);
            testOrder.setTotal(updatedOrder.getTotal());
            return null;
        }).when(orderDao).update(eq(1), any(OrderPojo.class));

        // Act
        OrderData result = orderDto.update(1, updateForm);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getTotal());
        
        // Verify DAO was called
        verify(orderDao, times(2)).select(1);
        verify(orderDao).update(eq(1), any(OrderPojo.class));
    }

    @Test
    void testDtoToFlowToApi_CancelOrder() {
        // Arrange
        when(orderDao.select(1)).thenReturn(testOrder);
        when(orderItemDao.selectByOrderId(1)).thenReturn(Arrays.asList(testOrderItem));
        doNothing().when(inventoryApi).addStock(1, 2);
        doAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            OrderPojo updatedOrder = invocation.getArgument(1);
            testOrder.setStatus(updatedOrder.getStatus());
            return null;
        }).when(orderDao).update(eq(1), any(OrderPojo.class));

        // Act
        orderDto.cancelOrder(1);

        // Assert
        verify(orderDao).select(1);
        verify(orderItemDao).selectByOrderId(1);
        verify(inventoryApi).addStock(1, 2);
        verify(orderDao).update(eq(1), argThat(order -> order.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void testDtoToFlowToApi_UpdateStatus() {
        // Arrange
        when(orderDao.select(1)).thenReturn(testOrder);
        doAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            OrderPojo updatedOrder = invocation.getArgument(1);
            testOrder.setStatus(updatedOrder.getStatus());
            return null;
        }).when(orderDao).update(eq(1), any(OrderPojo.class));

        // Act
        orderFlow.updateStatus(1, OrderStatus.INVOICED);

        // Assert
        verify(orderDao).select(1);
        verify(orderDao).update(eq(1), argThat(order -> order.getStatus() == OrderStatus.INVOICED));
    }

    @Test
    void testDtoToFlowToApi_GetOrdersByDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        OrderPojo order2 = new OrderPojo();
        order2.setId(2);
        order2.setDate(Instant.now());
        order2.setTotal(200.0);
        order2.setStatus(OrderStatus.INVOICED);
        order2.setUserId("user456");
        
        when(orderDao.findOrdersByDateRange(startDate, endDate)).thenReturn(Arrays.asList(testOrder, order2));

        // Act
        List<OrderData> results = orderDto.getOrdersByDateRange(startDate, endDate);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(100.0, results.get(0).getTotal());
        assertEquals(200.0, results.get(1).getTotal());
        
        // Verify DAO was called
        verify(orderDao).findOrdersByDateRange(startDate, endDate);
    }

    @Test
    void testDtoToFlowToApi_FindByUserId() {
        // Arrange
        OrderPojo order2 = new OrderPojo();
        order2.setId(2);
        order2.setDate(Instant.now());
        order2.setTotal(200.0);
        order2.setStatus(OrderStatus.INVOICED);
        order2.setUserId("user123");
        
        when(orderDao.findByUserId("user123")).thenReturn(Arrays.asList(testOrder, order2));

        // Act
        List<OrderData> results = orderDto.getOrdersByUserId("user123");

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("user123", results.get(0).getUserId());
        assertEquals("user123", results.get(1).getUserId());
        
        // Verify DAO was called
        verify(orderDao).findByUserId("user123");
    }

    @Test
    void testDtoToFlowToApi_FindOrdersBySubstringId() {
        // Arrange
        when(orderDao.findOrdersBySubstringId("1", 10)).thenReturn(Arrays.asList(testOrder));

        // Act
        List<OrderData> results = orderDto.findOrdersBySubstringId("1", 10);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getId());
        
        // Verify DAO was called
        verify(orderDao).findOrdersBySubstringId("1", 10);
    }

    @Test
    void testDtoToFlowToApi_CountOrdersBySubstringId() {
        // Arrange
        when(orderDao.countOrdersBySubstringId("1")).thenReturn(5L);

        // Act
        long result = orderDto.countOrdersBySubstringId("1");

        // Assert
        assertEquals(5L, result);
        
        // Verify DAO was called
        verify(orderDao).countOrdersBySubstringId("1");
    }

    @Test
    void testDtoToFlowToApi_Validation_OrderNotFound() {
        // Arrange
        when(orderDao.select(999)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.get(999);
        });
        
        assertEquals("Order with ID 999 not found", exception.getMessage());
        
        // Verify DAO was called
        verify(orderDao).select(999);
    }

    @Test
    void testDtoToFlowToApi_Validation_CancelOrderNotFound() {
        // Arrange
        when(orderDao.select(999)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.cancelOrder(999);
        });
        
        assertEquals("Order not found", exception.getMessage());
        
        // Verify DAO was called
        verify(orderDao).select(999);
        verify(orderItemDao, never()).selectByOrderId(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_CancelOrderAlreadyCancelled() {
        // Arrange
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderDao.select(1)).thenReturn(testOrder);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.cancelOrder(1);
        });
        
        assertEquals("Order is already cancelled", exception.getMessage());
        
        // Verify DAO was called
        verify(orderDao).select(1);
        verify(orderItemDao, never()).selectByOrderId(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_UpdateStatusOrderNotFound() {
        // Arrange
        when(orderDao.select(999)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderFlow.updateStatus(999, OrderStatus.INVOICED);
        });
        
        assertEquals("Order not found", exception.getMessage());
        
        // Verify DAO was called
        verify(orderDao).select(999);
        verify(orderDao, never()).update(any(), any());
    }

    @Test
    void testDtoToFlowToApi_Validation_DateRangeInvalid() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(7);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.getOrdersByDateRange(startDate, endDate);
        });
        
        assertEquals("End date cannot be before start date", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderDao, never()).findOrdersByDateRange(any(), any());
    }

    @Test
    void testDtoToFlowToApi_Validation_SubstringIdNull() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.findOrdersBySubstringId(null, 10);
        });
        
        assertEquals("Search ID cannot be null or empty", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderDao, never()).findOrdersBySubstringId(any(), anyInt());
    }

    @Test
    void testDtoToFlowToApi_Validation_SubstringIdEmpty() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.findOrdersBySubstringId("", 10);
        });
        
        assertEquals("Search ID cannot be null or empty", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderDao, never()).findOrdersBySubstringId(any(), anyInt());
    }

    @Test
    void testDtoToFlowToApi_Validation_SubstringIdInvalidMaxResults() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.findOrdersBySubstringId("1", 0);
        });
        
        assertEquals("Max results must be positive", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderDao, never()).findOrdersBySubstringId(any(), anyInt());
    }

    @Test
    void testDtoToFlowToApi_Validation_CountSubstringIdNull() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.countOrdersBySubstringId(null);
        });
        
        assertEquals("Search ID cannot be null or empty", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderDao, never()).countOrdersBySubstringId(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_CountSubstringIdEmpty() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.countOrdersBySubstringId("");
        });
        
        assertEquals("Search ID cannot be null or empty", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderDao, never()).countOrdersBySubstringId(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_DateRangeNullStartDate() {
        // Arrange
        LocalDate endDate = LocalDate.now();

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.getOrdersByDateRange(null, endDate);
        });
        
        assertEquals("Start date and end date cannot be null", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderDao, never()).findOrdersByDateRange(any(), any());
    }

    @Test
    void testDtoToFlowToApi_Validation_DateRangeNullEndDate() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.getOrdersByDateRange(startDate, null);
        });
        
        assertEquals("Start date and end date cannot be null", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderDao, never()).findOrdersByDateRange(any(), any());
    }
} 