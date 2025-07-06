package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.pojo.OrderPojo;

@Repository
public class OrderDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(OrderPojo order) {
        em.persist(order);
    }

    public OrderPojo select(Integer id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> query = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        
        // Fetch order items eagerly to avoid lazy loading issues
        root.fetch("orderItems", JoinType.LEFT);
        
        query.select(root)
             .where(cb.equal(root.get("id"), id));
        
        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<OrderPojo> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> query = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        
        // Fetch order items eagerly to avoid lazy loading issues
        root.fetch("orderItems", JoinType.LEFT);
        
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    public void update(Integer id, OrderPojo order) {
        // Preserve the version field to avoid optimistic locking conflicts
        OrderPojo existing = select(id);
        if (existing != null) {
            existing.setDate(order.getDate());
            existing.setTotal(order.getTotal());
            existing.setOrderItems(order.getOrderItems());
            em.merge(existing);
        }
    }

    public void delete(Integer id) {
        OrderPojo order = select(id);
        if(order != null) {
            em.remove(order);
        }
    }

    public List<OrderPojo> findOrdersByDate(java.time.LocalDate date) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> cq = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = cq.from(OrderPojo.class);
        
        // Fetch order items eagerly to avoid lazy loading issues
        root.fetch("orderItems", JoinType.LEFT);
        
        // Convert LocalDate to UTC start/end instants
        java.time.Instant start = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        java.time.Instant end = date.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        Predicate dateBetween = cb.between(root.get("date"), start, end);
        cq.select(root).where(dateBetween);
        return em.createQuery(cq).getResultList();
    }
}
