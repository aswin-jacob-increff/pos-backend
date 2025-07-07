package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.pojo.OrderPojo;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

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
        
        // Fetch order items and their relationships eagerly to avoid lazy loading issues
        Join<OrderPojo, org.example.pojo.OrderItemPojo> orderItems = root.join("orderItems", JoinType.LEFT);
        Fetch<Object, Object> productFetch = orderItems.fetch("product", JoinType.LEFT);
        productFetch.fetch("client", JoinType.LEFT);
        
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
        
        // Fetch order items and their relationships eagerly to avoid lazy loading issues
        Join<OrderPojo, org.example.pojo.OrderItemPojo> orderItems = root.join("orderItems", JoinType.LEFT);
        Fetch<Object, Object> productFetch = orderItems.fetch("product", JoinType.LEFT);
        productFetch.fetch("client", JoinType.LEFT);
        
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    public void update(Integer id, OrderPojo order) {
        OrderPojo existing = select(id);
        if (existing != null) {
            existing.setDate(order.getDate());
            existing.setTotal(order.getTotal());
            existing.setOrderItems(order.getOrderItems());
            existing.setStatus(order.getStatus());
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
        
        // Fetch order items and their relationships eagerly to avoid lazy loading issues
        Join<OrderPojo, org.example.pojo.OrderItemPojo> orderItems = root.join("orderItems", JoinType.LEFT);
        Fetch<Object, Object> productFetch = orderItems.fetch("product", JoinType.LEFT);
        productFetch.fetch("client", JoinType.LEFT);
        
        // Convert UTC LocalDate to UTC start/end instants
        java.time.Instant start = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        java.time.Instant end = date.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        Predicate dateBetween = cb.between(root.get("date"), start, end);
        cq.select(root).where(dateBetween);
        return em.createQuery(cq).getResultList();
    }

    public LocalDate findEarliestOrderDate() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Instant> cq = cb.createQuery(Instant.class);
        Root<OrderPojo> root = cq.from(OrderPojo.class);
        cq.select(root.get("date")).orderBy(cb.asc(root.get("date")));
        List<Instant> results = em.createQuery(cq).setMaxResults(1).getResultList();
        if (results.isEmpty() || results.get(0) == null) return LocalDate.now(ZoneOffset.UTC);
        return results.get(0).atZone(ZoneOffset.UTC).toLocalDate();
    }
}
