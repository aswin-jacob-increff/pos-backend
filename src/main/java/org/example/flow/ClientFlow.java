package org.example.flow;

import org.example.pojo.ClientPojo;
import org.example.api.ClientApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClientFlow extends AbstractFlow<ClientPojo> {

    @Autowired
    private ClientApi api;

    @Override
    protected Integer getEntityId(ClientPojo entity) {
        return entity.getId();
    }

    @Override
    protected String getEntityName() {
        return "Client";
    }

    public ClientPojo getByName(String name) {
        return api.getByName(name);
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
