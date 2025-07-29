package org.example.client.unit;

import org.example.dto.ClientDto;
import org.example.api.ClientApi;
import org.example.model.data.ClientData;
import org.example.model.form.ClientForm;
import org.example.model.data.TsvUploadResult;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.pojo.ClientPojo;
import org.example.exception.ApiException;
import org.example.util.ClientTsvParser;
import org.example.util.FileValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientDtoTest {

    @Mock
    private ClientApi clientApi;

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
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(clientApi).add(any(ClientPojo.class));

        // Act
        ClientData result = clientDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals("test client", result.getClientName());
        assertTrue(result.getStatus());
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
        assertFalse(result.getStatus());
        verify(clientApi).add(any(ClientPojo.class));
    }

    @Test
    void testAdd_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.add(null));
        verify(clientApi, never()).add(any());
    }

    @Test
    void testAdd_NullClientName() {
        // Arrange
        testForm.setClientName(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.add(testForm));
        verify(clientApi, never()).add(any());
    }

    @Test
    void testAdd_EmptyClientName() {
        // Arrange
        testForm.setClientName("");

        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.add(testForm));
        verify(clientApi, never()).add(any());
    }

    @Test
    void testAdd_WhitespaceClientName() {
        // Arrange
        testForm.setClientName("   ");

        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.add(testForm));
        verify(clientApi, never()).add(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        ClientData result = clientDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        assertEquals(testClient.getClientName(), result.getClientName());
        verify(clientApi).get(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.get(null));
        verify(clientApi, never()).get(any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<ClientPojo> clients = Arrays.asList(testClient, testClient);
        when(clientApi.getAll()).thenReturn(clients);

        // Act
        List<ClientData> result = clientDto.getAll();

        // Assert
        assertEquals(2, result.size());
        verify(clientApi).getAll();
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(clientApi).update(anyInt(), any(ClientPojo.class));

        // Act
        ClientData result = clientDto.update(1, testForm);

        // Assert
        assertNotNull(result);
        assertEquals("test client", result.getClientName());
        verify(clientApi).update(eq(1), any(ClientPojo.class));
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.update(null, testForm));
        verify(clientApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.update(1, null));
        verify(clientApi, never()).update(any(), any());
    }

    @Test
    void testToggleStatus_Success() {
        // Arrange
        doNothing().when(clientApi).toggleStatus(1);

        // Act
        clientDto.toggleStatus(1);

        // Assert
        verify(clientApi).toggleStatus(1);
    }

    @Test
    void testToggleStatus_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.toggleStatus(null));
        verify(clientApi, never()).toggleStatus(any());
    }

    @Test
    void testToggleStatusByName_Success() {
        // Arrange
        doNothing().when(clientApi).toggleStatusByName("test client");

        // Act
        clientDto.toggleStatusByName("Test Client");

        // Assert
        verify(clientApi).toggleStatusByName("test client");
    }

    @Test
    void testToggleStatusByName_NullName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.toggleStatusByName(null));
        verify(clientApi, never()).toggleStatusByName(any());
    }

    @Test
    void testToggleStatusByName_EmptyName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.toggleStatusByName(""));
        verify(clientApi, never()).toggleStatusByName(any());
    }

    @Test
    void testToggleStatusByName_WhitespaceName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.toggleStatusByName("   "));
        verify(clientApi, never()).toggleStatusByName(any());
    }

    @Test
    void testGetByNameOrId_WithIdAndName_Matching() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testClient);
        when(clientApi.getByName("test client")).thenReturn(testClient);

        // Act
        ClientData result = clientDto.getByNameOrId(1, "Test Client");

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        verify(clientApi).get(1);
        verify(clientApi).getByName("test client");
    }

    @Test
    void testGetByNameOrId_WithIdAndName_NotMatching() {
        // Arrange
        ClientPojo differentClient = new ClientPojo();
        differentClient.setId(2);
        differentClient.setClientName("different client");
        
        when(clientApi.get(1)).thenReturn(testClient);
        when(clientApi.getByName("test client")).thenReturn(differentClient);

        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.getByNameOrId(1, "Test Client"));
        verify(clientApi).get(1);
        verify(clientApi).getByName("test client");
    }

    @Test
    void testGetByNameOrId_WithIdOnly() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        ClientData result = clientDto.getByNameOrId(1, "");

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        verify(clientApi).get(1);
        verify(clientApi, never()).getByName(any());
    }

    @Test
    void testGetByNameOrId_WithNameOnly() {
        // Arrange
        when(clientApi.getByName("test client")).thenReturn(testClient);

        // Act
        ClientData result = clientDto.getByNameOrId(null, "Test Client");

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        verify(clientApi, never()).get(any());
        verify(clientApi).getByName("test client");
    }

    @Test
    void testGetByNameOrId_WithNullIdAndNullName() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> clientDto.getByNameOrId(null, null));
        verify(clientApi, never()).get(any());
        verify(clientApi, never()).getByName(any());
    }

    @Test
    void testGetByNameLikePaginated_Success() {
        // Arrange
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<ClientPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testClient));
        expectedResponse.setTotalElements(1);
        
        when(clientApi.getPaginated(any())).thenReturn(expectedResponse);

        // Act
        PaginationResponse<ClientData> result = clientDto.getByNameLikePaginated("test", request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(clientApi).getPaginated(any());
    }

    @Test
    void testUploadClientsFromTsv_Success() throws Exception {
        // Arrange
        String tsvContent = "clientName\nTest Client 1\nTest Client 2";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Mock the parser to return a successful result
        TsvUploadResult mockResult = new TsvUploadResult();
        mockResult.setTotalRows(2);
        mockResult.setSuccessfulRows(2);
        mockResult.setFailedRows(0);
        
        // Mock the static method call
        try (MockedStatic<ClientTsvParser> mockedParser = mockStatic(ClientTsvParser.class);
             MockedStatic<FileValidationUtil> mockedValidation = mockStatic(FileValidationUtil.class)) {
            
            doNothing().when(FileValidationUtil.class);
            FileValidationUtil.validateTsvFile(any(MockMultipartFile.class));
            mockedParser.when(() -> ClientTsvParser.parseWithDuplicateDetection(any(InputStream.class)))
                .thenReturn(mockResult);



            // Act
            TsvUploadResult result = clientDto.uploadClientsFromTsv(file);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalRows());
            assertEquals(2, result.getSuccessfulRows());
            assertEquals(0, result.getFailedRows());
        }
    }

    @Test
    void testUploadClientsFromTsv_NullFile() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.uploadClientsFromTsv(null));
    }

    @Test
    void testUploadClientsFromTsv_EmptyFile() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.tsv", "text/tab-separated-values", new byte[0]);

        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.uploadClientsFromTsv(file));
    }

    @Test
    void testToggleStatus_WithId() {
        // Arrange
        doNothing().when(clientApi).toggleStatus(1);

        // Act
        clientDto.toggleStatus(1, null);

        // Assert
        verify(clientApi).toggleStatus(1);
        verify(clientApi, never()).toggleStatusByName(any());
    }

    @Test
    void testToggleStatus_WithName() {
        // Arrange
        doNothing().when(clientApi).toggleStatusByName("test client");

        // Act
        clientDto.toggleStatus(null, "Test Client");

        // Assert
        verify(clientApi, never()).toggleStatus(any());
        verify(clientApi).toggleStatusByName("test client");
    }

    @Test
    void testToggleStatus_WithBothIdAndName() {
        // Arrange
        doNothing().when(clientApi).toggleStatus(1);

        // Act
        clientDto.toggleStatus(1, "Test Client");

        // Assert
        verify(clientApi).toggleStatus(1);
        verify(clientApi, never()).toggleStatusByName(any());
    }

    @Test
    void testToggleStatus_WithNullIdAndNullName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.toggleStatus(null, null));
        verify(clientApi, never()).toggleStatus(any());
        verify(clientApi, never()).toggleStatusByName(any());
    }

    @Test
    void testToggleStatus_WithNullIdAndEmptyName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.toggleStatus(null, ""));
        verify(clientApi, never()).toggleStatus(any());
        verify(clientApi, never()).toggleStatusByName(any());
    }

    @Test
    void testToggleStatus_WithNullIdAndWhitespaceName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientDto.toggleStatus(null, "   "));
        verify(clientApi, never()).toggleStatus(any());
        verify(clientApi, never()).toggleStatusByName(any());
    }
} 