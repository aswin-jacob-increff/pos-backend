package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.example.pojo.OrderPojo;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Repository
public class OrderDao extends AbstractDao<OrderPojo> {

    public OrderDao() {
        super(OrderPojo.class);
    }

    @Override
    public OrderPojo select(Integer id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> query = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        query.select(root)
             .where(cb.equal(root.get("id"), id));
        try {
            return em.createQuery(query).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<OrderPojo> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> query = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    @Override
    protected void updateEntity(OrderPojo existing, OrderPojo updated) {
        existing.setDate(updated.getDate());
        existing.setTotal(updated.getTotal());
        existing.setStatus(updated.getStatus());
        // Order items are now managed separately
    }

    public List<OrderPojo> findOrdersByDate(LocalDate date) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> cq = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = cq.from(OrderPojo.class);
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Predicate dateBetween = cb.between(root.get("date"), start, end);
        cq.select(root).where(dateBetween);
        return em.createQuery(cq).getResultList();
    }

    /**
     * Find orders within a date range (inclusive of both start and end dates)
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of orders within the date range, ordered by date ascending
     */
    public List<OrderPojo> findOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> cq = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = cq.from(OrderPojo.class);
        // Convert LocalDate (IST) to UTC Instants for comparison
        java.time.ZoneId istZone = java.time.ZoneId.of("Asia/Kolkata");
        Instant start = startDate.atStartOfDay(istZone).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(istZone).toInstant();
        Predicate dateBetween = cb.between(root.get("date"), start, end);
        cq.select(root)
          .where(dateBetween)
          .orderBy(cb.asc(root.get("date")));
        return em.createQuery(cq).getResultList();
    }

    public LocalDate findEarliestOrderDate() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Instant> cq = cb.createQuery(Instant.class);
        Root<OrderPojo> root = cq.from(OrderPojo.class);
        cq.select(root.get("date")).orderBy(cb.asc(root.get("date")));
        List<Instant> results = em.createQuery(cq).setMaxResults(1).getResultList();
        if (results.isEmpty() || results.get(0) == null) return LocalDate.now(ZoneId.of("Asia/Kolkata"));
        return results.get(0).atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
    }

    public List<OrderPojo> findByUserId(String userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> cq = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = cq.from(OrderPojo.class);
        cq.select(root).where(cb.equal(root.get("userId"), userId)).orderBy(cb.desc(root.get("date")));
        return em.createQuery(cq).getResultList();
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all orders with pagination support, ordered by date descending (most recent first).
     */
    public org.example.model.data.PaginationResponse<OrderPojo> getAllPaginated(org.example.model.form.PaginationRequest request) {
        request = org.example.util.PaginationUtil.validateAndSetDefaults(request);
        
        // Get total count
        long totalElements = countAll();
        
        if (totalElements == 0) {
            return org.example.util.PaginationUtil.createEmptyResponse(request);
        }
        
        // Get paginated results with default sorting by date descending (most recent first)
        List<OrderPojo> content = getAllWithPagination(request);
        
        return org.example.util.PaginationUtil.createResponse(content, totalElements, request);
    }

    /**
     * Get orders by user ID with pagination support, ordered by date descending (most recent first).
     */
    public org.example.model.data.PaginationResponse<OrderPojo> getByUserIdPaginated(String userId, org.example.model.form.PaginationRequest request) {
        request = org.example.util.PaginationUtil.validateAndSetDefaults(request);
        
        // Get total count
        long totalElements = countByUserId(userId);
        
        if (totalElements == 0) {
            return org.example.util.PaginationUtil.createEmptyResponse(request);
        }
        
        // Get paginated results
        List<OrderPojo> content = getByUserIdWithPagination(userId, request);
        
        return org.example.util.PaginationUtil.createResponse(content, totalElements, request);
    }

    /**
     * Get orders by date range with pagination support, ordered by date descending (most recent first).
     */
    public org.example.model.data.PaginationResponse<OrderPojo> getByDateRangePaginated(java.time.LocalDate startDate, java.time.LocalDate endDate, org.example.model.form.PaginationRequest request) {
        request = org.example.util.PaginationUtil.validateAndSetDefaults(request);
        
        // Get total count
        long totalElements = countByDateRange(startDate, endDate);
        
        if (totalElements == 0) {
            return org.example.util.PaginationUtil.createEmptyResponse(request);
        }
        
        // Get paginated results
        List<OrderPojo> content = getByDateRangeWithPagination(startDate, endDate, request);
        
        return org.example.util.PaginationUtil.createResponse(content, totalElements, request);
    }

    // ========== COUNT METHODS ==========

    /**
     * Count orders by user ID.
     */
    public long countByUserId(String userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        query.select(cb.count(root))
             .where(cb.equal(root.get("userId"), userId));
        return em.createQuery(query).getSingleResult();
    }

    /**
     * Count orders by date range.
     */
    public long countByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        
        // Convert LocalDate (IST) to UTC Instants for comparison
        java.time.ZoneId istZone = java.time.ZoneId.of("Asia/Kolkata");
        Instant start = startDate.atStartOfDay(istZone).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(istZone).toInstant();
        
        Predicate dateBetween = cb.between(root.get("date"), start, end);
        query.select(cb.count(root)).where(dateBetween);
        return em.createQuery(query).getSingleResult();
    }

    // ========== SUBSTRING SEARCH METHODS ==========

    /**
     * Find orders by ID substring matching.
     * This allows finding orders where the search term appears exactly as a substring in the order ID.
     * 
     * @param searchId The ID substring to search for
     * @param maxResults Maximum number of results to return
     * @return List of orders where the search term appears as a substring in the ID, ordered by date descending
     */
    public List<OrderPojo> findOrdersBySubstringId(String searchId, int maxResults) {
        if (searchId == null || searchId.trim().isEmpty()) {
            return List.of();
        }
        
        String searchTerm = searchId.trim();
        
        // Get all orders for substring comparison
        List<OrderPojo> allOrders = selectAll();
        List<OrderPojo> matchingOrders = new java.util.ArrayList<>();
        
        for (OrderPojo order : allOrders) {
            String orderIdStr = String.valueOf(order.getId());
            if (orderIdStr.contains(searchTerm)) {
                matchingOrders.add(order);
                if (matchingOrders.size() >= maxResults) {
                    break;
                }
            }
        }
        
        // Sort by date descending (most recent first)
        matchingOrders.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        return matchingOrders;
    }

    /**
     * Find orders by ID substring with pagination support.
     * 
     * @param searchId The ID substring to search for
     * @param request Pagination request
     * @return Paginated response with orders containing the substring, ordered by date descending
     */
    public org.example.model.data.PaginationResponse<OrderPojo> findOrdersBySubstringIdPaginated(
            String searchId, 
            org.example.model.form.PaginationRequest request) {
        
        if (searchId == null || searchId.trim().isEmpty()) {
            return org.example.util.PaginationUtil.createEmptyResponse(request);
        }
        
        String searchTerm = searchId.trim();
        
        // Get all matching orders
        List<OrderPojo> allMatchingOrders = getAllSubstringMatches(searchTerm);
        
        if (allMatchingOrders.isEmpty()) {
            return org.example.util.PaginationUtil.createEmptyResponse(request);
        }
        
        // Apply pagination
        int totalElements = allMatchingOrders.size();
        int pageSize = request.getPageSize();
        int pageNumber = request.getPageNumber();
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalElements);
        
        List<OrderPojo> paginatedContent;
        if (startIndex >= totalElements) {
            paginatedContent = List.of();
        } else {
            paginatedContent = allMatchingOrders.subList(startIndex, endIndex);
        }
        
        return new org.example.model.data.PaginationResponse<>(
            paginatedContent,
            totalElements,
            pageNumber,
            pageSize
        );
    }

    /**
     * Count orders by ID substring.
     * 
     * @param searchId The ID substring to search for
     * @return Number of orders containing the substring
     */
    public long countOrdersBySubstringId(String searchId) {
        if (searchId == null || searchId.trim().isEmpty()) {
            return 0;
        }
        
        return getAllSubstringMatches(searchId.trim()).size();
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Get all orders with pagination applied, ordered by date descending (most recent first).
     */
    private List<OrderPojo> getAllWithPagination(org.example.model.form.PaginationRequest request) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> query = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        query.select(root);
        
        // Apply sorting - default to date descending (most recent first)
        String sortBy = request.getSortBy();
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "date"; // Default sort field
        }
        
        if (request.isDescending()) {
            query.orderBy(cb.desc(root.get(sortBy)));
        } else {
            query.orderBy(cb.asc(root.get(sortBy)));
        }
        
        return em.createQuery(query)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.getPageSize())
                .getResultList();
    }

    /**
     * Get orders by user ID with pagination applied, ordered by date descending (most recent first).
     */
    private List<OrderPojo> getByUserIdWithPagination(String userId, org.example.model.form.PaginationRequest request) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> query = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        query.select(root).where(cb.equal(root.get("userId"), userId));
        
        // Apply sorting - default to date descending (most recent first)
        String sortBy = request.getSortBy();
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "date"; // Default sort field
        }
        
        if (request.isDescending()) {
            query.orderBy(cb.desc(root.get(sortBy)));
        } else {
            query.orderBy(cb.asc(root.get(sortBy)));
        }
        
        return em.createQuery(query)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.getPageSize())
                .getResultList();
    }

    /**
     * Get orders by date range with pagination applied, ordered by date descending (most recent first).
     */
    private List<OrderPojo> getByDateRangeWithPagination(java.time.LocalDate startDate, java.time.LocalDate endDate, org.example.model.form.PaginationRequest request) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> query = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);
        
        // Convert LocalDate (IST) to UTC Instants for comparison
        java.time.ZoneId istZone = java.time.ZoneId.of("Asia/Kolkata");
        Instant start = startDate.atStartOfDay(istZone).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(istZone).toInstant();
        
        Predicate dateBetween = cb.between(root.get("date"), start, end);
        query.select(root).where(dateBetween);
        
        // Apply sorting - default to date descending (most recent first)
        String sortBy = request.getSortBy();
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "date"; // Default sort field
        }
        
        if (request.isDescending()) {
            query.orderBy(cb.desc(root.get(sortBy)));
        } else {
            query.orderBy(cb.asc(root.get(sortBy)));
        }
        
        return em.createQuery(query)
                .setFirstResult(request.getOffset())
                .setMaxResults(request.getPageSize())
                .getResultList();
    }

    /**
     * Get all orders that contain the search term as a substring in their ID.
     */
    private List<OrderPojo> getAllSubstringMatches(String searchTerm) {
        List<OrderPojo> allOrders = selectAll();
        List<OrderPojo> matchingOrders = new java.util.ArrayList<>();
        
        for (OrderPojo order : allOrders) {
            String orderIdStr = String.valueOf(order.getId());
            if (orderIdStr.contains(searchTerm)) {
                matchingOrders.add(order);
            }
        }
        
        // Sort by date descending (most recent first)
        matchingOrders.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        return matchingOrders;
    }
}
