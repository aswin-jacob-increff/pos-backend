package org.example.flow;

import org.example.pojo.ClientPojo;
import org.example.api.ClientApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClientFlow {

    @Autowired
    private ClientApi api;

    public ClientPojo add(ClientPojo clientPojo) {
        api.add(clientPojo);
        return clientPojo;
    }

    public ClientPojo update(Integer id, ClientPojo clientPojo) {
        api.update(id, clientPojo);
        return api.get(id);
    }

    public ClientPojo get(Integer id) {
        return api.get(id);
    }

    public List<ClientPojo> getAll() {
        return api.getAll();
    }

    public ClientPojo getByName(String name) {
        return api.getByName(name);
    }

    public void delete(Integer id) {
        api.delete(id);
    }

    public void deleteClientByName(String name) {
        ClientPojo client = api.getByName(name);
        if (client == null) {
            throw new RuntimeException("Client with name '" + name + "' not found.");
        }
        api.delete(client.getId());
    }

    public void toggleStatus(Integer id) {
        ClientPojo client = api.get(id);
        if (client == null) {
            throw new RuntimeException("Client with ID '" + id + "' not found.");
        }
        // TODO: Implement status toggle logic when status field is added to ClientPojo
        // For now, this is a placeholder
        throw new RuntimeException("Status toggle not yet implemented - requires status field in ClientPojo");
    }

    public void toggleStatusByName(String name) {
        ClientPojo client = api.getByName(name);
        if (client == null) {
            throw new RuntimeException("Client with name '" + name + "' not found.");
        }
        // TODO: Implement status toggle logic when status field is added to ClientPojo
        // For now, this is a placeholder
        throw new RuntimeException("Status toggle not yet implemented - requires status field in ClientPojo");
    }

    public void createClient(String clientName) {
        ClientPojo client = new ClientPojo();
        client.setClientName(clientName);
        api.add(client);
    }
}
