package org.example.client.unit;

import org.example.dto.ClientDto;
import org.example.pojo.ClientPojo;
import org.example.model.ClientData;
import org.example.model.ClientForm;
import org.example.exception.ApiException;
import org.example.flow.ClientFlow;
import org.example.api.ClientApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientDtoTest {

    @Mock
    private ClientFlow clientFlow;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private ClientDto clientDto;

    private ClientPojo testPojo;
    private ClientData testData;
    private ClientForm testForm;
    private Instant testDateTime;

    @BeforeEach
    void setUp() throws Exception {
        testDateTime = Instant.now();
        
        testPojo = new ClientPojo();
        testPojo.setId(1);
        testPojo.setClientName("Test Client");
        testPojo.setStatus(true);
        testPojo.setCreatedAt(testDateTime);
        testPojo.setUpdatedAt(testDateTime);
        testPojo.setVersion(1);

        testData = new ClientData();
        testData.setId(1);
        testData.setClientName("Test Client");
        testData.setStatus(true);

        testForm = new ClientForm();
        testForm.setClientName("Test Client");

        // Manually inject the api field using reflection
        Field apiField = clientDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(clientDto, clientApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doAnswer(invocation -> {
            ClientPojo pojo = invocation.getArgument(0);
            pojo.setId(1); // Set the ID on the entity
            return null;
        }).when(clientApi).add(any(ClientPojo.class));

        // Act
        ClientData result = clientDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test client", result.getClientName()); // StringUtil.format() converts to lowercase
        assertEquals(true, result.getStatus());
        verify(clientApi, times(1)).add(any(ClientPojo.class));
    }

    @Test
    void testAdd_WithNullStatus() {
        // Arrange
        testForm.setStatus(null);
        doAnswer(invocation -> {
            ClientPojo pojo = invocation.getArgument(0);
            pojo.setId(1); // Set the ID on the entity
            return null;
        }).when(clientApi).add(any(ClientPojo.class));

        // Act
        ClientData result = clientDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test client", result.getClientName()); // StringUtil.format() converts to lowercase
        assertEquals(true, result.getStatus());
        verify(clientApi, times(1)).add(any(ClientPojo.class));
    }

    @Test
    void testAdd_WithExplicitStatus() {
        // Arrange
        testForm.setStatus(false);
        doAnswer(invocation -> {
            ClientPojo pojo = invocation.getArgument(0);
            pojo.setId(1); // Set the ID on the entity
            return null;
        }).when(clientApi).add(any(ClientPojo.class));

        // Act
        ClientData result = clientDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test client", result.getClientName()); // StringUtil.format() converts to lowercase
        assertFalse(result.getStatus());
        verify(clientApi, times(1)).add(any(ClientPojo.class));
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testPojo);

        // Act
        ClientData result = clientDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(testPojo.getClientName(), result.getClientName());
        assertEquals(testPojo.getStatus(), result.getStatus());
        verify(clientApi, times(1)).get(1);
    }

    @Test
    void testGet_InvalidId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.get(0));
        assertThrows(ApiException.class, () -> clientDto.get(-1));
        assertThrows(ApiException.class, () -> clientDto.get(null));
        
        verify(clientApi, never()).get(anyInt());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        ClientPojo pojo1 = new ClientPojo();
        pojo1.setId(1);
        pojo1.setClientName("Client 1");
        pojo1.setStatus(true);

        ClientPojo pojo2 = new ClientPojo();
        pojo2.setId(2);
        pojo2.setClientName("Client 2");
        pojo2.setStatus(false);

        List<ClientPojo> pojos = Arrays.asList(pojo1, pojo2);
        when(clientApi.getAll()).thenReturn(pojos);

        // Act
        List<ClientData> result = clientDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Client 1", result.get(0).getClientName());
        assertTrue(result.get(0).getStatus());
        assertEquals("Client 2", result.get(1).getClientName());
        assertFalse(result.get(1).getStatus());
        verify(clientApi, times(1)).getAll();
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testPojo);
        doNothing().when(clientApi).update(eq(1), any(ClientPojo.class));

        // Act
        ClientData result = clientDto.update(1, testForm);

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(testPojo.getClientName(), result.getClientName());
        assertEquals(testPojo.getStatus(), result.getStatus());
        verify(clientApi, times(1)).get(1);
        verify(clientApi, times(1)).update(eq(1), any(ClientPojo.class));
    }

    @Test
    void testUpdate_InvalidId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.update(0, testForm));
        assertThrows(ApiException.class, () -> clientDto.update(-1, testForm));
        assertThrows(ApiException.class, () -> clientDto.update(null, testForm));
        
        verify(clientApi, never()).get(anyInt());
        verify(clientApi, never()).update(anyInt(), any(ClientPojo.class));
    }

    @Test
    void testDelete_Success() {
        // Arrange
        doNothing().when(clientApi).delete(1);

        // Act
        clientDto.delete(1);

        // Assert
        verify(clientApi, times(1)).delete(1);
    }

    @Test
    void testDelete_InvalidId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.delete(0));
        assertThrows(ApiException.class, () -> clientDto.delete(-1));
        assertThrows(ApiException.class, () -> clientDto.delete(null));
        
        verify(clientApi, never()).delete(anyInt());
    }

    @Test
    void testDeleteByName_Success() {
        // Arrange
        doNothing().when(clientFlow).deleteClientByName("test client");

        // Act
        clientDto.deleteByName("Test Client");

        // Assert
        verify(clientFlow, times(1)).deleteClientByName("test client");
    }

    @Test
    void testDeleteByName_EmptyName() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> clientDto.deleteByName(""));
        assertThrows(IllegalArgumentException.class, () -> clientDto.deleteByName("   "));
        
        verify(clientFlow, never()).deleteClientByName(anyString());
    }

    @Test
    void testToggleStatusById_Success() {
        // Arrange
        doNothing().when(clientFlow).toggleStatus(1);

        // Act
        clientDto.toggleStatus(1);

        // Assert
        verify(clientFlow, times(1)).toggleStatus(1);
    }

    @Test
    void testToggleStatusById_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.toggleStatus((Integer) null));
        
        verify(clientFlow, never()).toggleStatus(anyInt());
    }

    @Test
    void testToggleStatusByName_Success() {
        // Arrange
        doNothing().when(clientFlow).toggleStatusByName("test client");

        // Act
        clientDto.toggleStatusByName("Test Client");

        // Assert
        verify(clientFlow, times(1)).toggleStatusByName("test client");
    }

    @Test
    void testToggleStatusByName_NullName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.toggleStatusByName(null));
        assertThrows(ApiException.class, () -> clientDto.toggleStatusByName(""));
        assertThrows(ApiException.class, () -> clientDto.toggleStatusByName("   "));
        
        verify(clientFlow, never()).toggleStatusByName(anyString());
    }

    @Test
    void testGetByNameOrId_WithIdOnly() {
        // Arrange
        when(clientFlow.get(1)).thenReturn(testPojo);

        // Act
        ClientData result = clientDto.getByNameOrId(1, "");

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(testPojo.getClientName(), result.getClientName());
        assertEquals(testPojo.getStatus(), result.getStatus());
        verify(clientFlow, times(1)).get(1);
        verify(clientFlow, never()).getByName(anyString());
    }

    @Test
    void testGetByNameOrId_WithNameOnly() {
        // Arrange
        when(clientFlow.getByName("test client")).thenReturn(testPojo);

        // Act
        ClientData result = clientDto.getByNameOrId(null, "Test Client");

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(testPojo.getClientName(), result.getClientName());
        assertEquals(testPojo.getStatus(), result.getStatus());
        verify(clientFlow, never()).get(anyInt());
        verify(clientFlow, times(1)).getByName("test client");
    }

    @Test
    void testGetByNameOrId_WithBothIdAndName_Matching() {
        // Arrange
        when(clientFlow.get(1)).thenReturn(testPojo);
        when(clientFlow.getByName("test client")).thenReturn(testPojo);

        // Act
        ClientData result = clientDto.getByNameOrId(1, "Test Client");

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(testPojo.getClientName(), result.getClientName());
        assertEquals(testPojo.getStatus(), result.getStatus());
        verify(clientFlow, times(1)).get(1);
        verify(clientFlow, times(1)).getByName("test client");
    }

    @Test
    void testGetByNameOrId_WithBothIdAndName_NotMatching() {
        // Arrange
        ClientPojo differentPojo = new ClientPojo();
        differentPojo.setId(2);
        differentPojo.setClientName("Different Client");
        differentPojo.setStatus(false);

        when(clientFlow.get(1)).thenReturn(testPojo);
        when(clientFlow.getByName("test client")).thenReturn(differentPojo);

        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.getByNameOrId(1, "Test Client"));
        verify(clientFlow, times(1)).get(1);
        verify(clientFlow, times(1)).getByName("test client");
    }

    @Test
    void testToggleStatus_WithId() {
        // Arrange
        doNothing().when(clientFlow).toggleStatus(1);

        // Act
        clientDto.toggleStatus(1, "Test Client");

        // Assert
        verify(clientFlow, times(1)).toggleStatus(1);
        verify(clientFlow, never()).toggleStatusByName(anyString());
    }

    @Test
    void testToggleStatus_WithName() {
        // Arrange
        doNothing().when(clientFlow).toggleStatusByName("test client");

        // Act
        clientDto.toggleStatus(null, "Test Client");

        // Assert
        verify(clientFlow, never()).toggleStatus(anyInt());
        verify(clientFlow, times(1)).toggleStatusByName("test client");
    }

    @Test
    void testToggleStatus_WithNeither() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.toggleStatus(null, null));
        
        verify(clientFlow, never()).toggleStatus(anyInt());
        verify(clientFlow, never()).toggleStatusByName(anyString());
    }

    @Test
    void testPreprocess_ClientNameFormatting() {
        // Test that client names are properly formatted during preprocessing
        // This is tested indirectly through the add method
        testForm.setClientName("  test client  ");
        doAnswer(invocation -> {
            ClientPojo pojo = invocation.getArgument(0);
            pojo.setId(1); // Set the ID on the entity
            return null;
        }).when(clientApi).add(any(ClientPojo.class));

        // Act
        ClientData result = clientDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test client", result.getClientName());
        verify(clientApi, times(1)).add(any(ClientPojo.class));
        // The actual formatting is handled by StringUtil.format() in the preprocess method
    }
} 