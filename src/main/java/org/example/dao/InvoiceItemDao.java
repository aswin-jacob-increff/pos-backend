package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.pojo.InvoiceItemPojo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class InvoiceItemDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(InvoiceItemPojo item) {
        em.persist(item);
    }

    public InvoiceItemPojo select(Integer id) {
        return em.find(InvoiceItemPojo.class, id);
    }

    public List<InvoiceItemPojo> selectAll() {
        String query = "SELECT i FROM InvoiceItemPojo i";
        return em.createQuery(query, InvoiceItemPojo.class).getResultList();
    }
}

