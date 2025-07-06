package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.example.pojo.ProductPojo;
import java.util.List;

@Repository
public class ProductDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(ProductPojo product) {
        em.persist(product);
    }

    public ProductPojo select(Integer id) {
        return em.find(ProductPojo.class, id);
    }

    public ProductPojo selectByBarcode(String barcode) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
        Root<ProductPojo> root = query.from(ProductPojo.class);
        
        query.select(root)
             .where(cb.equal(root.get("barcode"), barcode));
        
        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ProductPojo selectByName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
        Root<ProductPojo> root = query.from(ProductPojo.class);
        
        query.select(root)
             .where(cb.equal(root.get("name"), name));
        
        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<ProductPojo> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
        Root<ProductPojo> root = query.from(ProductPojo.class);
        
        // Join with client to fetch eagerly
        root.fetch("client", JoinType.LEFT);
        query.select(root);
        
        return em.createQuery(query).getResultList();
    }

    public void update(Integer id, ProductPojo product) {
        // Preserve the version field to avoid optimistic locking conflicts
        ProductPojo existing = select(id);
        if (existing != null) {
            existing.setName(product.getName());
            existing.setMrp(product.getMrp());
            existing.setImageUrl(product.getImageUrl());
            existing.setClient(product.getClient());
            em.merge(existing);
        }
    }

    public void delete(Integer id) {
        ProductPojo product = select(id);
        if(product != null) {
            em.remove(product);
        }
    }
}
