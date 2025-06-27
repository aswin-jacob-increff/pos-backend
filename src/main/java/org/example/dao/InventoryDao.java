package org.example.dao;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.example.pojo.InventoryPojo;

@Repository
@Transactional
public class InventoryDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(InventoryPojo inventory) {
        em.persist(inventory);
    }

    public InventoryPojo select(Integer id) {
        return em.find(InventoryPojo.class, id);
    }

    public List<InventoryPojo> selectAll() {
        String query = "SELECT i FROM InventoryPojo i";
        return em.createQuery(query, InventoryPojo.class).getResultList();
    }

    public void update(Integer id, InventoryPojo inventory) {
        inventory.setId(id);
        em.merge(inventory);
    }

    public void delete(Integer id) {
        InventoryPojo inventory = select(id);
        if(inventory != null) {
            em.remove(inventory);
        }
    }

    public InventoryPojo getByProductId(Integer product_id) {
        String jpql = "SELECT i FROM InventoryPojo i WHERE i.product.id = :product_id";
        TypedQuery<InventoryPojo> query = em.createQuery(jpql, InventoryPojo.class);
        query.setParameter("product_id", product_id);

        return query.getResultStream().findFirst().orElse(null); // returns null if not found
    }

    public InventoryPojo getByProductName(String name) {
        String jpql = """
            SELECT i FROM InventoryPojo i
            WHERE LOWER(i.product.name) = :name
        """;
        TypedQuery<InventoryPojo> query = em.createQuery(jpql, InventoryPojo.class);
        query.setParameter("name", name.trim().toLowerCase());

        return query.getResultStream().findFirst().orElse(null);
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        String jpql = """
            SELECT i FROM InventoryPojo i
            WHERE LOWER(i.product.barcode) = :barcode
        """;
        TypedQuery<InventoryPojo> query = em.createQuery(jpql, InventoryPojo.class);
        query.setParameter("barcode", barcode.trim().toLowerCase());

        return query.getResultStream().findFirst().orElse(null);
    }
}
