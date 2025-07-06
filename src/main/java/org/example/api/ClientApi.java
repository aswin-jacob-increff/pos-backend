package org.example.api;

import jakarta.transaction.Transactional;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.dao.ClientDao;
import org.example.pojo.ClientPojo;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    public void add(ClientPojo clientPojo) {
        ClientPojo existing = clientDao.selectByName(clientPojo.getClientName());
        if(Objects.nonNull(existing)) {
            throw new ApiException("Client already exists");
        }
        clientDao.insert(clientPojo);
    }

    public ClientPojo get(Integer id) {
        if (Objects.isNull(clientDao.select(id))) {
            throw new ApiException("Client does not exist.");
        }
        return clientDao.select(id);
    }

    public List<ClientPojo> getAll() {
        if(Objects.isNull(clientDao.selectAll())) {
            throw new ApiException("No clients.");
        }
        return clientDao.selectAll();
    }

    public void update(Integer id, ClientPojo updatedClient) {
        ClientPojo existing = clientDao.select(id);
        if (Objects.isNull(existing)) {
            throw new ApiException("Client not found for ID : " + id);
        }
        
        // Check if the new name is different from the current name
        if (!existing.getClientName().equalsIgnoreCase(updatedClient.getClientName())) {
            // Check if the new name already exists for another client
            ClientPojo clientWithNewName = clientDao.selectByName(updatedClient.getClientName());
            if (Objects.nonNull(clientWithNewName) && !clientWithNewName.getId().equals(id)) {
                throw new ApiException("Client name already exists");
            }
        }
        
        clientDao.update(id, updatedClient);
    }

    public void delete(Integer id) {
        if (Objects.isNull(clientDao.select(id))) {
            throw new ApiException("Client not found for ID : " + id);
        }
        clientDao.delete(id);
    }

    public ClientPojo getByName(String clientName) {
        if (Objects.isNull(clientDao.selectByName(clientName.trim().toLowerCase()))) {
            throw new ApiException("Client with name " + clientName + " does not exist.");
        }
        return clientDao.selectByName(clientName);
    }
} 