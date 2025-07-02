package org.example.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.example.dao.ClientDao;
import org.example.pojo.ClientPojo;

import java.util.List;

@Service
@Transactional
public class ClientService {

    @Autowired
    private ClientDao clientDao;

    public void add(ClientPojo clientPojo) {
        ClientPojo existing = clientDao.selectByName(clientPojo.getClientName());
        if(existing != null) {
            throw new ApiException("Client already exists");
        }
        clientDao.insert(clientPojo);
    }

    public ClientPojo get(Integer id) {
        if (clientDao.select(id) == null) {
            throw new ApiException("Client does not exist.");
        }
        return clientDao.select(id);
    }

    public List<ClientPojo> getAll() {
        if(clientDao.selectAll() == null) {
            throw new ApiException("No clients.");
        }
        return clientDao.selectAll();
    }

    public void update(Integer id, ClientPojo updatedClient) {
        ClientPojo existing = clientDao.select(id);
        if (existing == null) {
            throw new ApiException("Client not found for ID : " + id);
        }
        if (existing.getClientName().equalsIgnoreCase(updatedClient.getClientName())) {
            throw new ApiException("Client name cannot be the same.");
        }
        clientDao.update(id, updatedClient);
    }

    public void delete(Integer id) {
        if (clientDao.select(id) == null) {
            throw new ApiException("Client not found for ID : " + id);
        }
        clientDao.delete(id);
    }

    public ClientPojo getByName(String clientName) {
        if (clientDao.selectByName(clientName.trim().toLowerCase()) == null) {
            throw new ApiException("Client with name " + clientName + " does not exist.");
        }
        return clientDao.selectByName(clientName);
    }
}
