package org.example.util;

import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import java.util.List;

public class PaginationUtil {
    
    /**
     * Create a pagination response from a list of content and pagination request
     */
    public static <T> PaginationResponse<T> createResponse(List<T> content, long totalElements, PaginationRequest request) {
        return new PaginationResponse<>(
            content,
            totalElements,
            request.getPageNumber(),
            request.getPageSize()
        );
    }
    
    /**
     * Create an empty pagination response
     */
    public static <T> PaginationResponse<T> createEmptyResponse(PaginationRequest request) {
        return PaginationResponse.empty(request.getPageNumber(), request.getPageSize());
    }
    
    /**
     * Validate pagination request and set defaults if needed
     */
    public static PaginationRequest validateAndSetDefaults(PaginationRequest request) {
        if (request == null) {
            return new PaginationRequest();
        }
        
        // Set defaults if not provided
        if (request.getPageNumber() == null) {
            request.setPageNumber(0);
        }
        
        if (request.getPageSize() == null) {
            request.setPageSize(20);
        }
        
        if (request.getSortDirection() == null) {
            request.setSortDirection("ASC");
        }
        
        // Validate the request
        request.validate();
        
        return request;
    }
    
    /**
     * Calculate the total number of pages
     */
    public static int calculateTotalPages(long totalElements, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageSize);
    }
    
    /**
     * Check if a page number is valid for the given total pages
     */
    public static boolean isValidPage(int pageNumber, int totalPages) {
        return pageNumber >= 0 && pageNumber < totalPages;
    }
    
    /**
     * Get the start index for a page (0-based)
     */
    public static int getStartIndex(int pageNumber, int pageSize) {
        return pageNumber * pageSize;
    }
    
    /**
     * Get the end index for a page (exclusive)
     */
    public static int getEndIndex(int pageNumber, int pageSize) {
        return (pageNumber + 1) * pageSize;
    }
    
    /**
     * Check if there are more pages after the current page
     */
    public static boolean hasNextPage(int currentPage, int totalPages) {
        return currentPage < totalPages - 1;
    }
    
    /**
     * Check if there are pages before the current page
     */
    public static boolean hasPreviousPage(int currentPage) {
        return currentPage > 0;
    }
} 