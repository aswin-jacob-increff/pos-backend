package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public abstract class AbstractDao<T> {

    @PersistenceContext
    protected EntityManager em;

    protected final Class<T> entityClass;

    protected AbstractDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void insert(T entity) {
        em.persist(entity);
    }

    public T select(Integer id) {
        return em.find(entityClass, id);
    }

    public List<T> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    public void update(Integer id, T entity) {
        T existing = select(id);
        if (existing != null) {
            updateEntity(existing, entity);
            em.merge(existing);
        }
    }

    public void delete(Integer id) {
        T entity = select(id);
        if (entity != null) {
            em.remove(entity);
        }
    }

    /**
     * Template method for updating entity fields.
     * Subclasses should override this to implement specific update logic.
     */
    protected abstract void updateEntity(T existing, T updated);

    /**
     * Generic method to find by a single field value.
     * Subclasses can use this for common field-based queries.
     */
    public T selectByField(String fieldName, Object value) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        query.select(root)
             .where(cb.equal(root.get(fieldName), value));
        
        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Generic method to find by multiple field values.
     * Subclasses can use this for complex queries.
     */
    public List<T> selectByFields(String[] fieldNames, Object[] values) {
        if (fieldNames.length != values.length) {
            throw new IllegalArgumentException("Field names and values arrays must have the same length");
        }
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        Predicate[] predicates = new Predicate[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            predicates[i] = cb.equal(root.get(fieldNames[i]), values[i]);
        }
        
        query.select(root).where(predicates);
        return em.createQuery(query).getResultList();
    }
} 