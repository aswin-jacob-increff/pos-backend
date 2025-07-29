package org.example.dto;

import org.example.exception.ApiException;
import org.example.api.AbstractApi;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.model.data.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

@Component
public abstract class AbstractDto<T, F, D> {

    @Autowired
    protected AbstractApi<T> api;

    // ========== BASIC CRUD OPERATIONS ==========

    public D add(@Valid F form) {
        preprocess(form);
        T entity = convertFormToEntity(form);
        api.add(entity);
        return convertEntityToData(entity);
    }

    public D get(Integer id) {
        validateId(id);
        T entity = api.get(id);
        return convertEntityToData(entity);
    }

    public List<D> getAll() {
        List<T> entities = api.getAll();
        return entities.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional
    public D update(Integer id, @Valid F form) {
        validateId(id);
        preprocess(form);
        T entity = convertFormToEntity(form);
        api.update(id, entity);
        return convertEntityToData(api.get(id));
    }

    public void delete(Integer id) {
        validateId(id);
        api.delete(id);
    }

    // ========== ENHANCED QUERY METHODS ==========

    /**
     * Generic method to get by field with validation
     */
    public D getByField(String fieldName, Object value) {
        validateFieldValue(fieldName, value);
        T entity = api.getByField(fieldName, value);
        return convertEntityToData(entity);
    }

    /**
     * Generic method to find by field (returns null if not found)
     */
    public D findByField(String fieldName, Object value) {
        T entity = api.findByField(fieldName, value);
        return entity != null ? convertEntityToData(entity) : null;
    }

    /**
     * Generic method to get by field with Optional return
     */
    public Optional<D> getByFieldOptional(String fieldName, Object value) {
        T entity = api.findByField(fieldName, value);
        return entity != null ? Optional.of(convertEntityToData(entity)) : Optional.empty();
    }

    /**
     * Generic method to get by multiple fields
     */
    public List<D> getByFields(String[] fieldNames, Object[] values) {
        validateFieldArrays(fieldNames, values);
        List<T> entities = api.getByFields(fieldNames, values);
        return entities.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    /**
     * Generic method to get by field with partial string matching
     */
    public List<D> getByFieldLike(String fieldName, String searchPattern) {
        validateSearchPattern(searchPattern);
        List<T> entities = api.getByFieldLikeWithValidation(fieldName, searchPattern, fieldName);
        return entities.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }



    /**
     * Generic method to check if entity exists by field
     */
    public boolean existsByField(String fieldName, Object value) {
        return api.findByField(fieldName, value) != null;
    }

    /**
     * Generic method to check if entity exists by multiple fields
     */
    public boolean existsByFields(String[] fieldNames, Object[] values) {
        validateFieldArrays(fieldNames, values);
        List<T> entities = api.getByFields(fieldNames, values);
        return entities != null && !entities.isEmpty();
    }

    // ========== GENERALIZED PAGINATION METHOD ==========

    /**
     * Generalized pagination method that handles all types of queries.
     */
    public PaginationResponse<D> getPaginated(PaginationQuery query) {
        if (query == null) {
            throw new ApiException("Pagination query cannot be null");
        }
        
        PaginationResponse<T> paginatedEntities = api.getPaginated(query);
        
        List<D> dataList = paginatedEntities.getContent().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
        
        return new PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
    }

    /**
     * Convenience method for paginated queries by field
     */
    public PaginationResponse<D> getByFieldPaginated(String fieldName, Object value, PaginationRequest request) {
        return getPaginated(PaginationQuery.byField(fieldName, value, request));
    }

    /**
     * Convenience method for paginated queries by field with like matching
     */
    public PaginationResponse<D> getByFieldLikePaginated(String fieldName, String searchPattern, PaginationRequest request) {
        return getPaginated(PaginationQuery.byFieldLike(fieldName, searchPattern, request));
    }

    /**
     * Convenience method for paginated queries by multiple fields
     */
    public PaginationResponse<D> getByFieldsPaginated(String[] fieldNames, Object[] values, PaginationRequest request) {
        return getPaginated(PaginationQuery.byFields(fieldNames, values, request));
    }

    // ========== BATCH OPERATIONS ==========

    /**
     * Convert a list of entities to data objects
     */
    protected List<D> convertEntitiesToData(List<T> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    /**
     * Convert a list of forms to entities
     */
    protected List<T> convertFormsToEntities(List<F> forms) {
        if (forms == null) {
            return new ArrayList<>();
        }
        return forms.stream()
                .map(this::convertFormToEntity)
                .collect(Collectors.toList());
    }

    // ========== VALIDATION METHODS ==========

    /**
     * Validate field value is not null or empty
     */
    protected void validateFieldValue(String fieldName, Object value) {
        if (value == null) {
            throw new ApiException(fieldName + " cannot be null");
        }
        if (value instanceof String && ((String) value).trim().isEmpty()) {
            throw new ApiException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validate search pattern is not null or empty
     */
    protected void validateSearchPattern(String searchPattern) {
        if (searchPattern == null || searchPattern.trim().isEmpty()) {
            throw new ApiException("Search pattern cannot be null or empty");
        }
    }

    /**
     * Validate field arrays have matching lengths
     */
    protected void validateFieldArrays(String[] fieldNames, Object[] values) {
        if (fieldNames == null || values == null) {
            throw new ApiException("Field names and values cannot be null");
        }
        if (fieldNames.length != values.length) {
            throw new ApiException("Field names and values arrays must have the same length");
        }
    }

    /**
     * Validate ID parameter
     */
    protected void validateId(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException(getEntityName() + " ID cannot be null");
        }
        if (id <= 0) {
            throw new ApiException(getEntityName() + " ID must be positive");
        }
    }

    // ========== TEMPLATE METHODS ==========

    /**
     * Get entity name for error messages
     */
    protected abstract String getEntityName();

    /**
     * Convert form to entity
     */
    protected abstract T convertFormToEntity(F form);

    /**
     * Convert entity to data
     */
    protected abstract D convertEntityToData(T entity);

    /**
     * Preprocess form before conversion
     */
    protected void preprocess(F form) {
        // Default implementation - subclasses can override
    }
} 