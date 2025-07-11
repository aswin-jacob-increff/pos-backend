package org.example.client.unit;

import org.example.controller.ClientController;
import org.example.model.ClientData;
import org.example.model.ClientForm;
import org.example.dto.ClientDto;
import org.example.exception.ApiException;
import org.example.pojo.ClientPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientDto clientDto;

    @InjectMocks
    private ClientController clientController;

    private ClientPojo testClientPojo;
    private ClientData testClientData;
    private ClientForm testClientForm;

    @BeforeEach
    void setUp() {
        testClientPojo = new ClientPojo();
        testClientPojo.setId(1);
        testClientPojo.setClientName("Test Client");
        testClientPojo.setStatus(true);
        testClientPojo.setCreatedAt(Instant.now());
        testClientPojo.setUpdatedAt(Instant.now());

        testClientData = new ClientData();
        testClientData.setId(1);
        testClientData.setClientName("Test Client");
        testClientData.setStatus(true);

        testClientForm = new ClientForm();
        testClientForm.setClientName("Test Client");
    }

    @Test
    void testGetAllClients_Success() {
        // Arrange
        List<ClientData> clients = Arrays.asList(testClientData);
        when(clientDto.getAll()).thenReturn(clients);

        // Act
        List<ClientData> result = clientController.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Client", result.get(0).getClientName());
        verify(clientDto, times(1)).getAll();
    }

    @Test
    void testGetAllClients_EmptyList() {
        // Arrange
        when(clientDto.getAll()).thenReturn(Arrays.asList());

        // Act
        List<ClientData> result = clientController.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clientDto, times(1)).getAll();
    }

    @Test
    void testGetClientById_Success() {
        // Arrange
        when(clientDto.getByNameOrId(1, null)).thenReturn(testClientData);

        // Act
        ClientData result = clientController.getClient(1, null);

        // Assert
        assertNotNull(result);
        assertEquals("Test Client", result.getClientName());
        assertEquals(true, result.getStatus());
        verify(clientDto, times(1)).getByNameOrId(1, null);
    }

    @Test
    void testGetClientByName_Success() {
        // Arrange
        when(clientDto.getByNameOrId(null, "Test Client")).thenReturn(testClientData);

        // Act
        ClientData result = clientController.getClient(null, "Test Client");

        // Assert
        assertNotNull(result);
        assertEquals("Test Client", result.getClientName());
        assertEquals(true, result.getStatus());
        verify(clientDto, times(1)).getByNameOrId(null, "Test Client");
    }

    @Test
    void testAddClient_Success() {
        // Arrange
        when(clientDto.add(any(ClientForm.class))).thenReturn(testClientData);

        // Act
        ClientData result = clientController.add(testClientForm);

        // Assert
        assertNotNull(result);
        assertEquals("Test Client", result.getClientName());
        verify(clientDto, times(1)).add(any(ClientForm.class));
    }

    @Test
    void testAddClient_ValidationError() {
        // Arrange
        when(clientDto.add(any(ClientForm.class)))
            .thenThrow(new ApiException("Client already exists"));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientController.add(testClientForm);
        });

        assertEquals("Client already exists", exception.getMessage());
        verify(clientDto, times(1)).add(any(ClientForm.class));
    }

    @Test
    void testUpdateClient_Success() {
        // Arrange
        when(clientDto.update(eq(1), any(ClientForm.class))).thenReturn(testClientData);

        // Act
        ClientData result = clientController.update(1, testClientForm);

        // Assert
        assertNotNull(result);
        assertEquals("Test Client", result.getClientName());
        verify(clientDto, times(1)).update(eq(1), any(ClientForm.class));
    }

    @Test
    void testUpdateClient_ValidationError() {
        // Arrange
        when(clientDto.update(eq(1), any(ClientForm.class)))
            .thenThrow(new ApiException("Client name already exists"));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientController.update(1, testClientForm);
        });

        assertEquals("Client name already exists", exception.getMessage());
        verify(clientDto, times(1)).update(eq(1), any(ClientForm.class));
    }

    @Test
    void testToggleStatusById_Success() {
        // Arrange
        doNothing().when(clientDto).toggleStatus(1, null);

        // Act
        clientController.toggleStatus(1, null);

        // Assert
        verify(clientDto, times(1)).toggleStatus(1, null);
    }

    @Test
    void testToggleStatusByName_Success() {
        // Arrange
        doNothing().when(clientDto).toggleStatus(null, "Test Client");

        // Act
        clientController.toggleStatus(null, "Test Client");

        // Assert
        verify(clientDto, times(1)).toggleStatus(null, "Test Client");
    }

    @Test
    void testToggleStatus_HasProducts() {
        // Arrange
        doThrow(new ApiException("Client status toggle failed. Client has products."))
            .when(clientDto).toggleStatus(1, null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientController.toggleStatus(1, null);
        });

        assertEquals("Client status toggle failed. Client has products.", exception.getMessage());
        verify(clientDto, times(1)).toggleStatus(1, null);
    }

    @Test
    void testUploadClientsFromTsv_Success() {
        // Arrange
        org.springframework.mock.web.MockMultipartFile file = 
            new org.springframework.mock.web.MockMultipartFile(
                "file", 
                "test.tsv", 
                "text/tab-separated-values", 
                "Test Client\ttrue".getBytes()
            );
        when(clientDto.uploadClientsFromTsv(any())).thenReturn("Uploaded 1 clients");

        // Act
        ResponseEntity<String> response = clientController.uploadClientsFromTsv(file);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Uploaded 1 clients", response.getBody());
        verify(clientDto, times(1)).uploadClientsFromTsv(any());
    }

    @Test
    void testUploadClientsFromTsv_Error() {
        // Arrange
        org.springframework.mock.web.MockMultipartFile file = 
            new org.springframework.mock.web.MockMultipartFile(
                "file", 
                "test.tsv", 
                "text/tab-separated-values", 
                "Invalid Data".getBytes()
            );
        when(clientDto.uploadClientsFromTsv(any()))
            .thenThrow(new ApiException("Invalid file format"));

        // Act
        ResponseEntity<String> response = clientController.uploadClientsFromTsv(file);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid file format", response.getBody());
        verify(clientDto, times(1)).uploadClientsFromTsv(any());
    }

    @Test
    void testUploadClientsFromTsv_InternalError() {
        // Arrange
        org.springframework.mock.web.MockMultipartFile file = 
            new org.springframework.mock.web.MockMultipartFile(
                "file", 
                "test.tsv", 
                "text/tab-separated-values", 
                "Test Data".getBytes()
            );
        when(clientDto.uploadClientsFromTsv(any()))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<String> response = clientController.uploadClientsFromTsv(file);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to process file"));
        verify(clientDto, times(1)).uploadClientsFromTsv(any());
    }
} 