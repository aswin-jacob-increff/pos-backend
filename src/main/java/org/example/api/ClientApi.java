package org.example.api;

import jakarta.transaction.Transactional;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.dao.ClientDao;
import org.example.pojo.ClientPojo;
import java.util.Objects;

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
        return getByField("clientName", name.trim().toLowerCase());
    }

    public void toggleStatus(Integer id) {
        ClientPojo client = get(id);
        if (client == null) {
            throw new ApiException("Client with ID '" + id + "' not found");
        }
        
        // Check if we're trying to set status to false (inactive)
        if (client.getStatus()) {
            // Client is currently active, check if it has products before deactivating
            if (productApi.hasProductsByClientName(client.getClientName())) {
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
            if (productApi.hasProductsByClientName(client.getClientName())) {
                throw new ApiException("Client status toggle failed. Client has products.");
            }
        }
        
        dao.toggleStatusByName(name);
    }
} 