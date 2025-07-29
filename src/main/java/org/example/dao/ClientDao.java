package org.example.dao;

import org.springframework.transaction.annotation.Transactional;
import org.example.pojo.ClientPojo;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@Transactional
public class ClientDao extends AbstractDao<ClientPojo> {

    public ClientDao() {
        super(ClientPojo.class);
    }

    public ClientPojo selectByName(String name) {
        List<ClientPojo> results = getByParams("clientName", name);
        return results.isEmpty() ? null : results.get(0);
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
