package org.example.model.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.example.exception.ApiException;

public class PaginationRequest {
    
    @Min(value = 0, message = "Page number must be 0 or greater")
    private Integer pageNumber = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer pageSize = 20;
    
    private String sortBy;
    private String sortDirection = "ASC"; // ASC or DESC
    
    // Default constructor
    public PaginationRequest() {}
    
    // Constructor with parameters
    public PaginationRequest(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        this.pageNumber = pageNumber != null ? pageNumber : 0;
        this.pageSize = pageSize != null ? pageSize : 20;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection != null ? sortDirection.toUpperCase() : "ASC";
        validate();
    }
    
    // Constructor with just page parameters
    public PaginationRequest(Integer pageNumber, Integer pageSize) {
        this(pageNumber, pageSize, null, "ASC");
    }
    
    // Validation method
    public void validate() {
        if (pageNumber == null || pageNumber < 0) {
            throw new ApiException("Page number must be 0 or greater");
        }
        
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw new ApiException("Page size must be between 1 and 100");
        }
        
        if (sortDirection != null && !sortDirection.equalsIgnoreCase("ASC") && !sortDirection.equalsIgnoreCase("DESC")) {
            throw new ApiException("Sort direction must be either 'ASC' or 'DESC'");
        }
    }
    
    // Calculate offset for SQL queries
    public int getOffset() {
        return pageNumber * pageSize;
    }
    
    // Get sort direction as boolean (true for DESC, false for ASC)
    public boolean isDescending() {
        return "DESC".equalsIgnoreCase(sortDirection);
    }
    
    // Getters and Setters
    public Integer getPageNumber() {
        return pageNumber;
    }
    
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
        validate();
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        validate();
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection != null ? sortDirection.toUpperCase() : "ASC";
        validate();
    }
    
    @Override
    public String toString() {
        return "PaginationRequest{" +
                "pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
} 