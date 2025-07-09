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
        try {
            em.persist(inventory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InventoryPojo select(Integer id) {
        try {
            return em.find(InventoryPojo.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<InventoryPojo> selectAll() {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
            Root<InventoryPojo> root = query.from(InventoryPojo.class);
            // Fetch product eagerly
            root.fetch("product", JoinType.LEFT);
            query.select(root);
            return em.createQuery(query).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void update(Integer id, InventoryPojo inventory) {
        try {
            // Preserve the version field to avoid optimistic locking conflicts
            InventoryPojo existing = select(id);
            if (existing != null) {
                existing.setQuantity(inventory.getQuantity());
                existing.setProduct(inventory.getProduct());
                em.merge(existing);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(Integer id) {
        try {
            InventoryPojo inventory = select(id);
            if(inventory != null) {
                em.remove(inventory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InventoryPojo getByProductId(Integer product_id) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
            Root<InventoryPojo> root = query.from(InventoryPojo.class);
            
            query.select(root)
                 .where(cb.equal(root.get("product").get("id"), product_id));
            
            return em.createQuery(query).getResultStream().findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public InventoryPojo getByProductName(String name) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
            Root<InventoryPojo> root = query.from(InventoryPojo.class);
            
            query.select(root)
                 .where(cb.equal(cb.lower(root.get("product").get("name")), name.trim().toLowerCase()));
            
            return em.createQuery(query).getResultStream().findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public InventoryPojo getByProductBarcode(String barcode) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
            Root<InventoryPojo> root = query.from(InventoryPojo.class);
            
            query.select(root)
                 .where(cb.equal(cb.lower(root.get("product").get("barcode")), barcode.trim().toLowerCase()));
            
            return em.createQuery(query).getResultStream().findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
