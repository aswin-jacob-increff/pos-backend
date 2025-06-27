package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.pojo.InvoicePojo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class InvoiceDao {

    @PersistenceContext
    private EntityManager em;

    // Insert a new invoice with its items (cascades automatically)
    public void insert(InvoicePojo invoice) {
        em.persist(invoice);
    }

    // Fetch a specific invoice by ID
    public InvoicePojo select(Integer id) {
        return em.find(InvoicePojo.class, id);
    }

    // Fetch all invoices
    public List<InvoicePojo> selectAll() {
        String query = "SELECT i FROM InvoicePojo i";
        return em.createQuery(query, InvoicePojo.class).getResultList();
    }

    public InvoicePojo selectByOrderId(Integer orderId) {
        String jpql = "SELECT i FROM InvoicePojo i WHERE i.order.id = :orderId";
        return em.createQuery(jpql, InvoicePojo.class)
                .setParameter("orderId", orderId)
                .getSingleResult();
    }

    public InvoicePojo selectWithItems(Integer id) {
        String jpql = """
        SELECT DISTINCT i FROM InvoicePojo i
        LEFT JOIN FETCH i.invoiceItemList item
        LEFT JOIN FETCH i.order
        LEFT JOIN FETCH item.orderItem oi
        LEFT JOIN FETCH oi.product
        WHERE i.id = :id
    """;
        return em.createQuery(jpql, InvoicePojo.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public List<InvoicePojo> selectAllWithItems() {
        String jpql = """
        SELECT DISTINCT i FROM InvoicePojo i
        LEFT JOIN FETCH i.invoiceItemList item
        LEFT JOIN FETCH i.order
        LEFT JOIN FETCH item.orderItem oi
        LEFT JOIN FETCH oi.product
    """;
        return em.createQuery(jpql, InvoicePojo.class).getResultList();
    }




}
