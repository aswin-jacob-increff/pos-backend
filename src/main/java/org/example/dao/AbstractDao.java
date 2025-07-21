package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import org.example.util.PaginationUtil;

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
     * Generic method to find by a single field value with partial string matching.
     * Returns a list of entities where the field contains the search pattern.
     * Subclasses can use this for partial string searches.
     */
    public List<T> selectByFieldLike(String fieldName, String searchPattern) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        query.select(root)
             .where(cb.like(cb.lower(root.get(fieldName)), "%" + searchPattern.toLowerCase() + "%"));
        
        return em.createQuery(query).getResultList();
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

    // ========== PAGINATION METHODS ==========

    /**
     * Get all entities with pagination support.
     */
    public PaginationResponse<T> selectAllPaginated(PaginationRequest request) {
        request = PaginationUtil.validateAndSetDefaults(request);
        
        // Get total count
        long totalElements = countAll();
        
        if (totalElements == 0) {
            return PaginationUtil.createEmptyResponse(request);
        }
        
        // Get paginated results
        List<T> content = selectAllWithPagination(request);
        
        return PaginationUtil.createResponse(content, totalElements, request);
    }

    /**
     * Get entities by field value with pagination support.
     */
    public PaginationResponse<T> selectByFieldPaginated(String fieldName, Object value, PaginationRequest request) {
        request = PaginationUtil.validateAndSetDefaults(request);
        
        // Get total count
        long totalElements = countByField(fieldName, value);
        
        if (totalElements == 0) {
            return PaginationUtil.createEmptyResponse(request);
        }
        
        // Get paginated results
        List<T> content = selectByFieldWithPagination(fieldName, value, request);
        
        return PaginationUtil.createResponse(content, totalElements, request);
    }

    /**
     * Get entities by field value with partial string matching and pagination support.
     */
    public PaginationResponse<T> selectByFieldLikePaginated(String fieldName, String searchPattern, PaginationRequest request) {
        request = PaginationUtil.validateAndSetDefaults(request);
        
        // Get total count
        long totalElements = countByFieldLike(fieldName, searchPattern);
        
        if (totalElements == 0) {
            return PaginationUtil.createEmptyResponse(request);
        }
        
        // Get paginated results
        List<T> content = selectByFieldLikeWithPagination(fieldName, searchPattern, request);
        
        return PaginationUtil.createResponse(content, totalElements, request);
    }

    // ========== COUNT METHODS ==========

    /**
     * Count total number of entities.
     */
    public long countAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root));
        return em.createQuery(query).getSingleResult();
    }

    /**
     * Count entities by field value.
     */
    public long countByField(String fieldName, Object value) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root))
             .where(cb.equal(root.get(fieldName), value));
        return em.createQuery(query).getSingleResult();
    }

    /**
     * Count entities by field value with partial string matching.
     */
    public long countByFieldLike(String fieldName, String searchPattern) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root))
             .where(cb.like(cb.lower(root.get(fieldName)), "%" + searchPattern.toLowerCase() + "%"));
        return em.createQuery(query).getSingleResult();
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Get all entities with pagination applied.
     */
    private List<T> selectAllWithPagination(PaginationRequest request) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);
        
        // Apply sorting if specified
        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            if (request.isDescending()) {
                query.orderBy(cb.desc(root.get(request.getSortBy())));
            } else {
                query.orderBy(cb.asc(root.get(request.getSortBy())));
            }
        }
        
        return em.createQuery(query)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.getPageSize())
                .getResultList();
    }

    /**
     * Get entities by field value with pagination applied.
     */
    private List<T> selectByFieldWithPagination(String fieldName, Object value, PaginationRequest request) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root)
             .where(cb.equal(root.get(fieldName), value));
        
        // Apply sorting if specified
        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            if (request.isDescending()) {
                query.orderBy(cb.desc(root.get(request.getSortBy())));
            } else {
                query.orderBy(cb.asc(root.get(request.getSortBy())));
            }
        }
        
        return em.createQuery(query)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.getPageSize())
                .getResultList();
    }

    /**
     * Get entities by field value with partial string matching and pagination applied.
     */
    private List<T> selectByFieldLikeWithPagination(String fieldName, String searchPattern, PaginationRequest request) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root)
             .where(cb.like(cb.lower(root.get(fieldName)), "%" + searchPattern.toLowerCase() + "%"));
        
        // Apply sorting if specified
        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            if (request.isDescending()) {
                query.orderBy(cb.desc(root.get(request.getSortBy())));
            } else {
                query.orderBy(cb.asc(root.get(request.getSortBy())));
            }
        }
        
        return em.createQuery(query)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.getPageSize())
                .getResultList();
    }
} 