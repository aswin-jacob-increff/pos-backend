package org.example.dao;

import jakarta.persistence.*;
import org.example.pojo.DaySalesPojo;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.criteria.*;

@Repository
public class DaySalesDao extends AbstractDao<DaySalesPojo> {

    public DaySalesDao() {
        super(DaySalesPojo.class);
    }

    public void saveOrUpdate(DaySalesPojo daySales) {
        try {
            System.out.println("Upserting day sales for date: " + daySales.getDate() + 
                              " (Revenue: " + daySales.getTotalRevenue() + 
                              ", Orders: " + daySales.getInvoicedOrdersCount() + 
                              ", Items: " + daySales.getInvoicedItemsCount() + ")");
            
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
            System.out.println("Day sales upsert completed. Rows affected: " + rowsAffected);
            em.flush();
        } catch (Exception e) {
            System.err.println("Error in day sales upsert for date " + daySales.getDate() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<DaySalesPojo> findByDateRange(LocalDate start, LocalDate end) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DaySalesPojo> cq = cb.createQuery(DaySalesPojo.class);
        Root<DaySalesPojo> root = cq.from(DaySalesPojo.class);
        // cb.between() is inclusive of both start and end dates
        cq.select(root)
          .where(cb.between(root.get("date"), start, end))
          .orderBy(cb.asc(root.get("date")));
        return em.createQuery(cq).getResultList();
    }

    public LocalDate findLatestDate() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LocalDate> cq = cb.createQuery(LocalDate.class);
        Root<DaySalesPojo> root = cq.from(DaySalesPojo.class);
        cq.select(root.get("date")).orderBy(cb.desc(root.get("date")));
        List<LocalDate> results = em.createQuery(cq).setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public DaySalesPojo findByDate(LocalDate date) {
        return em.find(DaySalesPojo.class, date);
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
        // Orders are now denormalized and managed separately
    }
} 