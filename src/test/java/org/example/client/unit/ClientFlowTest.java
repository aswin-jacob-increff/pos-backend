package org.example.client.unit;

import org.example.flow.AbstractFlow;
import org.example.api.ClientApi;
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
class ClientFlowTest {

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private TestClientFlow clientFlow;

    private ClientPojo testClient;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("test client");
        testClient.setStatus(true);

        // Inject the api field from AbstractFlow
        Field apiField = clientFlow.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(clientFlow, clientApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(clientApi).add(any(ClientPojo.class));

        // Act
        ClientPojo result = clientFlow.add(testClient);

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        verify(clientApi).add(testClient);
    }

    @Test
    void testAdd_NullClient() {
        // Arrange - API should throw exception for null
        doThrow(new ApiException("Client cannot be null")).when(clientApi).add(null);

        // Act & Assert - AbstractFlow.add calls API which throws exception
        assertThrows(ApiException.class, () -> clientFlow.add(null));
        verify(clientApi).add(null);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        doNothing().when(clientApi).update(anyInt(), any(ClientPojo.class));

        // Act
        clientFlow.update(1, testClient);

        // Assert
        verify(clientApi).update(1, testClient);
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientFlow.update(null, testClient));
        verify(clientApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullClient() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientFlow.update(1, null));
        verify(clientApi, never()).update(any(), any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<ClientPojo> clients = Arrays.asList(testClient, testClient);
        when(clientApi.getAll()).thenReturn(clients);

        // Act
        List<ClientPojo> result = clientFlow.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(clientApi).getAll();
    }

    @Test
    void testGetByField_Success() {
        // Arrange
        when(clientApi.getByField("clientName", "test client")).thenReturn(testClient);

        // Act
        ClientPojo result = clientFlow.getByField("clientName", "test client");

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        verify(clientApi).getByField("clientName", "test client");
    }

    @Test
    void testGetByField_NullFieldName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientFlow.getByField(null, "test"));
        verify(clientApi, never()).getByField(any(), any());
    }

    @Test
    void testGetByField_NullValue() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientFlow.getByField("clientName", null));
        verify(clientApi, never()).getByField(any(), any());
    }

    @Test
    void testFindByField_Success() {
        // Arrange
        when(clientApi.findByField("clientName", "test client")).thenReturn(testClient);

        // Act
        ClientPojo result = clientFlow.findByField("clientName", "test client");

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        verify(clientApi).findByField("clientName", "test client");
    }

    @Test
    void testFindByField_NotFound() {
        // Arrange
        when(clientApi.findByField("clientName", "nonexistent")).thenReturn(null);

        // Act
        ClientPojo result = clientFlow.findByField("clientName", "nonexistent");

        // Assert
        assertNull(result);
        verify(clientApi).findByField("clientName", "nonexistent");
    }

    @Test
    void testGetByFields_Success() {
        // Arrange
        String[] fieldNames = {"clientName", "status"};
        Object[] values = {"test client", true};
        List<ClientPojo> clients = Arrays.asList(testClient);
        when(clientApi.getByFields(fieldNames, values)).thenReturn(clients);

        // Act
        List<ClientPojo> result = clientFlow.getByFields(fieldNames, values);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clientApi).getByFields(fieldNames, values);
    }

    @Test
    void testGetByFields_NullFieldNames() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientFlow.getByFields(null, new Object[]{"test"}));
        verify(clientApi, never()).getByFields(any(), any());
    }

    @Test
    void testGetByFields_NullValues() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientFlow.getByFields(new String[]{"clientName"}, null));
        verify(clientApi, never()).getByFields(any(), any());
    }



    // Test implementation of AbstractFlow for ClientPojo
    private static class TestClientFlow extends AbstractFlow<ClientPojo> {
        public TestClientFlow() {
            super(ClientPojo.class);
        }

        @Override
        protected String getEntityName() {
            return "Client";
        }
    }
} 