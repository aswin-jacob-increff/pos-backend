package org.example.dao;

import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.pojo.OrderItemPojo;
import org.example.model.data.SalesReportData;
import java.time.LocalDate;

@Repository
public class OrderItemDao extends AbstractDao<OrderItemPojo> {

    public OrderItemDao() {
        super(OrderItemPojo.class);
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
        return getByParams(new String[]{"orderId"}, new Object[]{orderId});
    }

    public List<OrderItemPojo> selectByProductId(Integer productId) {
        return getByParams(new String[]{"productId"}, new Object[]{productId});
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

    public List<SalesReportRow> getSalesReport(LocalDate start, LocalDate end, String brand, String category) {
        // This method needs to be refactored to work with the normalized structure
        // For now, return empty list as the sales report functionality should be handled differently
        // The sales report should join with Product and Client tables to get the required information
        return new java.util.ArrayList<>();
    }
}
