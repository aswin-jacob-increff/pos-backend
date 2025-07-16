package org.example.config;

import org.springframework.context.annotation.Configuration;

/**
 * Centralized configuration for API endpoint patterns
 * This class defines the URL patterns for different access levels
 */
@Configuration
public class ApiConfig {
    
    /**
     * API patterns for supervisor-only endpoints
     * These endpoints can only be accessed by users with SUPERVISOR role
     */
    public static final class Supervisor {
        public static final String BASE_PATH = "/api/supervisor";
        public static final String USERS = BASE_PATH + "/users";
        public static final String ORDERS = BASE_PATH + "/orders";
        public static final String PRODUCTS = BASE_PATH + "/products";
        public static final String PRODUCTS_BY_CLIENT = PRODUCTS + "/client";
        public static final String PRODUCTS_BY_CLIENT_ID = PRODUCTS_BY_CLIENT + "/{clientId}";
        public static final String PRODUCTS_BY_CLIENT_NAME = PRODUCTS_BY_CLIENT + "/name/{clientName}";
        public static final String CLIENTS = BASE_PATH + "/clients";
        public static final String INVENTORY = BASE_PATH + "/inventory";
        public static final String REPORTS = BASE_PATH + "/reports";
        public static final String INVOICES = BASE_PATH + "/invoices";
    }
    
    /**
     * API patterns for user endpoints
     * These endpoints can be accessed by both USER and SUPERVISOR roles
     */
    public static final class User {
        public static final String BASE_PATH = "/api/user";
        public static final String ORDERS = BASE_PATH + "/orders";
        public static final String ORDERS_BY_ID = BASE_PATH + "/orders/{id}";
        public static final String ORDERS_BY_DATE_RANGE = BASE_PATH + "/orders/by-date-range";
        public static final String ORDERS_CANCEL = BASE_PATH + "/orders/{id}/cancel";
        public static final String ORDERS_DOWNLOAD_INVOICE = BASE_PATH + "/orders/{id}/download-invoice";
        public static final String CURRENT_USER = BASE_PATH + "/current-user";
        public static final String SIGNUP = BASE_PATH + "/signup";
        public static final String AUTH_STATUS = BASE_PATH + "/auth-status";
    }
} 