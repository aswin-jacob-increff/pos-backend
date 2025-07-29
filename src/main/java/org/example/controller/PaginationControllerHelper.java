package org.example.controller;

import org.example.exception.ApiException;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.model.data.PaginationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.util.function.Function;

/**
 * Helper class to reduce controller bulkiness by providing common pagination patterns.
 * This class encapsulates the repetitive pagination logic found in controllers.
 */
public class PaginationControllerHelper {

    /**
     * Creates a pagination request from common controller parameters.
     */
    public static PaginationRequest createPaginationRequest(
            Integer page, 
            Integer size, 
            String sortBy, 
            String sortDirection) {
        return new PaginationRequest(page, size, sortBy, sortDirection);
    }

    /**
     * Generic method to handle paginated endpoints with standard error handling.
     * This method reduces the repetitive try-catch blocks in controllers.
     */
    public static <T> ResponseEntity<PaginationResponse<T>> handlePaginatedRequest(
            String endpointName,
            PaginationRequest request,
            Function<PaginationRequest, PaginationResponse<T>> paginationFunction) {
        
        logEndpointCall(endpointName, request);
        
        try {
            PaginationResponse<T> response = paginationFunction.apply(request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get paginated data: " + e.getMessage());
        }
    }

    /**
     * Generic method to handle paginated endpoints with field-based queries.
     */
    public static <T> ResponseEntity<PaginationResponse<T>> handleFieldPaginatedRequest(
            String endpointName,
            String fieldName,
            Object fieldValue,
            PaginationRequest request,
            Function<PaginationQuery, PaginationResponse<T>> paginationFunction) {
        
        logFieldEndpointCall(endpointName, fieldName, fieldValue, request);
        
        try {
            PaginationQuery query = PaginationQuery.byField(fieldName, fieldValue, request);
            PaginationResponse<T> response = paginationFunction.apply(query);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get paginated data by " + fieldName + ": " + e.getMessage());
        }
    }

    /**
     * Generic method to handle paginated endpoints with LIKE queries.
     */
    public static <T> ResponseEntity<PaginationResponse<T>> handleLikePaginatedRequest(
            String endpointName,
            String fieldName,
            String searchPattern,
            PaginationRequest request,
            Function<PaginationQuery, PaginationResponse<T>> paginationFunction) {
        
        logLikeEndpointCall(endpointName, fieldName, searchPattern, request);
        
        try {
            PaginationQuery query = PaginationQuery.byFieldLike(fieldName, searchPattern, request);
            PaginationResponse<T> response = paginationFunction.apply(query);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to search paginated data by " + fieldName + ": " + e.getMessage());
        }
    }

    /**
     * Generic method to handle paginated endpoints with multiple field queries.
     */
    public static <T> ResponseEntity<PaginationResponse<T>> handleMultiFieldPaginatedRequest(
            String endpointName,
            String[] fieldNames,
            Object[] fieldValues,
            PaginationRequest request,
            Function<PaginationQuery, PaginationResponse<T>> paginationFunction) {
        
        logMultiFieldEndpointCall(endpointName, fieldNames, fieldValues, request);
        
        try {
            PaginationQuery query = PaginationQuery.byFields(fieldNames, fieldValues, request);
            PaginationResponse<T> response = paginationFunction.apply(query);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get paginated data by multiple fields: " + e.getMessage());
        }
    }

    // ========== PRIVATE HELPER METHODS ==========

    private static void logEndpointCall(String endpointName, PaginationRequest request) {
    }

    private static void logFieldEndpointCall(String endpointName, String fieldName, Object fieldValue, 
                                           PaginationRequest request) {
    }

    private static void logLikeEndpointCall(String endpointName, String fieldName, String searchPattern, 
                                          PaginationRequest request) {
    }

    private static void logMultiFieldEndpointCall(String endpointName, String[] fieldNames, Object[] fieldValues, 
                                                PaginationRequest request) {
        
        StringBuilder fields = new StringBuilder();
        for (int i = 0; i < fieldNames.length; i++) {
            if (i > 0) fields.append(", ");
            fields.append(fieldNames[i]).append(" = ").append(fieldValues[i]);
        }
    }
} 