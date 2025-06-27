package org.example.dao;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.example.pojo.OrderPojo;

@Repository
@Transactional
public class OrderDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(OrderPojo order) {
        em.persist(order);
    }

    public OrderPojo select(Integer id) {
        return em.find(OrderPojo.class, id);
    }

    public List<OrderPojo> selectAll() {
        String query = "SELECT o FROM OrderPojo o";
        return em.createQuery(query, OrderPojo.class).getResultList();
    }

    public void update(Integer id, OrderPojo order) {
        order.setId(id);
        em.merge(order);
    }

    public void delete(Integer id) {
        OrderPojo order = select(id);
        if(order != null) {
            em.remove(order);
        }
    }
}
