package org.example.dao;

import jakarta.persistence.*;
import org.example.pojo.DaySales;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.criteria.*;

@Repository
public class DaySalesRepository {
    @PersistenceContext
    private EntityManager em;

    public void saveOrUpdate(DaySales daySales) {
        em.merge(daySales);
    }

    public List<DaySales> findByDateRange(LocalDate start, LocalDate end) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DaySales> cq = cb.createQuery(DaySales.class);
        Root<DaySales> root = cq.from(DaySales.class);
        cq.select(root)
          .where(cb.between(root.get("date"), start, end))
          .orderBy(cb.asc(root.get("date")));
        return em.createQuery(cq).getResultList();
    }
} 