package org.example.dao;

import jakarta.transaction.Transactional;
import org.example.pojo.ClientPojo;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class ClientDao extends AbstractDao<ClientPojo> {

    public ClientDao() {
        super(ClientPojo.class);
    }

    public ClientPojo selectByName(String name) {
        return selectByField("clientName", name);
    }

    public void toggleStatus(Integer id) {
        ClientPojo client = select(id);
        if (client != null) {
            client.setStatus(!client.getStatus());
            update(id, client);
        }
    }

    public void toggleStatusByName(String name) {
        ClientPojo client = selectByName(name);
        if (client != null) {
            client.setStatus(!client.getStatus());
            update(client.getId(), client);
        }
    }

    @Override
    protected void updateEntity(ClientPojo existing, ClientPojo updated) {
        existing.setClientName(updated.getClientName());
        existing.setStatus(updated.getStatus());
    }
}
