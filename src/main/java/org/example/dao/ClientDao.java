package org.example.dao;

import jakarta.transaction.Transactional;
import org.example.pojo.ClientPojo;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class ClientDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(ClientPojo client) {
        em.persist(client);
    }

    public ClientPojo select(Integer id) {
        return em.find(ClientPojo.class, id);
    }

    public List<ClientPojo> selectAll() {
        String query = "SELECT c FROM ClientPojo c";
        return em.createQuery(query, ClientPojo.class).getResultList();
    }

    public void update(Integer id, ClientPojo client) {
        client.setId(id);
        em.merge(client);
    }

    public void delete(Integer id) {
        ClientPojo client = select(id);
        if(client != null) {
            em.remove(client);
        }
    }

    public ClientPojo selectByName(String clientName) {
        String query = "SELECT c FROM ClientPojo c WHERE c.clientName = :clientName";
        try {
            return em.createQuery(query, ClientPojo.class)
                    .setParameter("clientName", clientName)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
