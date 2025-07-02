package org.example.dao;

import jakarta.persistence.*;
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
        return em.find(OrderItemPojo.class, id);
    }

    public List<OrderItemPojo> selectAll() {
        String query = "SELECT oi FROM OrderItemPojo oi";
        return em.createQuery(query, OrderItemPojo.class).getResultList();
    }

    public void update(Integer id, OrderItemPojo item) {
        item.setId(id);
        em.merge(item);
    }

    public void delete(Integer id) {
        OrderItemPojo item = select(id);
        if(item != null) {
            em.remove(item);
        }
    }

    public List<OrderItemPojo> selectByOrderId(Integer orderId) {
        String query = "SELECT oi FROM OrderItemPojo oi WHERE oi.order.id = :orderId";
        return em.createQuery(query, OrderItemPojo.class).setParameter("orderId", orderId).getResultList();
    }

    public List<OrderItemPojo> selectByProductId(Integer productId) {
        String query = "SELECT oi FROM OrderItemPojo oi WHERE oi.product.id = :productId";
        return em.createQuery(query, OrderItemPojo.class).setParameter("productId", productId).getResultList();
    }
}
