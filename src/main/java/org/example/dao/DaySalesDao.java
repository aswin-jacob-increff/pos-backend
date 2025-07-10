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
            DaySalesPojo existing = findByDate(daySales.getDate());
            if (existing != null) {
                // Update existing entity
                existing.setTotalRevenue(daySales.getTotalRevenue());
                existing.setInvoicedOrdersCount(daySales.getInvoicedOrdersCount());
                existing.setInvoicedItemsCount(daySales.getInvoicedItemsCount());
                em.merge(existing);
            } else {
                // Persist new entity
                em.persist(daySales);
            }
            // Flush to ensure the operation is executed immediately
            em.flush();
        } catch (Exception e) {
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