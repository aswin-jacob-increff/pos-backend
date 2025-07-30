package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.example.pojo.DaySalesPojo;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class DaySalesDao extends AbstractDao<DaySalesPojo> {

    public DaySalesDao() {
        super(DaySalesPojo.class);
    }

    public void saveOrUpdate(DaySalesPojo daySales) {
        try {
            // Use native SQL with ON DUPLICATE KEY UPDATE to handle race conditions
            String sql = "INSERT INTO pos_day_sales (date, totalRevenue, invoicedOrdersCount, invoicedItemsCount) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "totalRevenue = VALUES(totalRevenue), " +
                        "invoicedOrdersCount = VALUES(invoicedOrdersCount), " +
                        "invoicedItemsCount = VALUES(invoicedItemsCount)";
            
            Query query = em.createNativeQuery(sql);
            query.setParameter(1, daySales.getDate());
            query.setParameter(2, daySales.getTotalRevenue());
            query.setParameter(3, daySales.getInvoicedOrdersCount());
            query.setParameter(4, daySales.getInvoicedItemsCount());
            
            int rowsAffected = query.executeUpdate();
            em.flush();
        } catch (Exception e) {
            System.err.println("Error in day sales upsert for date " + daySales.getDate() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<DaySalesPojo> findByDateRange(LocalDate start, LocalDate end) {
        // This is a specialized date range query that should remain as is
        // since it uses the date-specific logic from AbstractDao
        return selectByDateRange("date", start, end);
    }

    public LocalDate findLatestDate() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ZonedDateTime> query = cb.createQuery(ZonedDateTime.class);
        Root<DaySalesPojo> root = query.from(DaySalesPojo.class);
        query.select(root.get("date")).orderBy(cb.desc(root.get("date")));
        List<ZonedDateTime> results = em.createQuery(query).setMaxResults(1).getResultList();
        if (results.isEmpty() || results.get(0) == null) return LocalDate.now();
        return results.get(0).toLocalDate();
    }

    public DaySalesPojo findByDate(LocalDate date) {
        // Convert LocalDate to ZonedDateTime for the primary key lookup
        ZonedDateTime zonedDate = date.atStartOfDay(java.time.ZoneId.of("Asia/Kolkata"));
        return em.find(DaySalesPojo.class, zonedDate);
    }
    
    @Override
    public List<DaySalesPojo> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DaySalesPojo> cq = cb.createQuery(DaySalesPojo.class);
        Root<DaySalesPojo> root = cq.from(DaySalesPojo.class);
        cq.select(root).orderBy(cb.desc(root.get("date")));
        return em.createQuery(cq).getResultList();
    }

    @Override
    protected void updateEntity(DaySalesPojo existing, DaySalesPojo updated) {
        existing.setDate(updated.getDate());
        existing.setTotalRevenue(updated.getTotalRevenue());
        existing.setInvoicedOrdersCount(updated.getInvoicedOrdersCount());
        existing.setInvoicedItemsCount(updated.getInvoicedItemsCount());
    }
} 