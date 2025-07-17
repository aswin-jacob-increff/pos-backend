package org.example.client.unit;

import org.example.dto.ClientDto;
import org.example.pojo.ClientPojo;
import org.example.model.data.ClientData;
import org.example.model.form.ClientForm;
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
        doNothing().when(clientApi).add(any(ClientPojo.class));

        // Act
        ClientData result = clientDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals("test client", result.getClientName()); // StringUtil.format() converts to lowercase
        verify(clientApi).add(any(ClientPojo.class));
    }

    @Test
    void testAdd_WithNullStatus() {
        // Arrange
        testForm.setStatus(null);
        doNothing().when(clientApi).add(any(ClientPojo.class));

        // Act
        ClientData result = clientDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals("test client", result.getClientName()); // StringUtil.format() converts to lowercase
        assertTrue(result.getStatus()); // Should default to true
        verify(clientApi).add(any(ClientPojo.class));
    }

    @Test
    void testAdd_WithExplicitStatus() {
        // Arrange
        testForm.setStatus(false);
        doNothing().when(clientApi).add(any(ClientPojo.class));

        // Act
        ClientData result = clientDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals("test client", result.getClientName()); // StringUtil.format() converts to lowercase
        assertFalse(result.getStatus());
        verify(clientApi).add(any(ClientPojo.class));
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
        // Given
        List<ClientPojo> clients = Arrays.asList(
            new ClientPojo(), new ClientPojo()
        );
        when(clientFlow.getAll()).thenReturn(clients);

        // When
        List<ClientData> result = clientDto.getAll();

        // Then
        assertEquals(2, result.size());
        verify(clientFlow).getAll();
    }

    @Test
    void testUpdate_Success() {
        // Given
        ClientForm form = new ClientForm();
        form.setClientName("Updated Client");
        form.setStatus(true);

        ClientPojo client = new ClientPojo();
        client.setId(1);
        client.setClientName("updated client"); // StringUtil.format converts to lowercase
        client.setStatus(true);

        when(clientApi.get(1)).thenReturn(client);
        doNothing().when(clientFlow).update(eq(1), any(ClientPojo.class));

        // When
        ClientData result = clientDto.update(1, form);

        // Then
        assertNotNull(result);
        assertEquals("updated client", result.getClientName());
        verify(clientFlow).update(eq(1), any(ClientPojo.class));
        verify(clientApi).get(1);
    }

    @Test
    void testUpdate_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> clientDto.update(null, new ClientForm()));
        verify(clientApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullForm() {
        // When & Then
        assertThrows(ApiException.class, () -> clientDto.update(1, null));
        verify(clientApi, never()).update(any(), any());
    }

    @Test
    void testToggleStatusById_Success() {
        // Given
        doNothing().when(clientFlow).toggleStatus(1);

        // When
        clientDto.toggleStatus(1);

        // Then
        verify(clientFlow).toggleStatus(1);
    }

    @Test
    void testToggleStatusById_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> clientDto.toggleStatus((Integer) null));
        verify(clientFlow, never()).toggleStatus(any());
    }

    @Test
    void testToggleStatusByName_Success() {
        // Given
        doNothing().when(clientFlow).toggleStatusByName("test client");

        // When
        clientDto.toggleStatusByName("Test Client");

        // Then
        verify(clientFlow).toggleStatusByName("test client");
    }

    @Test
    void testToggleStatusByName_NullName() {
        // When & Then
        assertThrows(ApiException.class, () -> clientDto.toggleStatusByName(null));
        assertThrows(ApiException.class, () -> clientDto.toggleStatusByName(""));
        assertThrows(ApiException.class, () -> clientDto.toggleStatusByName("   "));
        verify(clientFlow, never()).toggleStatusByName(any());
    }

    @Test
    void testGetByNameOrId_WithIdOnly() {
        // Given
        when(clientApi.get(1)).thenReturn(testPojo);

        // When
        ClientData result = clientDto.getByNameOrId(1, "");

        // Then
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(testPojo.getClientName(), result.getClientName());
        assertEquals(testPojo.getStatus(), result.getStatus());
        verify(clientApi).get(1);
        verify(clientApi, never()).getByName(any());
    }

    @Test
    void testGetByNameOrId_WithNameOnly() {
        // Given
        when(clientApi.getByName("test client")).thenReturn(testPojo);

        // When
        ClientData result = clientDto.getByNameOrId(null, "Test Client");

        // Then
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(testPojo.getClientName(), result.getClientName());
        assertEquals(testPojo.getStatus(), result.getStatus());
        verify(clientApi, never()).get(any());
        verify(clientApi).getByName("test client");
    }

    @Test
    void testGetByNameOrId_WithBothIdAndName_Matching() {
        // Given
        when(clientApi.get(1)).thenReturn(testPojo);
        when(clientApi.getByName("test client")).thenReturn(testPojo);

        // When
        ClientData result = clientDto.getByNameOrId(1, "Test Client");

        // Then
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(testPojo.getClientName(), result.getClientName());
        assertEquals(testPojo.getStatus(), result.getStatus());
        verify(clientApi).get(1);
        verify(clientApi).getByName("test client");
    }

    @Test
    void testGetByNameOrId_WithBothIdAndName_NotMatching() {
        // Given
        ClientPojo differentPojo = new ClientPojo();
        differentPojo.setId(2);
        differentPojo.setClientName("Different Client");
        differentPojo.setStatus(false);

        when(clientApi.get(1)).thenReturn(testPojo);
        when(clientApi.getByName("test client")).thenReturn(differentPojo);

        // When & Then
        assertThrows(ApiException.class, () -> clientDto.getByNameOrId(1, "Test Client"));
        verify(clientApi).get(1);
        verify(clientApi).getByName("test client");
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
        // Arrange
        ClientForm form = new ClientForm();
        form.setClientName("  Test Client  ");

        // Act
        ClientData result = clientDto.add(form);

        // Assert
        assertNotNull(result);
        assertEquals("test client", result.getClientName()); // StringUtil.format() converts to lowercase and trims
        verify(clientApi).add(any(ClientPojo.class));
    }
} 