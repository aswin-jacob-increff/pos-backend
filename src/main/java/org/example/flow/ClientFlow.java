package org.example.flow;

import org.example.pojo.ClientPojo;
import org.example.pojo.ProductPojo;
import org.example.api.ClientApi;
import org.example.api.ProductApi;
import org.example.exception.ApiException;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ClientFlow extends AbstractFlow<ClientPojo> {

    @Autowired
    private ClientApi api;

    @Autowired
    private ProductApi productApi;

    public ClientFlow() {
        super(ClientPojo.class);
    }

    @Override
    protected Integer getEntityId(ClientPojo pojo) {
        return pojo.getId();
    }

    @Override
    protected String getEntityName() {
        return "Client";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void update(Integer id, ClientPojo clientPojo) {
        if (Objects.isNull(id)) {
            throw new ApiException("Client ID cannot be null");
        }
        if (Objects.isNull(clientPojo)) {
            throw new ApiException("Client cannot be null");
        }
        
        // Get the existing client to compare the client name
        ClientPojo existingClient = api.get(id);
        if (existingClient == null) {
            throw new ApiException("Client with ID " + id + " not found");
        }
        
        // Update the client
        api.update(id, clientPojo);
        
        // Note: Products now use clientId instead of clientName, so no need to update products
        // when client name changes. The relationship is maintained through the clientId.
    }

    public ClientPojo getByName(String name) {
        return api.getByName(name);
    }

    public List<ClientPojo> getByNameLike(String name) {
        return api.getByNameLike(name);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all clients with pagination support.
     */
    public PaginationResponse<ClientPojo> getAllPaginated(PaginationRequest request) {
        return api.getAllPaginated(request);
    }

    /**
     * Get clients by name with pagination support.
     */
    public PaginationResponse<ClientPojo> getByNamePaginated(String name, PaginationRequest request) {
        return api.getByNamePaginated(name, request);
    }

    /**
     * Get clients by name with partial matching and pagination support.
     */
    public PaginationResponse<ClientPojo> getByNameLikePaginated(String name, PaginationRequest request) {
        return api.getByNameLikePaginated(name, request);
    }

    public ClientPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Client ID cannot be null");
        }
        return api.get(id);
    }

    public void toggleStatus(Integer id) {
        ClientPojo pojo = api.get(id);
        if (pojo == null) {
            throw new ApiException("Client with ID '" + id + "' not found.");
        }
        api.toggleStatus(id);
    }

    public void toggleStatusByName(String name) {
        ClientPojo pojo = api.getByName(name);
        if (pojo == null) {
            throw new ApiException("Client with name '" + name + "' not found.");
        }
        api.toggleStatusByName(name);
    }

    public void createClient(String name) {
        ClientPojo pojo = new ClientPojo();
        pojo.setClientName(name);
        api.add(pojo);
    }
}
