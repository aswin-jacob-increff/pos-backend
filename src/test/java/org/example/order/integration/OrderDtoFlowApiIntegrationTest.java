package org.example.order.integration;

import org.example.dto.OrderDto;
import org.example.dto.OrderItemDto;
import org.example.flow.OrderFlow;
import org.example.api.OrderApi;
import org.example.api.OrderItemApi;
import org.example.api.InventoryApi;
import org.example.api.ClientApi;
import org.example.api.ProductApi;
import org.example.api.InvoiceApi;
import org.example.dao.OrderDao;
import org.example.dao.OrderItemDao;
import org.example.dao.InventoryDao;
import org.example.dao.ClientDao;
import org.example.dao.ProductDao;
import org.example.dao.InvoiceDao;
import org.example.flow.ProductFlow;
import org.example.flow.OrderItemFlow;
import org.example.model.enums.OrderStatus;
import org.example.model.form.OrderForm;
import org.example.model.data.OrderData;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderItemPojo;
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
import org.springframework.web.client.RestTemplate;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.example.pojo.ProductPojo;

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
            dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
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
        
        @Bean
        @Primary
        public InventoryDao inventoryDao() {
            return mock(InventoryDao.class);
        }
        
        @Bean
        @Primary
        public OrderItemApi orderItemApi() {
            return mock(OrderItemApi.class);
        }
        
        @Bean
        @Primary
        public InventoryApi inventoryApi() {
            return mock(InventoryApi.class);
        }
        
        @Bean
        @Primary
        public ClientApi clientApi() {
            return mock(ClientApi.class);
        }
        
        @Bean
        @Primary
        public ClientDao clientDao() {
            return mock(ClientDao.class);
        }
        
        @Bean
        @Primary
        public ProductApi productApi() {
            return mock(ProductApi.class);
        }
        
        @Bean
        @Primary
        public ProductDao productDao() {
            return mock(ProductDao.class);
        }
        
        @Bean
        @Primary
        public InvoiceApi invoiceApi() {
            return mock(InvoiceApi.class);
        }
        
        @Bean
        @Primary
        public InvoiceDao invoiceDao() {
            return mock(InvoiceDao.class);
        }
        
        @Bean
        public OrderApi orderApi() {
            return new OrderApi();
        }
        
        @Bean
        public OrderFlow orderFlow() {
            return new OrderFlow();
        }
        
        @Bean
        public OrderDto orderDto() {
            return new OrderDto();
        }

        @Bean
        @Primary
        public ProductFlow productFlow() {
            return mock(ProductFlow.class);
        }

        @Bean
        @Primary
        public OrderItemFlow orderItemFlow() {
            return mock(OrderItemFlow.class);
        }

        @Bean
        @Primary
        public RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }

        @Bean
        @Primary
        public OrderItemDto orderItemDto() {
            return mock(OrderItemDto.class);
        }
    }

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderItemApi orderItemApi;

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private OrderItemDto orderItemDto;

    @Mock
    private OrderItemFlow orderItemFlow;

    private OrderDto orderDto;
    private OrderFlow orderFlow;
    private OrderApi orderApi;

    private OrderPojo testOrder;
    private OrderForm testForm;
    private OrderItemPojo testOrderItem;

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
        testOrderItem.setProductBarcode("TEST123");
        testOrderItem.setProductName("Test Product");
        testOrderItem.setQuantity(2);
        testOrderItem.setSellingPrice(50.0);
        testOrderItem.setAmount(100.0);

        testForm = new OrderForm();
        testForm.setDate(LocalDateTime.now());
        testForm.setUserId("user123");

        // Get beans from Spring context
        orderDto = new OrderDto();
        orderFlow = new OrderFlow();
        orderApi = new OrderApi();
        orderItemDto = new OrderItemDto();
        orderItemFlow = new OrderItemFlow();

        // Inject mocked dependencies using reflection
        injectMockDependencies();
    }

    private void injectMockDependencies() {
        try {
            // Inject orderDao into orderApi
            var orderDaoField = OrderApi.class.getDeclaredField("orderDao");
            orderDaoField.setAccessible(true);
            orderDaoField.set(orderApi, orderDao);

            // Inject orderDao into AbstractApi's 'dao' field
            var abstractDaoField = org.example.api.AbstractApi.class.getDeclaredField("dao");
            abstractDaoField.setAccessible(true);
            abstractDaoField.set(orderApi, orderDao);

            var orderItemApiField = OrderApi.class.getDeclaredField("orderItemApi");
            orderItemApiField.setAccessible(true);
            orderItemApiField.set(orderApi, orderItemApi);

            var inventoryApiField = OrderApi.class.getDeclaredField("inventoryApi");
            inventoryApiField.setAccessible(true);
            inventoryApiField.set(orderApi, inventoryApi);

            var productApiField = OrderApi.class.getDeclaredField("productApi");
            productApiField.setAccessible(true);
            productApiField.set(orderApi, productApi);

            // Inject orderApi into orderFlow
            var apiField = OrderFlow.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(orderFlow, orderApi);

            var orderItemApiFlowField = OrderFlow.class.getDeclaredField("orderItemApi");
            orderItemApiFlowField.setAccessible(true);
            orderItemApiFlowField.set(orderFlow, orderItemApi);

            // Inject orderFlow into orderDto
            var flowField = OrderDto.class.getDeclaredField("orderFlow");
            flowField.setAccessible(true);
            flowField.set(orderDto, orderFlow);

            var orderItemDtoField = OrderDto.class.getDeclaredField("orderItemDto");
            orderItemDtoField.setAccessible(true);
            orderItemDtoField.set(orderDto, orderItemDto);

            var orderItemFlowField = OrderItemDto.class.getDeclaredField("orderItemFlow");
            orderItemFlowField.setAccessible(true);
            orderItemFlowField.set(orderItemDto, orderItemFlow);

            var orderItemFlowApiField = OrderItemFlow.class.getDeclaredField("api");
            orderItemFlowApiField.setAccessible(true);
            orderItemFlowApiField.set(orderItemFlow, orderItemApi);

            // Inject orderApi into orderDto (inherited from AbstractDto)
            var abstractApiField = org.example.dto.AbstractDto.class.getDeclaredField("api");
            abstractApiField.setAccessible(true);
            abstractApiField.set(orderDto, orderApi);

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
        assertEquals("user123", result.getUserId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        assertEquals(0.0, result.getTotal()); // Initial total is 0
        
        // Verify DAO was called
        verify(orderDao).insert(any(OrderPojo.class));
    }

    @Test
    void testDtoToFlowToApi_GetOrder() {
        // Arrange
        when(orderDao.select(1)).thenReturn(testOrder);
        when(orderItemApi.getByOrderId(1)).thenReturn(Arrays.asList(testOrderItem));

        // Act
        OrderData result = orderDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals(100.0, result.getTotal());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        
        // Verify DAO was called
        verify(orderDao).select(1);
        verify(orderItemApi).getByOrderId(1);
    }

    @Test
    void testDtoToFlowToApi_GetAllOrders() {
        // Arrange
        OrderPojo order2 = new OrderPojo();
        order2.setId(2);
        order2.setDate(Instant.now());
        order2.setTotal(150.0);
        order2.setStatus(OrderStatus.CREATED);
        order2.setUserId("user456");
        
        when(orderDao.selectAll()).thenReturn(Arrays.asList(testOrder, order2));

        // Act
        List<OrderData> results = orderDto.getAll();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("user123", results.get(0).getUserId());
        assertEquals("user456", results.get(1).getUserId());
        
        // Verify DAO was called
        verify(orderDao).selectAll();
    }

    @Test
    void testDtoToFlowToApi_UpdateOrder() {
        // Arrange
        OrderForm updateForm = new OrderForm();
        updateForm.setDate(LocalDateTime.now());
        updateForm.setUserId("updatedUser");

        when(orderDao.select(1)).thenReturn(testOrder);
        doAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            OrderPojo updatedOrder = invocation.getArgument(1);
            testOrder.setUserId(updatedOrder.getUserId());
            return null;
        }).when(orderDao).update(eq(1), any(OrderPojo.class));

        // Act
        OrderData result = orderDto.update(1, updateForm);

        // Assert
        assertNotNull(result);
        assertEquals("updatedUser", result.getUserId());
        
        // Verify DAO was called
        verify(orderDao, times(2)).select(1);
        verify(orderDao).update(eq(1), any(OrderPojo.class));
    }

    @Test
    void testDtoToFlowToApi_CancelOrder() {
        // Arrange
        when(orderDao.select(1)).thenReturn(testOrder);
        when(orderItemApi.getByOrderId(1)).thenReturn(Arrays.asList(testOrderItem));
        
        // Mock product lookup by barcode
        ProductPojo testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setBarcode("TEST123");
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct);
        
        doNothing().when(inventoryApi).addStock(1, 2); // Use productId instead of barcode
        doNothing().when(orderDao).update(eq(1), any(OrderPojo.class));

        // Act
        orderDto.cancelOrder(1);

        // Assert
        verify(orderDao).select(1);
        verify(orderItemApi).getByOrderId(1);
        verify(productApi).getByBarcode("TEST123");
        verify(inventoryApi).addStock(1, 2); // Use productId instead of barcode
        verify(orderDao).update(eq(1), any(OrderPojo.class));
    }

    @Test
    void testDtoToFlowToApi_UpdateOrderStatus() {
        // Arrange
        when(orderDao.select(1)).thenReturn(testOrder);
        doNothing().when(orderDao).update(eq(1), any(OrderPojo.class));

        // Act
        orderFlow.updateStatus(1, OrderStatus.INVOICED);

        // Assert
        verify(orderDao).select(1);
        verify(orderDao).update(eq(1), any(OrderPojo.class));
    }

    @Test
    void testDtoToFlowToApi_Validation_NullOrderId() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderDto.cancelOrder(null);
        });
        
        assertEquals("Order ID cannot be null", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderDao, never()).select(any());
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
        
        assertEquals("Order not found", exception.getMessage()); // Updated error message
        
        // Verify DAO was called for validation but not for update
        verify(orderDao).select(999);
        verify(orderDao, never()).update(anyInt(), any());
    }
} 