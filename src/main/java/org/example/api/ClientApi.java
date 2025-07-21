package org.example.api;

import jakarta.transaction.Transactional;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.dao.ClientDao;
import org.example.pojo.ClientPojo;
import org.example.util.StringUtil;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import java.util.Objects;
import java.util.List;

@Service
@Transactional
public class ClientApi extends AbstractApi<ClientPojo> {

    @Autowired
    private ClientDao dao;

    @Autowired
    private ProductApi productApi;

    @Override
    protected String getEntityName() {
        return "Client";
    }

    @Override
    protected void validateAdd(ClientPojo pojo) {
        ClientPojo existing = dao.selectByName(pojo.getClientName());
        if(Objects.nonNull(existing)) {
            throw new ApiException("Client already exists");
        }
    }

    @Override
    protected void validateUpdate(ClientPojo existing, ClientPojo updated) {
        // Check if the new name is different from the current name
        if (!existing.getClientName().equalsIgnoreCase(updated.getClientName())) {
            // Check if the new name already exists for another client
            ClientPojo pojoWithNewName = dao.selectByName(updated.getClientName());
            if (Objects.nonNull(pojoWithNewName) && !pojoWithNewName.getId().equals(existing.getId())) {
                throw new ApiException("Client name already exists");
            }
        }
    }

    public ClientPojo getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        // Format the name consistently with how it's stored
        String formattedName = StringUtil.format(name);
        return findByField("clientName", formattedName);
    }

    public List<ClientPojo> getByNameLike(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        return dao.selectByNameLike(name);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all clients with pagination support.
     */
    public PaginationResponse<ClientPojo> getAllPaginated(PaginationRequest request) {
        return dao.getAllPaginated(request);
    }

    /**
     * Get clients by name with pagination support.
     */
    public PaginationResponse<ClientPojo> getByNamePaginated(String name, PaginationRequest request) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        // Format the name consistently with how it's stored
        String formattedName = StringUtil.format(name);
        return dao.selectByNamePaginated(formattedName, request);
    }

    /**
     * Get clients by name with partial matching and pagination support.
     */
    public PaginationResponse<ClientPojo> getByNameLikePaginated(String name, PaginationRequest request) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        return dao.selectByNameLikePaginated(name, request);
    }

    public ClientPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Client ID cannot be null");
        }
        ClientPojo client = dao.select(id);
        if (client == null) {
            throw new ApiException("Client with ID " + id + " not found");
        }
        return client;
    }

    public void toggleStatus(Integer id) {
        ClientPojo client = get(id);
        if (client == null) {
            throw new ApiException("Client with ID '" + id + "' not found");
        }
        
        // Check if we're trying to set status to false (inactive)
        if (client.getStatus()) {
            // Client is currently active, check if it has products before deactivating
            if (productApi.hasProductsByClientId(client.getId())) {
                throw new ApiException("Client status toggle failed. Client has products.");
            }
        }
        
        dao.toggleStatus(id);
    }

    public void toggleStatusByName(String name) {
        ClientPojo client = getByName(name);
        if (client == null) {
            throw new ApiException("Client with name '" + name + "' not found");
        }
        
        // Check if we're trying to set status to false (inactive)
        if (client.getStatus()) {
            // Client is currently active, check if it has products before deactivating
            if (productApi.hasProductsByClientId(client.getId())) {
                throw new ApiException("Client status toggle failed. Client has products.");
            }
        }
        
        dao.toggleStatusByName(name);
    }
} 