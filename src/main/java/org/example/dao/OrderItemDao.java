package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.pojo.OrderItemPojo;

@Repository
public class OrderItemDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(OrderItemPojo item) {
        em.persist(item);
    }

    public OrderItemPojo select(Integer id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> query = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = query.from(OrderItemPojo.class);
        // Fetch order directly
        root.fetch("order", JoinType.LEFT);
        query.select(root)
             .where(cb.equal(root.get("id"), id));
        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<OrderItemPojo> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> query = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = query.from(OrderItemPojo.class);
        // Fetch order directly
        root.fetch("order", JoinType.LEFT);
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    public void update(Integer id, OrderItemPojo item) {
        // Preserve the version field to avoid optimistic locking conflicts
        OrderItemPojo existing = select(id);
        if (existing != null) {
            existing.setOrder(item.getOrder());
            existing.setProductBarcode(item.getProductBarcode());
            existing.setProductName(item.getProductName());
            existing.setClientName(item.getClientName());
            existing.setProductMrp(item.getProductMrp());
            existing.setProductImageUrl(item.getProductImageUrl());
            existing.setQuantity(item.getQuantity());
            existing.setSellingPrice(item.getSellingPrice());
            existing.setAmount(item.getAmount());
            em.merge(existing);
        }
    }

    public void delete(Integer id) {
        OrderItemPojo item = select(id);
        if(item != null) {
            em.remove(item);
        }
    }

    public List<OrderItemPojo> selectByOrderId(Integer orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> query = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = query.from(OrderItemPojo.class);
        
        query.select(root)
             .where(cb.equal(root.get("order").get("id"), orderId));
        
        return em.createQuery(query).getResultList();
    }

    public List<OrderItemPojo> selectByProductBarcode(String barcode) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> query = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = query.from(OrderItemPojo.class);
        
        query.select(root)
             .where(cb.equal(root.get("productBarcode"), barcode));
        
        return em.createQuery(query).getResultList();
    }

    public static class SalesReportRow {
        private String brand;
        private String category;
        private Long quantity;
        private Double revenue;
        public SalesReportRow(String brand, String category, Long quantity, Double revenue) {
            this.brand = brand;
            this.category = category;
            this.quantity = quantity;
            this.revenue = revenue;
        }
        // getters and setters
        public String getBrand() { return brand; }
        public String getCategory() { return category; }
        public Long getQuantity() { return quantity; }
        public Double getRevenue() { return revenue; }
    }

    public List<SalesReportRow> getSalesReport(java.time.LocalDate start, java.time.LocalDate end, String brand, String category) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<OrderItemPojo> item = cq.from(OrderItemPojo.class);
        Join<Object, org.example.pojo.OrderPojo> order = item.join("order");
        List<Predicate> predicates = new java.util.ArrayList<>();
        // Order date is Instant, so filter between start and end (as UTC instants)
        java.time.Instant startInstant = start.atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        java.time.Instant endInstant = end.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        predicates.add(cb.between(order.get("date"), startInstant, endInstant));
        if (brand != null && !brand.isEmpty()) {
            predicates.add(cb.equal(item.get("clientName"), brand));
        }
        if (category != null && !category.isEmpty()) {
            predicates.add(cb.equal(item.get("productName"), category));
        }
        cq.multiselect(
            item.get("clientName"),
            item.get("productName"),
            cb.sum(item.get("quantity")),
            cb.sum(item.get("amount"))
        )
        .where(predicates.toArray(new Predicate[0]))
        .groupBy(item.get("clientName"), item.get("productName"));
        List<Object[]> results = em.createQuery(cq).getResultList();
        List<SalesReportRow> rows = new java.util.ArrayList<>();
        for (Object[] row : results) {
            rows.add(new SalesReportRow(
                (String) row[0],
                (String) row[1],
                (Long) row[2],
                (Double) row[3]
            ));
        }
        return rows;
    }
}
