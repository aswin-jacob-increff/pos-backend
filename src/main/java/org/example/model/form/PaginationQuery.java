package org.example.model.form;

import java.util.Objects;

/**
 * Represents a pagination query with different types of filtering.
 * This class helps reduce code duplication by providing a unified way to handle
 * different types of pagination queries across all layers.
 */
public class PaginationQuery {
    
    public enum QueryType {
        ALL,           // Get all entities
        BY_FIELD,      // Get by exact field match
        BY_FIELD_LIKE, // Get by field with LIKE pattern
        BY_FIELDS      // Get by multiple field matches
    }
    
    private final QueryType queryType;
    private final String fieldName;
    private final Object fieldValue;
    private final String[] fieldNames;
    private final Object[] fieldValues;
    private final String searchPattern;
    private final PaginationRequest paginationRequest;
    
    // Private constructor to enforce builder pattern
    private PaginationQuery(Builder builder) {
        this.queryType = builder.queryType;
        this.fieldName = builder.fieldName;
        this.fieldValue = builder.fieldValue;
        this.fieldNames = builder.fieldNames;
        this.fieldValues = builder.fieldValues;
        this.searchPattern = builder.searchPattern;
        this.paginationRequest = builder.paginationRequest;
    }
    
    // Static factory methods for different query types
    public static PaginationQuery all(PaginationRequest request) {
        return new Builder()
                .queryType(QueryType.ALL)
                .paginationRequest(request)
                .build();
    }
    
    public static PaginationQuery byField(String fieldName, Object fieldValue, PaginationRequest request) {
        return new Builder()
                .queryType(QueryType.BY_FIELD)
                .fieldName(fieldName)
                .fieldValue(fieldValue)
                .paginationRequest(request)
                .build();
    }
    
    public static PaginationQuery byFieldLike(String fieldName, String searchPattern, PaginationRequest request) {
        return new Builder()
                .queryType(QueryType.BY_FIELD_LIKE)
                .fieldName(fieldName)
                .searchPattern(searchPattern)
                .paginationRequest(request)
                .build();
    }
    
    public static PaginationQuery byFields(String[] fieldNames, Object[] fieldValues, PaginationRequest request) {
        return new Builder()
                .queryType(QueryType.BY_FIELDS)
                .fieldNames(fieldNames)
                .fieldValues(fieldValues)
                .paginationRequest(request)
                .build();
    }
    
    // Getters
    public QueryType getQueryType() {
        return queryType;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
    
    public String[] getFieldNames() {
        return fieldNames;
    }
    
    public Object[] getFieldValues() {
        return fieldValues;
    }
    
    public String getSearchPattern() {
        return searchPattern;
    }
    
    public PaginationRequest getPaginationRequest() {
        return paginationRequest;
    }
    
    // Validation
    public void validate() {
        if (paginationRequest == null) {
            throw new IllegalArgumentException("Pagination request cannot be null");
        }
        
        switch (queryType) {
            case BY_FIELD:
                if (fieldName == null || fieldName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Field name cannot be null or empty for BY_FIELD query");
                }
                if (fieldValue == null) {
                    throw new IllegalArgumentException("Field value cannot be null for BY_FIELD query");
                }
                break;
            case BY_FIELD_LIKE:
                if (fieldName == null || fieldName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Field name cannot be null or empty for BY_FIELD_LIKE query");
                }
                if (searchPattern == null || searchPattern.trim().isEmpty()) {
                    throw new IllegalArgumentException("Search pattern cannot be null or empty for BY_FIELD_LIKE query");
                }
                break;
            case BY_FIELDS:
                if (fieldNames == null || fieldNames.length == 0) {
                    throw new IllegalArgumentException("Field names cannot be null or empty for BY_FIELDS query");
                }
                if (fieldValues == null || fieldValues.length == 0) {
                    throw new IllegalArgumentException("Field values cannot be null or empty for BY_FIELDS query");
                }
                if (fieldNames.length != fieldValues.length) {
                    throw new IllegalArgumentException("Field names and field values arrays must have the same length");
                }
                break;
            case ALL:
                // No additional validation needed
                break;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginationQuery that = (PaginationQuery) o;
        return queryType == that.queryType &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(fieldValue, that.fieldValue) &&
                Objects.equals(searchPattern, that.searchPattern) &&
                Objects.equals(paginationRequest, that.paginationRequest);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(queryType, fieldName, fieldValue, searchPattern, paginationRequest);
    }
    
    @Override
    public String toString() {
        return "PaginationQuery{" +
                "queryType=" + queryType +
                ", fieldName='" + fieldName + '\'' +
                ", fieldValue=" + fieldValue +
                ", searchPattern='" + searchPattern + '\'' +
                ", paginationRequest=" + paginationRequest +
                '}';
    }
    
    // Builder class
    public static class Builder {
        private QueryType queryType;
        private String fieldName;
        private Object fieldValue;
        private String[] fieldNames;
        private Object[] fieldValues;
        private String searchPattern;
        private PaginationRequest paginationRequest;
        
        public Builder queryType(QueryType queryType) {
            this.queryType = queryType;
            return this;
        }
        
        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }
        
        public Builder fieldValue(Object fieldValue) {
            this.fieldValue = fieldValue;
            return this;
        }
        
        public Builder fieldNames(String[] fieldNames) {
            this.fieldNames = fieldNames;
            return this;
        }
        
        public Builder fieldValues(Object[] fieldValues) {
            this.fieldValues = fieldValues;
            return this;
        }
        
        public Builder searchPattern(String searchPattern) {
            this.searchPattern = searchPattern;
            return this;
        }
        
        public Builder paginationRequest(PaginationRequest paginationRequest) {
            this.paginationRequest = paginationRequest;
            return this;
        }
        
        public PaginationQuery build() {
            PaginationQuery query = new PaginationQuery(this);
            query.validate();
            return query;
        }
    }
} 