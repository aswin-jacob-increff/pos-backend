package org.example.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
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
        clientDao.insert(clientPojo);
    }

    public ClientPojo get(Integer id) {
        return clientDao.select(id);
    }

    public List<ClientPojo> getAll() {
        return clientDao.selectAll();
    }

    public void update(Integer id, ClientPojo updatedClient) {
        clientDao.update(id, updatedClient);
    }

    public void delete(Integer id) {
        clientDao.delete(id);
    }

    public ClientPojo getByName(String clientName) {
        return clientDao.selectByName(clientName);
    }

    @PostConstruct
    public void init() {
        System.out.println(">>> ClientService initialized. ClientDao = " + clientDao);
    }

}
