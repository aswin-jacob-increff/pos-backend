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
    protected Integer getEntityId(ClientPojo pojo) {
        return pojo.getId();
    }

    @Override
    protected String getEntityName() {
        return "Client";
    }

    public ClientPojo getByName(String name) {
        return api.getByName(name);
    }

    public void deleteClientByName(String name) {
        ClientPojo pojo = api.getByName(name);
        if (pojo == null) {
            throw new RuntimeException("Client with name '" + name + "' not found.");
        }
        api.delete(pojo.getId());
    }

    public void toggleStatus(Integer id) {
        ClientPojo pojo = api.get(id);
        if (pojo == null) {
            throw new RuntimeException("Client with ID '" + id + "' not found.");
        }
        api.toggleStatus(id);
    }

    public void toggleStatusByName(String name) {
        ClientPojo pojo = api.getByName(name);
        if (pojo == null) {
            throw new RuntimeException("Client with name '" + name + "' not found.");
        }
        api.toggleStatusByName(name);
    }

    public void createClient(String name) {
        ClientPojo pojo = new ClientPojo();
        pojo.setClientName(name);
        api.add(pojo);
    }
}
