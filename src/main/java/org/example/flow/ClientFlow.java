package org.example.flow;

import org.example.pojo.ClientPojo;
import org.example.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientFlow {

    @Autowired
    private ClientService clientService;

    public ClientPojo add(ClientPojo clientPojo) {
        clientService.add(clientPojo);
        return clientPojo;
    }

    public ClientPojo update(Integer id, ClientPojo clientPojo) {
        clientService.update(id, clientPojo);
        return clientService.get(id);
    }

    public ClientPojo get(Integer id) {
        return clientService.get(id);
    }

    public List<ClientPojo> getAll() {
        return clientService.getAll();
    }

    public ClientPojo getByName(String name) {
        return clientService.getByName(name);
    }

    public void delete(Integer id) {
        clientService.delete(id);
    }

    public void deleteClientByName(String name) {
        ClientPojo client = clientService.getByName(name);
        if (client == null) {
            throw new RuntimeException("Client with name '" + name + "' not found.");
        }
        clientService.delete(client.getId());
    }

    public void createClient(String clientName) {
        ClientPojo client = new ClientPojo();
        client.setClientName(clientName);
        clientService.add(client);
    }
}
