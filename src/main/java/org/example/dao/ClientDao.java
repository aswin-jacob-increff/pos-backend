package org.example.dao;

import jakarta.transaction.Transactional;
import org.example.pojo.ClientPojo;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
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
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientPojo> query = cb.createQuery(ClientPojo.class);
        Root<ClientPojo> root = query.from(ClientPojo.class);
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    public void update(Integer id, ClientPojo client) {
        // Preserve the version field to avoid optimistic locking conflicts
        ClientPojo existing = select(id);
        if (existing != null) {
            existing.setClientName(client.getClientName());
            em.merge(existing);
        }
    }

    public void delete(Integer id) {
        ClientPojo client = select(id);
        if(client != null) {
            em.remove(client);
        }
    }

    public ClientPojo selectByName(String clientName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientPojo> query = cb.createQuery(ClientPojo.class);
        Root<ClientPojo> root = query.from(ClientPojo.class);
        
        query.select(root)
             .where(cb.equal(root.get("clientName"), clientName));
        
        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
