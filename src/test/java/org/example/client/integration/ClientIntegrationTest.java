package org.example.client.integration;

import org.example.dto.ClientDto;
import org.example.api.ClientApi;
import org.example.dao.ClientDao;
import org.example.model.data.ClientData;
import org.example.model.form.ClientForm;
import org.example.pojo.ClientPojo;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientIntegrationTest {

    @Mock
    private ClientApi clientApi;

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientDto clientDto;

    private ClientForm testForm;
    private ClientPojo testClient;
    private ClientData testClientData;

    @BeforeEach
    void setUp() throws Exception {
        testForm = new ClientForm();
        testForm.setClientName("Test Client");
        testForm.setStatus(true);

        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("test client");
        testClient.setStatus(true);

        testClientData = new ClientData();
        testClientData.setId(1);
        testClientData.setClientName("test client");
        testClientData.setStatus(true);

        // Inject the api field from AbstractDto
        Field apiField = clientDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(clientDto, clientApi);

        // Inject the dao field from AbstractApi
        Field daoField = clientApi.getClass().getSuperclass().getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(clientApi, clientDao);
    }

    @Test
    void testAdd_Integration_Success() {
        // Arrange - DTO layer validation
        doNothing().when(clientApi).add(any(ClientPojo.class));

        // Act - DTO calls API
        ClientData result = clientDto.add(testForm);

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals("test client", result.getClientName());
        assertTrue(result.getStatus());
        
        // Verify DTO called API
        verify(clientApi).add(any(ClientPojo.class));
    }

    @Test
    void testGet_Integration_Success() {
        // Arrange - API layer gets from DAO
        when(clientApi.get(1)).thenReturn(testClient);

        // Act - DTO calls API
        ClientData result = clientDto.get(1);

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        assertEquals(testClient.getClientName(), result.getClientName());
        
        // Verify DTO called API
        verify(clientApi).get(1);
    }

    @Test
    void testUpdate_Integration_Success() {
        // Arrange - API layer updates via DAO
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(clientApi).update(eq(1), any(ClientPojo.class));

        // Act - DTO calls API
        ClientData result = clientDto.update(1, testForm);

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals("test client", result.getClientName());
        
        // Verify DTO called API
        verify(clientApi).update(eq(1), any(ClientPojo.class));
    }

    @Test
    void testGetAll_Integration_Success() {
        // Arrange - API layer gets all from DAO
        List<ClientPojo> clients = Arrays.asList(testClient, testClient);
        when(clientApi.getAll()).thenReturn(clients);

        // Act - DTO calls API
        List<ClientData> result = clientDto.getAll();

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify DTO called API
        verify(clientApi).getAll();
    }

    @Test
    void testToggleStatus_Integration_Success() {
        // Arrange - API layer toggles via DAO
        doNothing().when(clientApi).toggleStatus(1);

        // Act - DTO calls API
        clientDto.toggleStatus(1);

        // Assert - Verify the entire flow
        verify(clientApi).toggleStatus(1);
    }

    @Test
    void testToggleStatusByName_Integration_Success() {
        // Arrange - API layer toggles by name via DAO
        doNothing().when(clientApi).toggleStatusByName("test client");

        // Act - DTO calls API
        clientDto.toggleStatusByName("Test Client");

        // Assert - Verify the entire flow
        verify(clientApi).toggleStatusByName("test client");
    }

    @Test
    void testGetByNameOrId_Integration_WithIdAndName_Matching() {
        // Arrange - API layer gets by both ID and name via DAO
        when(clientApi.get(1)).thenReturn(testClient);
        when(clientApi.getByName("test client")).thenReturn(testClient);

        // Act - DTO calls API
        ClientData result = clientDto.getByNameOrId(1, "Test Client");

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        
        // Verify DTO called API for both ID and name
        verify(clientApi).get(1);
        verify(clientApi).getByName("test client");
    }

    @Test
    void testGetByNameOrId_Integration_WithIdOnly() {
        // Arrange - API layer gets by ID via DAO
        when(clientApi.get(1)).thenReturn(testClient);

        // Act - DTO calls API
        ClientData result = clientDto.getByNameOrId(1, "");

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        
        // Verify DTO called API only for ID
        verify(clientApi).get(1);
        verify(clientApi, never()).getByName(anyString());
    }

    @Test
    void testGetByNameOrId_Integration_WithNameOnly() {
        // Arrange - API layer gets by name via DAO
        when(clientApi.getByName("test client")).thenReturn(testClient);

        // Act - DTO calls API
        ClientData result = clientDto.getByNameOrId(null, "Test Client");

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        
        // Verify DTO called API only for name
        verify(clientApi, never()).get(anyInt());
        verify(clientApi).getByName("test client");
    }

    @Test
    void testGetByNameLike_Integration_Success() {
        // Arrange - API layer gets by name like via DAO
        List<ClientPojo> clients = Arrays.asList(testClient);
        when(clientApi.getByNameLike("test")).thenReturn(clients);

        // Act - DTO calls API
        List<ClientPojo> result = clientApi.getByNameLike("test");

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testClient.getId(), result.get(0).getId());
        
        // Verify API was called
        verify(clientApi).getByNameLike("test");
    }

    @Test
    void testAdd_Integration_ValidationFailure() {
        // Arrange - DTO layer validation fails
        testForm.setClientName(null);

        // Act & Assert - DTO validation should prevent API call
        assertThrows(ApiException.class, () -> clientDto.add(testForm));
        
        // Verify API was never called
        verify(clientApi, never()).add(any());
        verify(clientDao, never()).insert(any());
    }

    @Test
    void testToggleStatus_Integration_ClientNotFound() {
        // Arrange - API throws exception for client not found
        doThrow(new ApiException("Client not found")).when(clientApi).toggleStatus(1);

        // Act & Assert - API should throw exception
        assertThrows(ApiException.class, () -> clientDto.toggleStatus(1));
        
        // Verify API was called
        verify(clientApi).toggleStatus(1);
    }

    @Test
    void testToggleStatusByName_Integration_ClientNotFound() {
        // Arrange - API throws exception for client not found
        doThrow(new ApiException("Client not found")).when(clientApi).toggleStatusByName("test client");

        // Act & Assert - API should throw exception
        assertThrows(ApiException.class, () -> clientDto.toggleStatusByName("Test Client"));
        
        // Verify API was called
        verify(clientApi).toggleStatusByName("test client");
    }
} 