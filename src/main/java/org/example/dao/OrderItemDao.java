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
        existing.setProductId(updated.getProductId());
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

    public List<OrderItemPojo> selectByProductId(Integer productId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> query = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = query.from(OrderItemPojo.class);
        query.select(root)
             .where(cb.equal(root.get("productId"), productId));
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
        // This method needs to be refactored to work with the normalized structure
        // For now, return empty list as the sales report functionality should be handled differently
        // The sales report should join with Product and Client tables to get the required information
        return new java.util.ArrayList<>();
    }
}
