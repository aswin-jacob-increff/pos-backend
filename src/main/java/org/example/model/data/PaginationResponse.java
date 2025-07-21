package org.example.model.data;

import java.util.List;

public class PaginationResponse<T> {
    
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;
    
    // Default constructor
    public PaginationResponse() {}
    
    // Constructor with all parameters
    public PaginationResponse(List<T> content, long totalElements, int currentPage, int pageSize) {
        this.content = content;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
        this.isFirst = currentPage == 0;
        this.isLast = currentPage >= totalPages - 1;
    }
    
    // Constructor for empty results
    public PaginationResponse(int currentPage, int pageSize) {
        this(List.of(), 0, currentPage, pageSize);
    }
    
    // Static factory method for empty results
    public static <T> PaginationResponse<T> empty(int currentPage, int pageSize) {
        return new PaginationResponse<>(currentPage, pageSize);
    }
    
    // Static factory method for single page results
    public static <T> PaginationResponse<T> of(List<T> content) {
        return new PaginationResponse<>(content, content.size(), 0, content.size());
    }
    
    // Getters and Setters
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
        this.isFirst = currentPage == 0;
        this.isLast = currentPage >= totalPages - 1;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
        this.isFirst = currentPage == 0;
        this.isLast = currentPage >= totalPages - 1;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
        this.isFirst = currentPage == 0;
        this.isLast = currentPage >= totalPages - 1;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    public boolean isFirst() {
        return isFirst;
    }
    
    public void setIsFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }
    
    public boolean isLast() {
        return isLast;
    }
    
    public void setIsLast(boolean isLast) {
        this.isLast = isLast;
    }
    
    // Utility methods
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
    
    public int getNumberOfElements() {
        return content != null ? content.size() : 0;
    }
    
    @Override
    public String toString() {
        return "PaginationResponse{" +
                "contentSize=" + (content != null ? content.size() : 0) +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                ", isFirst=" + isFirst +
                ", isLast=" + isLast +
                '}';
    }
} 