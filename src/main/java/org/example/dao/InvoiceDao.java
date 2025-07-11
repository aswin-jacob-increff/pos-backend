package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.pojo.InvoicePojo;

@Repository
public class InvoiceDao extends AbstractDao<InvoicePojo> {

    public InvoiceDao() {
        super(InvoicePojo.class);
    }

    public InvoicePojo selectByOrderId(Integer orderId) {
        return selectByField("orderId", orderId);
    }
    
    public List<InvoicePojo> selectAllByOrderId(Integer orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InvoicePojo> query = cb.createQuery(InvoicePojo.class);
        Root<InvoicePojo> root = query.from(InvoicePojo.class);
        
        query.select(root)
             .where(cb.equal(root.get("orderId"), orderId));
        
        return em.createQuery(query).getResultList();
    }

    @Override
    protected void updateEntity(InvoicePojo existing, InvoicePojo updated) {
        existing.setOrderId(updated.getOrderId());
        existing.setFilePath(updated.getFilePath());
        existing.setInvoiceId(updated.getInvoiceId());
    }
} 