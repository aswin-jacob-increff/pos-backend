package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.pojo.OrderPojo;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Repository
public class OrderDao extends AbstractDao<OrderPojo> {

    public OrderDao() {
        super(OrderPojo.class);
    }

    @Override
    public OrderPojo select(Integer id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> query = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        query.select(root)
             .where(cb.equal(root.get("id"), id));
        try {
            return em.createQuery(query).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<OrderPojo> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> query = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    @Override
    protected void updateEntity(OrderPojo existing, OrderPojo updated) {
        existing.setDate(updated.getDate());
        existing.setTotal(updated.getTotal());
        existing.setStatus(updated.getStatus());
        // Order items are now managed separately
    }

    public List<OrderPojo> findOrdersByDate(LocalDate date) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> cq = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = cq.from(OrderPojo.class);
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Predicate dateBetween = cb.between(root.get("date"), start, end);
        cq.select(root).where(dateBetween);
        return em.createQuery(cq).getResultList();
    }

    /**
     * Find orders within a date range (inclusive of both start and end dates)
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of orders within the date range, ordered by date ascending
     */
    public List<OrderPojo> findOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> cq = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = cq.from(OrderPojo.class);
        // Convert LocalDate (IST) to UTC Instants for comparison
        java.time.ZoneId istZone = java.time.ZoneId.of("Asia/Kolkata");
        Instant start = startDate.atStartOfDay(istZone).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(istZone).toInstant();
        Predicate dateBetween = cb.between(root.get("date"), start, end);
        cq.select(root)
          .where(dateBetween)
          .orderBy(cb.asc(root.get("date")));
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

    public List<OrderPojo> findByUserId(String userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> cq = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = cq.from(OrderPojo.class);
        cq.select(root).where(cb.equal(root.get("userId"), userId)).orderBy(cb.desc(root.get("date")));
        return em.createQuery(cq).getResultList();
    }
}
