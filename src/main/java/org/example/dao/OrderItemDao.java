package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.pojo.OrderItemPojo;

@Repository
public class OrderItemDao extends AbstractDao<OrderItemPojo> {

    public OrderItemDao() {
        super(OrderItemPojo.class);
    }

    @Override
    public OrderItemPojo select(Integer id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> query = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = query.from(OrderItemPojo.class);
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

    @Override
    public List<OrderItemPojo> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> query = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = query.from(OrderItemPojo.class);
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    @Override
    protected void updateEntity(OrderItemPojo existing, OrderItemPojo updated) {
        existing.setOrderId(updated.getOrderId());
        existing.setProductBarcode(updated.getProductBarcode());
        existing.setProductName(updated.getProductName());
        existing.setClientName(updated.getClientName());
        existing.setProductMrp(updated.getProductMrp());
        existing.setProductImageUrl(updated.getProductImageUrl());
        existing.setQuantity(updated.getQuantity());
        existing.setSellingPrice(updated.getSellingPrice());
        existing.setAmount(updated.getAmount());
    }

    public List<OrderItemPojo> selectByOrderId(Integer orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> query = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = query.from(OrderItemPojo.class);
        query.select(root)
             .where(cb.equal(root.get("orderId"), orderId));
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
