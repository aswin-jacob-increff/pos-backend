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

    public ClientPojo selectByName(String clientName) {
        return selectByField("clientName", clientName);
    }

    @Override
    protected void updateEntity(ClientPojo existing, ClientPojo updated) {
        existing.setClientName(updated.getClientName());
    }
}
