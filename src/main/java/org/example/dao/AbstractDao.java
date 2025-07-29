package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.model.data.PaginationResponse;
import org.example.util.PaginationUtil;
import org.example.exception.ApiException;

@Repository
public abstract class AbstractDao<T> {

    @PersistenceContext
    protected EntityManager em;

    protected final Class<T> entityClass;

    protected AbstractDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // ========== BASIC CRUD OPERATIONS ==========

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

    // ========== STANDARDIZED GET METHODS ==========

    /**
     * Get all records with optional pagination
     */
    public List<T> getAll() {
        return selectAll();
    }

    /**
     * Get records by parameters (field-value pairs)
     */
    public List<T> getByParams(String[] fieldNames, Object[] values) {
        if (fieldNames == null || values == null || fieldNames.length != values.length) {
            throw new IllegalArgumentException("Field names and values arrays must have the same length and cannot be null");
        }
        return selectByFields(fieldNames, values);
    }

    /**
     * Get records by a single parameter
     */
    public List<T> getByParams(String fieldName, Object value) {
        if (fieldName == null || value == null) {
            throw new IllegalArgumentException("Field name and value cannot be null");
        }
        T result = selectByField(fieldName, value);
        return result != null ? List.of(result) : List.of();
    }

    /**
     * Get records by parameters with LIKE matching
     */
    public List<T> getByParamsLike(String fieldName, String searchPattern) {
        if (fieldName == null || searchPattern == null) {
            throw new IllegalArgumentException("Field name and search pattern cannot be null");
        }
        return selectByFieldLike(fieldName, searchPattern);
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

    // ========== ENHANCED QUERY METHODS ==========

    /**
     * Generic method to find by a single field value.
     */
    public T selectByField(String fieldName, Object value) {
        return selectByFieldOptional(fieldName, value).orElse(null);
    }

    /**
     * Generic method to find by a single field value with Optional return.
     */
    public Optional<T> selectByFieldOptional(String fieldName, Object value) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root).where(cb.equal(root.get(fieldName), value));
        List<T> results = em.createQuery(query).getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Generic method to find by a single field value with partial string matching.
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



    /**
     * Generic method to find by date range.
     */
    public List<T> selectByDateRange(String dateFieldName, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        Predicate dateBetween = cb.between(root.get(dateFieldName), startDate, endDate);
        query.select(root).where(dateBetween).orderBy(cb.asc(root.get(dateFieldName)));
        
        return em.createQuery(query).getResultList();
    }

    /**
     * Generic method to find by date range with Instant conversion.
     */
    public List<T> selectByDateRangeInstant(String dateFieldName, java.time.LocalDate startDate, java.time.LocalDate endDate, java.time.ZoneId zoneId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        java.time.Instant start = startDate.atStartOfDay(zoneId).toInstant();
        java.time.Instant end = endDate.plusDays(1).atStartOfDay(zoneId).toInstant();
        
        Predicate dateBetween = cb.between(root.get(dateFieldName), start, end);
        query.select(root).where(dateBetween).orderBy(cb.asc(root.get(dateFieldName)));
        
        return em.createQuery(query).getResultList();
    }

    /**
     * Generic method to find minimum value of a field.
     * Note: This method has type limitations and should be used carefully.
     */
    @SuppressWarnings("unchecked")
    public <R> R findMinValue(String fieldName, Class<R> resultType) {
        // For now, return null as this method has complex type constraints
        // Specific DAOs should implement their own min/max methods as needed
        return null;
    }

    /**
     * Generic method to find maximum value of a field.
     * Note: This method has type limitations and should be used carefully.
     */
    @SuppressWarnings("unchecked")
    public <R> R findMaxValue(String fieldName, Class<R> resultType) {
        // For now, return null as this method has complex type constraints
        // Specific DAOs should implement their own min/max methods as needed
        return null;
    }

    /**
     * Generic method to check if entity exists by field value.
     */
    public boolean existsByField(String fieldName, Object value) {
        return countByField(fieldName, value) > 0;
    }

    /**
     * Generic method to check if entity exists by multiple field values.
     */
    public boolean existsByFields(String[] fieldNames, Object[] values) {
        return countByFields(fieldNames, values) > 0;
    }

    // ========== STANDARDIZED PAGINATED GET METHODS ==========

    /**
     * Get all records with pagination
     */
    public PaginationResponse<T> getAllPaginated(PaginationRequest request) {
        return getPaginated(PaginationQuery.all(request));
    }

    /**
     * Get records by parameters with pagination
     */
    public PaginationResponse<T> getByParamsPaginated(String[] fieldNames, Object[] values, PaginationRequest request) {
        return getPaginated(PaginationQuery.byFields(fieldNames, values, request));
    }

    /**
     * Get records by a single parameter with pagination
     */
    public PaginationResponse<T> getByParamsPaginated(String fieldName, Object value, PaginationRequest request) {
        return getPaginated(PaginationQuery.byField(fieldName, value, request));
    }

    /**
     * Get records by parameters with LIKE matching and pagination
     */
    public PaginationResponse<T> getByParamsLikePaginated(String fieldName, String searchPattern, PaginationRequest request) {
        return getPaginated(PaginationQuery.byFieldLike(fieldName, searchPattern, request));
    }

    // ========== GENERALIZED PAGINATION METHOD ==========

    /**
     * Generalized pagination method that handles all types of queries.
     */
    public PaginationResponse<T> getPaginated(PaginationQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Pagination query cannot be null");
        }
        
        PaginationRequest request = PaginationUtil.validateAndSetDefaults(query.getPaginationRequest());
        
        long totalElements;
        List<T> content;
        
        switch (query.getQueryType()) {
            case ALL:
                totalElements = countAll();
                if (totalElements == 0) {
                    return PaginationUtil.createEmptyResponse(request);
                }
                content = selectAllWithPagination(request);
                break;
                
            case BY_FIELD:
                totalElements = countByField(query.getFieldName(), query.getFieldValue());
                if (totalElements == 0) {
                    return PaginationUtil.createEmptyResponse(request);
                }
                content = selectByFieldWithPagination(query.getFieldName(), query.getFieldValue(), request);
                break;
                
            case BY_FIELD_LIKE:
                totalElements = countByFieldLike(query.getFieldName(), query.getSearchPattern());
                if (totalElements == 0) {
                    return PaginationUtil.createEmptyResponse(request);
                }
                content = selectByFieldLikeWithPagination(query.getFieldName(), query.getSearchPattern(), request);
                break;
                
            case BY_FIELDS:
                totalElements = countByFields(query.getFieldNames(), query.getFieldValues());
                if (totalElements == 0) {
                    return PaginationUtil.createEmptyResponse(request);
                }
                content = selectByFieldsWithPagination(query.getFieldNames(), query.getFieldValues(), request);
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported query type: " + query.getQueryType());
        }
        
        return PaginationUtil.createResponse(content, totalElements, request);
    }

    // ========== COUNT METHODS ==========

    public long countAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root));
        return em.createQuery(query).getSingleResult();
    }

    public long countByField(String fieldName, Object value) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root))
             .where(cb.equal(root.get(fieldName), value));
        return em.createQuery(query).getSingleResult();
    }

    public long countByFieldLike(String fieldName, String searchPattern) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root))
             .where(cb.like(cb.lower(root.get(fieldName)), "%" + searchPattern.toLowerCase() + "%"));
        return em.createQuery(query).getSingleResult();
    }

    public long countByFields(String[] fieldNames, Object[] fieldValues) {
        if (fieldNames.length != fieldValues.length) {
            throw new IllegalArgumentException("Field names and values arrays must have the same length");
        }
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        
        Predicate[] predicates = new Predicate[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            predicates[i] = cb.equal(root.get(fieldNames[i]), fieldValues[i]);
        }
        
        query.select(cb.count(root)).where(predicates);
        return em.createQuery(query).getSingleResult();
    }

    // ========== PRIVATE PAGINATION HELPER METHODS ==========

    private List<T> selectAllWithPagination(PaginationRequest request) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);
        
        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            if ("ASC".equalsIgnoreCase(request.getSortDirection())) {
                query.orderBy(cb.asc(root.get(request.getSortBy())));
            } else {
                query.orderBy(cb.desc(root.get(request.getSortBy())));
            }
        }
        
        return em.createQuery(query)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.getPageSize())
                .getResultList();
    }

    private List<T> selectByFieldWithPagination(String fieldName, Object value, PaginationRequest request) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root).where(cb.equal(root.get(fieldName), value));
        
        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            if ("ASC".equalsIgnoreCase(request.getSortDirection())) {
                query.orderBy(cb.asc(root.get(request.getSortBy())));
            } else {
                query.orderBy(cb.desc(root.get(request.getSortBy())));
            }
        }
        
        return em.createQuery(query)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.getPageSize())
                .getResultList();
    }

    private List<T> selectByFieldLikeWithPagination(String fieldName, String searchPattern, PaginationRequest request) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root)
             .where(cb.like(cb.lower(root.get(fieldName)), "%" + searchPattern.toLowerCase() + "%"));
        
        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            if ("ASC".equalsIgnoreCase(request.getSortDirection())) {
                query.orderBy(cb.asc(root.get(request.getSortBy())));
            } else {
                query.orderBy(cb.desc(root.get(request.getSortBy())));
            }
        }
        
        return em.createQuery(query)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.getPageSize())
                .getResultList();
    }

    private List<T> selectByFieldsWithPagination(String[] fieldNames, Object[] fieldValues, PaginationRequest request) {
        if (fieldNames.length != fieldValues.length) {
            throw new IllegalArgumentException("Field names and values arrays must have the same length");
        }
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        Predicate[] predicates = new Predicate[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            predicates[i] = cb.equal(root.get(fieldNames[i]), fieldValues[i]);
        }
        
        query.select(root).where(predicates);
        
        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            if ("ASC".equalsIgnoreCase(request.getSortDirection())) {
                query.orderBy(cb.asc(root.get(request.getSortBy())));
            } else {
                query.orderBy(cb.desc(root.get(request.getSortBy())));
            }
        }
        
        return em.createQuery(query)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.getPageSize())
                .getResultList();
    }

    // ========== TEMPLATE METHODS ==========

    /**
     * Template method for updating entity fields.
     * Subclasses should override this to implement specific update logic.
     */
    protected abstract void updateEntity(T existing, T updated);
} 