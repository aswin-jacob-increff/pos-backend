package org.example.orderitem.integration;

import org.example.dto.OrderItemDto;
import org.example.flow.OrderItemFlow;
import org.example.api.OrderItemApi;
import org.example.dao.OrderItemDao;
import org.example.dao.OrderDao;
import org.example.dao.InventoryDao;
import org.example.api.OrderApi;
import org.example.api.ProductApi;
import org.example.api.InventoryApi;
import org.example.api.ClientApi;
import org.example.api.InvoiceApi;
import org.example.dao.ClientDao;
import org.example.dao.ProductDao;
import org.example.dao.InvoiceDao;
import org.example.model.enums.OrderStatus;
import org.example.model.form.OrderItemForm;
import org.example.model.data.OrderItemData;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.InventoryPojo;
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

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {OrderItemDtoFlowApiIntegrationTest.TestConfig.class})
class OrderItemDtoFlowApiIntegrationTest {

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
        public OrderItemDao orderItemDao() {
            return mock(OrderItemDao.class);
        }
        
        @Bean
        @Primary
        public OrderDao orderDao() {
            return mock(OrderDao.class);
        }
        
        @Bean
        @Primary
        public OrderApi orderApi() {
            return mock(OrderApi.class);
        }
        
        @Bean
        @Primary
        public ProductApi productApi() {
            return mock(ProductApi.class);
        }
        
        @Bean
        @Primary
        public InventoryApi inventoryApi() {
            return mock(InventoryApi.class);
        }
        
        @Bean
        @Primary
        public InventoryDao inventoryDao() {
            return mock(InventoryDao.class);
        }

        @Bean
        @Primary
        public ClientDao clientDao() {
            return mock(ClientDao.class);
        }

        @Bean
        @Primary
        public ClientApi clientApi() {
            return mock(ClientApi.class);
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
        public OrderItemApi orderItemApi() {
            return new OrderItemApi();
        }
        
        @Bean
        public OrderItemFlow orderItemFlow() {
            return new OrderItemFlow();
        }
        
        @Bean
        public OrderItemDto orderItemDto() {
            return new OrderItemDto();
        }
    }

    @Mock
    private OrderItemDao orderItemDao;

    @Mock
    private OrderApi orderApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private InventoryApi inventoryApi;

    private OrderItemDto orderItemDto;
    private OrderItemFlow orderItemFlow;
    private OrderItemApi orderItemApi;

    private OrderItemPojo testOrderItem;
    private OrderItemForm testForm;
    private OrderPojo testOrder;
    private ProductPojo testProduct;
    private InventoryPojo testInventory;

    @BeforeEach
    void setUp() {
        // Create test order item
        OrderItemPojo orderItem = new OrderItemPojo();
        orderItem.setId(1);
        orderItem.setOrderId(1);
        orderItem.setProductId(1);
        orderItem.setQuantity(5);
        orderItem.setSellingPrice(100.0);
        orderItem.setAmount(500.0);

        // Create test order
        testOrder = new OrderPojo();
        testOrder.setId(1);
        testOrder.setDate(Instant.now());
        testOrder.setTotal(100.0);
        testOrder.setStatus(OrderStatus.CREATED);
        testOrder.setUserId("user123");

        // Create test product
        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setClientId(1);
        testProduct.setMrp(50.0);
        testProduct.setImageUrl("http://example.com/image.jpg");

        // Create test inventory
        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1); // Use productId instead of productBarcode
        testInventory.setQuantity(100);

        // Get beans from Spring context
        orderItemDto = new OrderItemDto();
        orderItemFlow = new OrderItemFlow();
        orderItemApi = new OrderItemApi();

        // Inject mocked dependencies using reflection
        injectMockDependencies();
    }

    private void injectMockDependencies() {
        try {
            // Inject orderItemDao into orderItemApi
            var orderItemDaoField = OrderItemApi.class.getDeclaredField("orderItemDao");
            orderItemDaoField.setAccessible(true);
            orderItemDaoField.set(orderItemApi, orderItemDao);

            // Inject orderItemDao into AbstractApi's 'dao' field
            var abstractDaoField = org.example.api.AbstractApi.class.getDeclaredField("dao");
            abstractDaoField.setAccessible(true);
            abstractDaoField.set(orderItemApi, orderItemDao);

            var inventoryApiField = OrderItemApi.class.getDeclaredField("inventoryApi");
            inventoryApiField.setAccessible(true);
            inventoryApiField.set(orderItemApi, inventoryApi);

            var productApiField = OrderItemApi.class.getDeclaredField("productApi");
            productApiField.setAccessible(true);
            productApiField.set(orderItemApi, productApi);

            // Inject orderItemApi into orderItemFlow
            var apiField = OrderItemFlow.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(orderItemFlow, orderItemApi);

            // Inject orderItemFlow into orderItemDto
            var flowField = OrderItemDto.class.getDeclaredField("orderItemFlow");
            flowField.setAccessible(true);
            flowField.set(orderItemDto, orderItemFlow);

            var orderApiDtoField = OrderItemDto.class.getDeclaredField("orderApi");
            orderApiDtoField.setAccessible(true);
            orderApiDtoField.set(orderItemDto, orderApi);

            var productApiDtoField = OrderItemDto.class.getDeclaredField("productApi");
            productApiDtoField.setAccessible(true);
            productApiDtoField.set(orderItemDto, productApi);

            // Inject orderItemApi into orderItemDto (inherited from AbstractDto)
            var abstractApiField = org.example.dto.AbstractDto.class.getDeclaredField("api");
            abstractApiField.setAccessible(true);
            abstractApiField.set(orderItemDto, orderItemApi);

        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    @Test
    void testDtoToFlowToApi_AddOrderItem() {
        // Arrange
        when(productApi.get(1)).thenReturn(testProduct);
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct); // Add this mock
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory); // Use productId instead of barcode
        doNothing().when(inventoryApi).removeStock(1, 2); // Use productId instead of barcode
        doNothing().when(orderItemDao).insert(any(OrderItemPojo.class));

        // Act
        OrderItemData result = orderItemDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(2, result.getQuantity());
        assertEquals(50.0, result.getSellingPrice());
        assertEquals(100.0, result.getAmount());
        
        // Verify DAO was called
        verify(productApi, times(3)).get(1);
        verify(productApi, times(2)).getByBarcode("TEST123"); // Updated to 2 times
        verify(inventoryApi).getByProductId(1); // Use productId instead of barcode
        verify(inventoryApi).removeStock(1, 2); // Use productId instead of barcode
        verify(orderItemDao).insert(any(OrderItemPojo.class));
    }

    @Test
    void testDtoToFlowToApi_GetOrderItem() {
        // Arrange
        when(orderItemDao.select(1)).thenReturn(testOrderItem);
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct);
        when(orderApi.get(1)).thenReturn(testOrder);

        // Act
        OrderItemData result = orderItemDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getOrderId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(2, result.getQuantity());
        
        // Verify DAO was called
        verify(orderItemDao).select(1);
        verify(productApi).getByBarcode("TEST123");
        verify(orderApi).get(1);
    }

    @Test
    void testDtoToFlowToApi_GetAllOrderItems() {
        // Arrange
        OrderItemPojo orderItem2 = new OrderItemPojo();
        orderItem2.setId(2);
        orderItem2.setOrderId(1);
        orderItem2.setProductId(2); // Changed to productId
        orderItem2.setQuantity(1);
        orderItem2.setSellingPrice(75.0);
        orderItem2.setAmount(75.0);
        
        when(orderItemDao.selectAll()).thenReturn(Arrays.asList(testOrderItem, orderItem2));

        // Act
        List<OrderItemData> results = orderItemDto.getAll();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("TEST123", results.get(0).getBarcode());
        assertEquals("TEST456", results.get(1).getBarcode());
        
        // Verify DAO was called
        verify(orderItemDao).selectAll();
    }

    @Test
    void testDtoToFlowToApi_UpdateOrderItem() {
        // Arrange
        OrderItemForm updateForm = new OrderItemForm();
        updateForm.setOrderId(1);
        updateForm.setProductId(1);
        updateForm.setQuantity(3);

        when(orderItemDao.select(1)).thenReturn(testOrderItem);
        when(productApi.get(1)).thenReturn(testProduct);
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct); // Add this mock
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory); // Use productId instead of barcode
        doNothing().when(inventoryApi).addStock(1, 2); // Use productId instead of barcode
        doNothing().when(inventoryApi).removeStock(1, 3); // Use productId instead of barcode
        doNothing().when(orderItemDao).update(eq(1), any(OrderItemPojo.class));

        // Act
        OrderItemData result = orderItemDto.update(1, updateForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals(3, result.getQuantity());
        
        // Verify DAO was called
        verify(orderItemDao, times(2)).select(1);
        verify(productApi, times(3)).get(1);
        verify(productApi, times(2)).getByBarcode("TEST123"); // Updated to 2 times
        verify(inventoryApi).getByProductId(1); // Use productId instead of barcode
        verify(inventoryApi).addStock(1, 2); // Use productId instead of barcode
        verify(inventoryApi).removeStock(1, 3); // Use productId instead of barcode
        verify(orderItemDao).update(eq(1), any(OrderItemPojo.class));
    }

    @Test
    void testDtoToFlowToApi_GetByOrderId() {
        // Arrange
        when(orderItemDao.selectByOrderId(1)).thenReturn(Arrays.asList(testOrderItem));
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct);
        when(orderApi.get(1)).thenReturn(testOrder);

        // Act
        List<OrderItemData> results = orderItemDto.getByOrderId(1);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getOrderId());
        assertEquals("TEST123", results.get(0).getBarcode());
        assertEquals("Test Product", results.get(0).getProductName());
        
        // Verify DAO was called
        verify(orderItemDao).selectByOrderId(1);
        verify(productApi).getByBarcode("TEST123");
        verify(orderApi).get(1);
    }

    @Test
    void testDtoToFlowToApi_Validation_OrderIdRequired() {
        // Arrange
        OrderItemForm invalidForm = new OrderItemForm();
        invalidForm.setOrderId(null);
        invalidForm.setProductId(1);
        invalidForm.setQuantity(2);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderItemDto.add(invalidForm);
        });
        
        assertEquals("Order ID cannot be null", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderItemDao, never()).insert(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_ProductNotFound() {
        // Arrange
        when(productApi.get(999)).thenThrow(new ApiException("Product not found"));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            testForm.setProductId(999);
            orderItemDto.add(testForm);
        });
        
        assertEquals("Product not found", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderItemDao, never()).insert(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_InsufficientStock() {
        // Arrange
        testInventory.setQuantity(1);
        when(productApi.get(1)).thenReturn(testProduct);
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct); // Add this mock
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory); // Use productId instead of barcode

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderItemDto.add(testForm);
        });
        
        assertEquals("Insufficient stock. Available: 1, Requested: 2", exception.getMessage());
        
        // Verify DAO was called for validation but not for insert
        verify(productApi, times(3)).get(1); // Updated to 3 times
        verify(productApi).getByBarcode("TEST123"); // Add this verification
        verify(inventoryApi).getByProductId(1); // Use productId instead of barcode
        verify(orderItemDao, never()).insert(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_NullOrderItemId() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderItemDto.get(null);
        });
        
        assertEquals("Order item ID cannot be null", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderItemDao, never()).select(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_InvalidOrderId() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            orderItemDto.getByOrderId(0);
        });
        
        assertEquals("Order ID must be positive", exception.getMessage());
        
        // Verify DAO was not called
        verify(orderItemDao, never()).selectByOrderId(any());
    }
} 