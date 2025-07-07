package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.pojo.InvoicePojo;

@Repository
public class InvoiceDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(InvoicePojo invoice) {
        em.persist(invoice);
    }

    public InvoicePojo select(Integer id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InvoicePojo> query = cb.createQuery(InvoicePojo.class);
        Root<InvoicePojo> root = query.from(InvoicePojo.class);
        
        query.select(root)
             .where(cb.equal(root.get("id"), id));
        
        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public InvoicePojo selectByOrderId(Integer orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InvoicePojo> query = cb.createQuery(InvoicePojo.class);
        Root<InvoicePojo> root = query.from(InvoicePojo.class);
        
        query.select(root)
             .where(cb.equal(root.get("orderId"), orderId));
        
        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<InvoicePojo> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InvoicePojo> query = cb.createQuery(InvoicePojo.class);
        Root<InvoicePojo> root = query.from(InvoicePojo.class);
        
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    public void update(Integer id, InvoicePojo invoice) {
        // Preserve the version field to avoid optimistic locking conflicts
        InvoicePojo existing = select(id);
        if (existing != null) {
            existing.setOrderId(invoice.getOrderId());
            existing.setFilePath(invoice.getFilePath());
            existing.setInvoiceId(invoice.getInvoiceId());
            em.merge(existing);
        }
    }

    public void delete(Integer id) {
        InvoicePojo invoice = select(id);
        if(invoice != null) {
            em.remove(invoice);
        }
    }
} 