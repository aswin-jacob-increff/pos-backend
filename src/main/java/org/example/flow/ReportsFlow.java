package org.example.flow;

import org.example.pojo.OrderItemPojo;
import org.example.pojo.DaySalesPojo;
import org.example.pojo.OrderPojo;
import org.example.dao.OrderItemDao;
import org.example.dao.DaySalesDao;
import org.example.api.OrderApi;
import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ReportsFlow {

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private DaySalesDao daySalesDao;

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    /**
     * Get all order items for reporting purposes
     */
    public List<OrderItemPojo> getAllOrderItems() {
        return orderItemDao.selectAll();
    }

    /**
     * Get all day sales for reporting purposes
     */
    public List<DaySalesPojo> getAllDaySales() {
        return daySalesDao.selectAll();
    }

    /**
     * Get day sales by date range
     */
    public List<DaySalesPojo> getDaySalesByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        return daySalesDao.findByDateRange(startDate, endDate);
    }

    /**
     * Get order by ID for reporting purposes
     */
    public OrderPojo getOrder(Integer orderId) {
        if (orderId == null) {
            throw new ApiException("Order ID cannot be null");
        }
        return orderApi.get(orderId);
    }

    /**
     * Get product by ID for reporting purposes
     */
    public org.example.pojo.ProductPojo getProduct(Integer productId) {
        if (productId == null) {
            throw new ApiException("Product ID cannot be null");
        }
        return productApi.get(productId);
    }

    /**
     * Get client by ID for reporting purposes
     */
    public org.example.pojo.ClientPojo getClient(Integer clientId) {
        if (clientId == null) {
            throw new ApiException("Client ID cannot be null");
        }
        return clientApi.get(clientId);
    }
} 