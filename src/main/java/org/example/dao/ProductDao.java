package org.example.dao;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import org.example.pojo.ProductPojo;
import java.util.List;

@Repository
@Transactional
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
        String query = "SELECT p FROM ProductPojo p WHERE p.barcode = :barcode";
        return em.createQuery(query, ProductPojo.class).setParameter("barcode", barcode).getSingleResult();
    }

    public ProductPojo selectByName(String name) {
        try {
            String jpql = "SELECT p FROM ProductPojo p WHERE p.name = :name";
            return em.createQuery(jpql, ProductPojo.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    public List<ProductPojo> selectAll() {
        String query = "SELECT p FROM ProductPojo p";
        return em.createQuery(query, ProductPojo.class).getResultList();
    }

    public void update(Integer id, ProductPojo product) {
        product.setId(id);
        em.merge(product);
    }

    public void delete(Integer id) {
        ProductPojo product = select(id);
        if(product != null) {
            em.remove(product);
        }
    }
}
