package org.example.config;

/**
 * Configuration class containing API endpoint constants
 */
public class ApiConfig {
    
    public static class User {
        public static final String BASE_PATH = "/api/user";
        public static final String SIGNUP = "/api/user/signup";
        public static final String CURRENT_USER = "/api/user/current-user";
        public static final String ORDERS = "/api/user/orders";
        public static final String PRODUCTS = "/api/user/products";
        public static final String INVENTORY = "/api/user/inventory";
        public static final String SEARCH = "/api/user/search";
    }
    
    public static class Supervisor {
        public static final String BASE_PATH = "/api/supervisor";
        public static final String USERS = "/api/supervisor/users";
        public static final String ORDERS = "/api/supervisor/orders";
        public static final String PRODUCTS = "/api/supervisor/products";
        public static final String CLIENTS = "/api/supervisor/clients";
        public static final String INVENTORY = "/api/supervisor/inventory";
        public static final String REPORTS = "/api/supervisor/reports";
    }
} 