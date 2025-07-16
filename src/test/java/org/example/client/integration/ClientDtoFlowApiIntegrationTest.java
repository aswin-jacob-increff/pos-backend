package org.example.client.integration;

import org.example.dto.ClientDto;
import org.example.flow.ClientFlow;
import org.example.api.ClientApi;
import org.example.dao.ClientDao;
import org.example.api.ProductApi;
import org.example.model.ClientForm;
import org.example.model.ClientData;
import org.example.pojo.ClientPojo;
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

import jakarta.persistence.EntityManager;
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
@ContextConfiguration(classes = {ClientDtoFlowApiIntegrationTest.TestConfig.class})
class ClientDtoFlowApiIntegrationTest {

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
        public ClientDao clientDao() {
            return mock(ClientDao.class);
        }
        
        @Bean
        @Primary
        public ProductApi productApi() {
            return mock(ProductApi.class);
        }
        
        @Bean
        public ClientApi clientApi() {
            return new ClientApi();
        }
        
        @Bean
        public ClientFlow clientFlow() {
            return new ClientFlow();
        }
        
        @Bean
        public ClientDto clientDto() {
            return new ClientDto();
        }
    }

    @Mock
    private ClientDao clientDao;

    @Mock
    private ProductApi productApi;

    private ClientDto clientDto;
    private ClientFlow clientFlow;
    private ClientApi clientApi;

    private ClientPojo testClient;
    private ClientForm testForm;

    @BeforeEach
    void setUp() {
        // Create test data
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("test client");
        testClient.setStatus(true);
        testClient.setCreatedAt(Instant.now());
        testClient.setUpdatedAt(Instant.now());

        testForm = new ClientForm();
        testForm.setClientName("Test Client");
        testForm.setStatus(true);

        // Get beans from Spring context
        clientDto = new ClientDto();
        clientFlow = new ClientFlow();
        clientApi = new ClientApi();

        // Inject mocked dependencies using reflection
        injectMockDependencies();
    }

    private void injectMockDependencies() {
        try {
            // Inject clientDao into clientApi
            var daoField = ClientApi.class.getDeclaredField("dao");
            daoField.setAccessible(true);
            daoField.set(clientApi, clientDao);

            var productApiField = ClientApi.class.getDeclaredField("productApi");
            productApiField.setAccessible(true);
            productApiField.set(clientApi, productApi);

            // Inject clientApi into clientFlow
            var apiField = ClientFlow.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(clientFlow, clientApi);

            var productApiFlowField = ClientFlow.class.getDeclaredField("productApi");
            productApiFlowField.setAccessible(true);
            productApiFlowField.set(clientFlow, productApi);

            // Inject clientFlow into clientDto
            var flowField = ClientDto.class.getDeclaredField("flow");
            flowField.setAccessible(true);
            flowField.set(clientDto, clientFlow);

            // Inject clientApi into clientDto (inherited from AbstractDto)
            var abstractApiField = org.example.dto.AbstractDto.class.getDeclaredField("api");
            abstractApiField.setAccessible(true);
            abstractApiField.set(clientDto, clientApi);

        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    @Test
    void testDtoToFlowToApi_AddClient() {
        // Arrange
        when(clientDao.selectByName("test client")).thenReturn(null);
        doNothing().when(clientDao).insert(any(ClientPojo.class));

        // Act
        ClientData result = clientDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals("test client", result.getClientName());
        assertTrue(result.getStatus());
        
        // Verify DAO was called
        verify(clientDao).selectByName("test client");
        verify(clientDao).insert(any(ClientPojo.class));
    }

    @Test
    void testDtoToFlowToApi_GetClient() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);

        // Act
        ClientData result = clientDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test client", result.getClientName());
        assertTrue(result.getStatus());
        
        // Verify DAO was called
        verify(clientDao).select(1);
    }

    @Test
    void testDtoToFlowToApi_GetAllClients() {
        // Arrange
        ClientPojo client2 = new ClientPojo();
        client2.setId(2);
        client2.setClientName("test client 2");
        client2.setStatus(false);
        
        when(clientDao.selectAll()).thenReturn(Arrays.asList(testClient, client2));

        // Act
        List<ClientData> results = clientDto.getAll();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("test client", results.get(0).getClientName());
        assertEquals("test client 2", results.get(1).getClientName());
        
        // Verify DAO was called
        verify(clientDao).selectAll();
    }

    @Test
    void testDtoToFlowToApi_UpdateClient() {
        // Arrange
        ClientForm updateForm = new ClientForm();
        updateForm.setClientName("Updated Client");
        updateForm.setStatus(false);

        when(clientDao.select(1)).thenReturn(testClient);
        when(clientDao.selectByName("updated client")).thenReturn(null);
        doNothing().when(clientDao).update(eq(1), any(ClientPojo.class));
        when(productApi.getByClientName("test client")).thenReturn(Arrays.asList());

        // Act
        ClientData result = clientDto.update(1, updateForm);

        // Assert
        assertNotNull(result);
        assertEquals("updated client", result.getClientName());
        assertFalse(result.getStatus());
        
        // Verify DAO was called
        verify(clientDao).select(1);
        verify(clientDao).selectByName("updated client");
        verify(clientDao).update(eq(1), any(ClientPojo.class));
    }

    @Test
    void testDtoToFlowToApi_ToggleStatusById() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);
        when(productApi.hasProductsByClientName("test client")).thenReturn(false);
        doNothing().when(clientDao).toggleStatus(1);

        // Act
        clientDto.toggleStatus(1);

        // Assert
        verify(clientDao).select(1);
        verify(productApi).hasProductsByClientName("test client");
        verify(clientDao).toggleStatus(1);
    }

    @Test
    void testDtoToFlowToApi_ToggleStatusByName() {
        // Arrange
        when(clientDao.selectByField("clientName", "test client")).thenReturn(testClient);
        when(productApi.hasProductsByClientName("test client")).thenReturn(false);
        doNothing().when(clientDao).toggleStatusByName("test client");

        // Act
        clientDto.toggleStatusByName("test client");

        // Assert
        verify(clientDao).selectByField("clientName", "test client");
        verify(productApi).hasProductsByClientName("test client");
        verify(clientDao).toggleStatusByName("test client");
    }

    @Test
    void testDtoToFlowToApi_GetByNameOrId_ById() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);

        // Act
        ClientData result = clientDto.getByNameOrId(1, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test client", result.getClientName());
        
        // Verify DAO was called
        verify(clientDao).select(1);
    }

    @Test
    void testDtoToFlowToApi_GetByNameOrId_ByName() {
        // Arrange
        when(clientDao.selectByField("clientName", "test client")).thenReturn(testClient);

        // Act
        ClientData result = clientDto.getByNameOrId(null, "test client");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test client", result.getClientName());
        
        // Verify DAO was called
        verify(clientDao).selectByField("clientName", "test client");
    }

    @Test
    void testDtoToFlowToApi_Validation_ClientNameRequired() {
        // Arrange
        ClientForm invalidForm = new ClientForm();
        invalidForm.setClientName(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientDto.add(invalidForm);
        });
        
        assertEquals("Client name is required", exception.getMessage());
        
        // Verify DAO was not called
        verify(clientDao, never()).insert(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_ClientAlreadyExists() {
        // Arrange
        when(clientDao.selectByName("test client")).thenReturn(testClient);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientDto.add(testForm);
        });
        
        assertEquals("Client already exists", exception.getMessage());
        
        // Verify DAO was called for validation but not for insert
        verify(clientDao).selectByName("test client");
        verify(clientDao, never()).insert(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_ToggleStatusWithProducts() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);
        when(productApi.hasProductsByClientName("test client")).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientDto.toggleStatus(1);
        });
        
        assertEquals("Client status toggle failed. Client has products.", exception.getMessage());
        
        // Verify DAO was called for validation but not for toggle
        verify(clientDao).select(1);
        verify(productApi).hasProductsByClientName("test client");
        verify(clientDao, never()).toggleStatus(anyInt());
    }
} 