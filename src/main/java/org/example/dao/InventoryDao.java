package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.pojo.InventoryPojo;

@Repository
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
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
        Root<InventoryPojo> root = query.from(InventoryPojo.class);
        
        // Fetch product and its client eagerly
        root.fetch("product", JoinType.LEFT).fetch("client", JoinType.LEFT);
        
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    public void update(Integer id, InventoryPojo inventory) {
        // Preserve the version field to avoid optimistic locking conflicts
        InventoryPojo existing = select(id);
        if (existing != null) {
            existing.setQuantity(inventory.getQuantity());
            existing.setProduct(inventory.getProduct());
            em.merge(existing);
        }
    }

    public void delete(Integer id) {
        InventoryPojo inventory = select(id);
        if(inventory != null) {
            em.remove(inventory);
        }
    }

    public InventoryPojo getByProductId(Integer product_id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
        Root<InventoryPojo> root = query.from(InventoryPojo.class);
        
        query.select(root)
             .where(cb.equal(root.get("product").get("id"), product_id));
        
        return em.createQuery(query).getResultStream().findFirst().orElse(null);
    }

    public InventoryPojo getByProductName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
        Root<InventoryPojo> root = query.from(InventoryPojo.class);
        
        query.select(root)
             .where(cb.equal(cb.lower(root.get("product").get("name")), name.trim().toLowerCase()));
        
        return em.createQuery(query).getResultStream().findFirst().orElse(null);
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
        Root<InventoryPojo> root = query.from(InventoryPojo.class);
        
        query.select(root)
             .where(cb.equal(cb.lower(root.get("product").get("barcode")), barcode.trim().toLowerCase()));
        
        return em.createQuery(query).getResultStream().findFirst().orElse(null);
    }
}
