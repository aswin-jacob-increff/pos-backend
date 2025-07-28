package org.example.dto;

import org.example.flow.OrderFlow;
import org.example.api.InvoiceApi;
import org.example.api.ClientApi;
import org.example.api.ProductApi;

import org.example.dao.OrderItemDao;
import org.example.model.data.OrderData;
import org.example.model.enums.OrderStatus;
import org.example.model.form.OrderForm;
import org.example.model.data.OrderItemData;
import org.example.model.form.OrderItemForm;
import org.example.model.data.InvoiceData;
import org.example.model.data.InvoiceItemData;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.pojo.InvoicePojo;
import org.example.exception.ApiException;
import org.example.util.TimeUtil;
import org.example.util.Base64ToPdfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class OrderDto extends AbstractDto<OrderPojo, OrderForm, OrderData> {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InvoiceApi invoiceApi;
    
    @Autowired
    private OrderItemDao orderItemDao;
    
    @Autowired
    private ClientApi clientApi;



    @Override
    protected String getEntityName() {
        return "Order";
    }

    @Override
    protected OrderPojo convertFormToEntity(OrderForm orderForm) {
        OrderPojo orderPojo = new OrderPojo();
        // Convert LocalDateTime (IST from frontend) to Instant (UTC) for DB storage
        orderPojo.setDate(orderForm.getDate() != null ? TimeUtil.toUTC(orderForm.getDate()) : null);
        orderPojo.setTotal(0.0);
        orderPojo.setUserId(orderForm.getUserId());
        // Note: Order items are now managed separately in the order creation process
        // The order is created first, then items are added with orderId reference
        return orderPojo;
    }

    @Override
    protected OrderData convertEntityToData(OrderPojo orderPojo) {
        if (orderPojo == null) {
            throw new ApiException("Order cannot be null");
        }
        
        OrderData orderData = new OrderData();
        orderData.setId(orderPojo.getId());
        // Convert UTC Instant from DB to IST LocalDateTime for frontend
        orderData.setDate(TimeUtil.toIST(orderPojo.getDate()));
        orderData.setTotal(orderPojo.getTotal());
        orderData.setStatus(orderPojo.getStatus());
        orderData.setUserId(orderPojo.getUserId());
        
        // Fetch order items for this order using internal method
        try {
            System.out.println("Fetching order items for order ID: " + orderPojo.getId());
            List<OrderItemData> orderItems = getOrderItemsByOrderId(orderPojo.getId());
            System.out.println("Found " + orderItems.size() + " order items for order " + orderPojo.getId());
            orderData.setOrderItemDataList(orderItems);
        } catch (Exception e) {
            // If order items cannot be fetched, set empty list
            System.out.println("Warning: Could not fetch order items for order " + orderPojo.getId() + ": " + e.getMessage());
            e.printStackTrace();
            orderData.setOrderItemDataList(new ArrayList<>());
        }
        
        return orderData;
    }

    // Custom methods

    public Resource downloadInvoice(Integer orderId) {
        if (orderId == null) {
            throw new ApiException("Order ID cannot be null");
        }
        OrderPojo orderPojo = api.get(orderId);
        
        try {
            System.out.println("Starting invoice download for order: " + orderId);
            System.out.println("Current order status: " + orderPojo.getStatus());
            
            // Clean up any duplicate invoices for this order
            invoiceApi.cleanupDuplicateInvoices(orderId);
            
            // Check if invoice already exists in database
            InvoicePojo existingInvoice = invoiceApi.getByOrderId(orderId);
            if (existingInvoice != null) {
                System.out.println("Invoice exists in database, checking file...");
                // Invoice exists, try to get the file
                try {
                    return orderFlow.getInvoiceFile(orderId);
                } catch (Exception e) {
                    // File doesn't exist, regenerate it
                    System.out.println("Invoice file not found, regenerating... Error: " + e.getMessage());
                }
            } else {
                System.out.println("No invoice found in database, creating new one...");
            }
            
            // Get order items for this order
            List<OrderItemPojo> orderItems = orderItemDao.selectByOrderId(orderId);
            
            // Create InvoiceData object to send to invoice service
            InvoiceData invoiceData = new InvoiceData();
            invoiceData.setOrderId(orderPojo.getId());
            invoiceData.setTotal(orderPojo.getTotal());
            invoiceData.setTotalQuantity(orderItems.stream()
                    .mapToInt(OrderItemPojo::getQuantity).sum());
            invoiceData.setId(orderPojo.getId());
            // Convert UTC Instant to IST String for invoice in dd/mm/yyyy hh:mm format
            invoiceData.setDate(orderPojo.getDate() != null ? 
                formatDateForInvoice(TimeUtil.toIST(orderPojo.getDate())) : null);

            // Convert order items to invoice items
            List<InvoiceItemData> invoiceItemDataList = new ArrayList<>();
            for (OrderItemPojo itemPojo : orderItems) {
                InvoiceItemData itemData = new InvoiceItemData();
                itemData.setId(itemPojo.getId());
                itemData.setProductId(itemPojo.getProductId());
                
                // Fetch product information using productId
                String productName = "Unknown";
                String productBarcode = "Unknown";
                String clientName = "Unknown";
                Integer clientId = null;
                
                try {
                    ProductPojo product = productApi.get(itemPojo.getProductId());
                    if (product != null) {
                        productName = product.getName() != null ? product.getName() : "Unknown";
                        productBarcode = product.getBarcode() != null ? product.getBarcode() : "Unknown";
                        clientId = product.getClientId();
                        
                        // Fetch client information
                        if (product.getClientId() != null && product.getClientId() > 0) {
                            try {
                                ClientPojo client = clientApi.get(product.getClientId());
                                if (client != null) {
                                    clientName = client.getClientName() != null ? client.getClientName() : "Unknown";
                                }
                            } catch (Exception e) {
                                // Client not found, use "Unknown"
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Warning: Could not find product for ID " + itemPojo.getProductId() + ", using default values");
                }
                
                itemData.setProductName(productName);
                itemData.setProductBarcode(productBarcode);
                itemData.setClientId(clientId);
                itemData.setClientName(clientName);
                itemData.setPrice(itemPojo.getSellingPrice());
                itemData.setQuantity(itemPojo.getQuantity());
                itemData.setAmount(itemPojo.getAmount());
                invoiceItemDataList.add(itemData);
            }
            invoiceData.setInvoiceItemPojoList(invoiceItemDataList);

            System.out.println("Calling external invoice service...");
            // Call invoice service
            String invoiceAppUrl = "http://localhost:8081/api/invoice";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<InvoiceData> entity = new HttpEntity<>(invoiceData, headers);

            ResponseEntity<String> response;
            try {
                response = restTemplate.postForEntity(invoiceAppUrl, entity, String.class);
                System.out.println("Invoice service response status: " + response.getStatusCode());
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // Connection refused, timeout, or network issues
                System.out.println("Network error calling invoice service: " + e.getMessage());
                throw new ApiException("Invoice service is not available. Please try again later. Error: " + e.getMessage());
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // 4ors (client errors)
                System.out.println("HTTP client error calling invoice service: " + e.getMessage());
                throw new ApiException("Invoice service returned an error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            } catch (org.springframework.web.client.HttpServerErrorException e) {
                // 5ors (server errors)
                System.out.println("HTTP server error calling invoice service: " + e.getMessage());
                throw new ApiException("Invoice service is experiencing issues. Please try again later. Error: " + e.getStatusCode());
            } catch (org.springframework.web.client.RestClientException e) {
                // Other RestTemplate exceptions
                System.out.println("RestTemplate error calling invoice service: " + e.getMessage());
                throw new ApiException("Failed to communicate with invoice service: " + e.getMessage());
            } catch (Exception e) {
                // Any other unexpected exceptions
                e.printStackTrace();
                System.out.println("Unexpected error calling invoice service: " + e.getMessage());
                throw new ApiException("Failed to connect to invoice service at " + invoiceAppUrl + ": " + e.getMessage());
            }

            if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("Invoice service call successful, saving PDF...");
                // Save PDF file
                String fileName = "order-" + orderId + ".pdf";
                String savePath = "src/main/resources/invoice/" + fileName;
                
                try {
                    Base64ToPdfUtil.saveBase64AsPdf(response.getBody(), savePath);
                    System.out.println("PDF saved successfully to: " + savePath);
                } catch (Exception e) {
                    System.out.println("Error saving PDF: " + e.getMessage());
                    throw new ApiException("Failed to save invoice PDF: " + e.getMessage());
                }

                System.out.println("Saving invoice to database...");
                // Create and save invoice entity to database using InvoiceApi
                InvoicePojo invoicePojo = new InvoicePojo();
                invoicePojo.setOrderId(orderId);
                invoicePojo.setFilePath("/invoice/" + fileName);
                invoicePojo.setInvoiceId(orderId.toString());
                invoiceApi.add(invoicePojo);
                System.out.println("Invoice saved to database successfully");

                System.out.println("Updating order status to INVOICED...");
                // Update order status to INVOICED
                orderFlow.updateStatus(orderId, OrderStatus.INVOICED);

                // Convert base64 to PDF bytes for immediate return
                byte[] pdfBytes = java.util.Base64.getDecoder().decode(response.getBody());
                
                // Create a resource from the byte array
                return new org.springframework.core.io.ByteArrayResource(pdfBytes) {
                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                };
            } else {
                System.out.println("Invoice service returned error. Status: " + (response != null ? response.getStatusCode() : "null"));
                throw new ApiException("Failed to fetch invoice from invoice service. Status: " + (response != null ? response.getStatusCode() : "null"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in downloadInvoice: " + e.getMessage());
            throw new ApiException("Failed to download invoice: " + e.getMessage());
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public OrderData add(@Valid OrderForm form) {
        if (Objects.isNull(form)) {
            throw new ApiException("Order form cannot be null");
        }
        preprocess(form);
        OrderPojo orderPojo = convertFormToEntity(form);
        
        // Create the order first
        orderFlow.add(orderPojo);
        
        // Now create the order items
        if (form.getOrderItemFormList() != null && !form.getOrderItemFormList().isEmpty()) {
            System.out.println("Creating " + form.getOrderItemFormList().size() + " order items for order " + orderPojo.getId());
            
            double totalAmount = 0.0;
            for (OrderItemForm itemForm : form.getOrderItemFormList()) {
                // Set the order ID for each item
                itemForm.setOrderId(orderPojo.getId());
                
                            // Create order item using internal method - this will trigger preprocessing
            OrderItemData orderItemData = addOrderItem(itemForm);
                totalAmount += orderItemData.getAmount();
            }
            
            // Update order total
            orderPojo.setTotal(totalAmount);
            orderFlow.update(orderPojo.getId(), orderPojo);
            
            System.out.println("Created order items successfully. Total amount: " + totalAmount);
        }
        
        return convertEntityToData(orderPojo);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public OrderData update(Integer id, @Valid OrderForm form) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order ID cannot be null");
        }
        if (Objects.isNull(form)) {
            throw new ApiException("Order form cannot be null");
        }
        return super.update(id, form);
    }

    @Override
    public OrderData get(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order ID cannot be null");
        }
        return super.get(id);
    }


    
    /**
     * Get orders within a date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of order data within the date range
     */
    public List<OrderData> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        
        List<OrderPojo> orderPojos = orderFlow.getOrdersByDateRange(startDate, endDate);
        List<OrderData> orderDataList = new ArrayList<>();
        
        for (OrderPojo orderPojo : orderPojos) {
            orderDataList.add(convertEntityToData(orderPojo));
        }
        
        return orderDataList;
    }

    public List<OrderData> getOrdersByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ApiException("User ID cannot be null or empty");
        }
        List<OrderPojo> orderPojos = orderFlow.getOrdersByUserId(userId);
        List<OrderData> orderDataList = new ArrayList<>();
        for (OrderPojo orderPojo : orderPojos) {
            orderDataList.add(convertEntityToData(orderPojo));
        }
        return orderDataList;
    }

    public List<OrderData> getOrdersByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        if (userId == null || startDate == null || endDate == null) {
            throw new ApiException("User ID, start date, and end date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        List<OrderPojo> allUserOrders = orderFlow.getOrdersByUserId(userId);
        List<OrderData> filtered = new ArrayList<>();
        for (OrderPojo order : allUserOrders) {
            if (order.getDate() != null) {
                LocalDate orderDateIST = TimeUtil.toIST(order.getDate()).toLocalDate();
                if ((orderDateIST.isEqual(startDate) || orderDateIST.isAfter(startDate)) && orderDateIST.isBefore(endDate.plusDays(1))) {
                    filtered.add(convertEntityToData(order));
                }
            }
        }
        return filtered;
    }

    /**
     * Format date for invoice in "Date: dd/mm/yyyy. Time: hh:mm" format
     */
    private String formatDateForInvoice(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // ========== PAGINATION METHODS ==========



    /**
     * Get orders by user ID with pagination support.
     */
    public PaginationResponse<OrderData> getOrdersByUserIdPaginated(String userId, PaginationRequest request) {
        PaginationResponse<OrderPojo> paginatedEntities = orderFlow.getByUserIdPaginated(userId, request);
        
        List<OrderData> dataList = paginatedEntities.getContent().stream()
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
     * Get orders by date range with pagination support.
     */
    public PaginationResponse<OrderData> getOrdersByDateRangePaginated(LocalDate startDate, LocalDate endDate, PaginationRequest request) {
        PaginationResponse<OrderPojo> paginatedEntities = orderFlow.getByDateRangePaginated(startDate, endDate, request);
        
        List<OrderData> dataList = paginatedEntities.getContent().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
        
        return new PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
    }

    // ========== SUBSTRING SEARCH METHODS ==========

    /**
     * Find orders by ID substring matching.
     * This allows finding orders where the search term appears exactly as a substring in the order ID.
     * 
     * @param searchId The ID substring to search for
     * @param maxResults Maximum number of results to return
     * @return List of orders where the search term appears as a substring in the ID
     */
    public List<OrderData> findOrdersBySubstringId(String searchId, int maxResults) {
        if (searchId == null || searchId.trim().isEmpty()) {
            throw new ApiException("Search ID cannot be null or empty");
        }
        if (maxResults <= 0) {
            throw new ApiException("Max results must be positive");
        }
        
        List<OrderPojo> matchingOrders = orderFlow.findOrdersBySubstringId(searchId, maxResults);
        return matchingOrders.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    /**
     * Find orders by ID substring with pagination support.
     * 
     * @param searchId The ID substring to search for
     * @param request Pagination request
     * @return Paginated response with orders containing the substring
     */
    public PaginationResponse<OrderData> findOrdersBySubstringIdPaginated(
            String searchId, 
            PaginationRequest request) {
        if (searchId == null || searchId.trim().isEmpty()) {
            throw new ApiException("Search ID cannot be null or empty");
        }
        if (request == null) {
            request = new PaginationRequest();
        }
        
        PaginationResponse<OrderPojo> paginatedEntities = orderFlow.findOrdersBySubstringIdPaginated(searchId, request);
        
        List<OrderData> dataList = paginatedEntities.getContent().stream()
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
     * Count orders by ID substring.
     * 
     * @param searchId The ID substring to search for
     * @return Number of orders containing the substring
     */
    public long countOrdersBySubstringId(String searchId) {
        if (searchId == null || searchId.trim().isEmpty()) {
            throw new ApiException("Search ID cannot be null or empty");
        }
        return orderFlow.countOrdersBySubstringId(searchId);
    }

    // ========== ORDER ITEM MANAGEMENT METHODS ==========

    /**
     * Get order items for a specific order
     */
    public List<OrderItemData> getOrderItemsByOrderId(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException("Order ID cannot be null");
        }
        if (orderId <= 0) {
            throw new ApiException("Order ID must be positive");
        }
        System.out.println("OrderDto: Getting order items for order ID: " + orderId);
        List<OrderItemPojo> orderItemPojoList = orderItemDao.selectByOrderId(orderId);
        System.out.println("OrderDto: Found " + orderItemPojoList.size() + " order item POJOs for order " + orderId);
        List<OrderItemData> orderItemDataList = new ArrayList<>();
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            System.out.println("OrderDto: Converting order item POJO ID: " + orderItemPojo.getId());
            orderItemDataList.add(convertOrderItemPojoToData(orderItemPojo));
        }
        System.out.println("OrderDto: Returning " + orderItemDataList.size() + " order item data objects");
        return orderItemDataList;
    }

    /**
     * Add a single order item
     */
    @org.springframework.transaction.annotation.Transactional
    public OrderItemData addOrderItem(OrderItemForm orderItemForm) {
        if (Objects.isNull(orderItemForm)) {
            throw new ApiException("Order item form cannot be null");
        }
        
        // Validate orderId is provided
        if (Objects.isNull(orderItemForm.getOrderId())) {
            throw new ApiException("Order ID cannot be null");
        }
        
        // Validate productId is provided
        if (Objects.isNull(orderItemForm.getProductId())) {
            throw new ApiException("Product ID is required");
        }
        
        // Validate that the product exists
        ProductPojo product = null;
        try {
            product = productApi.get(orderItemForm.getProductId());
            if (product == null) {
                throw new ApiException("Product with ID " + orderItemForm.getProductId() + " not found");
            }
        } catch (Exception e) {
            throw new ApiException("Product with ID " + orderItemForm.getProductId() + " not found");
        }

        // Reduce inventory before creating order item
        orderFlow.reduceInventoryForOrderItem(orderItemForm.getProductId(), orderItemForm.getQuantity());

        OrderItemPojo orderItemPojo = convertOrderItemFormToPojo(orderItemForm);
        orderItemDao.insert(orderItemPojo);
        return convertOrderItemPojoToData(orderItemPojo);
    }

    /**
     * Update an order item
     */
    @org.springframework.transaction.annotation.Transactional
    public OrderItemData updateOrderItem(Integer id, OrderItemForm orderItemForm) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order item ID cannot be null");
        }
        if (Objects.isNull(orderItemForm)) {
            throw new ApiException("Order item form cannot be null");
        }
        
        // Get the existing order item to calculate inventory adjustment
        OrderItemPojo existingItem = orderFlow.getOrderItem(id);
        if (existingItem == null) {
            throw new ApiException("Order item with ID " + id + " not found");
        }
        
        // Adjust inventory based on quantity difference
        orderFlow.adjustInventoryForOrderItemUpdate(
            orderItemForm.getProductId(), 
            existingItem.getQuantity(), 
            orderItemForm.getQuantity()
        );
        
        OrderItemPojo orderItemPojo = convertOrderItemFormToPojo(orderItemForm);
        orderItemDao.update(id, orderItemPojo);
        return convertOrderItemPojoToData(orderItemDao.select(id));
    }

    /**
     * Get a single order item by ID
     */
    public OrderItemData getOrderItem(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order item ID cannot be null");
        }
        OrderItemPojo orderItemPojo = orderItemDao.select(id);
        return convertOrderItemPojoToData(orderItemPojo);
    }

    /**
     * Convert OrderItemForm to OrderItemPojo
     */
    private OrderItemPojo convertOrderItemFormToPojo(OrderItemForm orderItemForm) {
        OrderItemPojo orderItemPojo = new OrderItemPojo();
        orderItemPojo.setOrderId(orderItemForm.getOrderId());
        orderItemPojo.setProductId(orderItemForm.getProductId());
        orderItemPojo.setQuantity(orderItemForm.getQuantity());
        
        // Get product details for pricing
        ProductPojo product = productApi.get(orderItemForm.getProductId());
        if (product == null) {
            throw new ApiException("Product with ID " + orderItemForm.getProductId() + " not found");
        }
        
        orderItemPojo.setSellingPrice(product.getMrp());
        orderItemPojo.setAmount(product.getMrp() * orderItemForm.getQuantity());
        
        return orderItemPojo;
    }

    /**
     * Convert OrderItemPojo to OrderItemData
     */
    private OrderItemData convertOrderItemPojoToData(OrderItemPojo orderItemPojo) {
        OrderItemData orderItemData = new OrderItemData();
        orderItemData.setId(orderItemPojo.getId());
        orderItemData.setOrderId(orderItemPojo.getOrderId());
        orderItemData.setProductId(orderItemPojo.getProductId());
        orderItemData.setQuantity(orderItemPojo.getQuantity());
        orderItemData.setSellingPrice(orderItemPojo.getSellingPrice());
        orderItemData.setAmount(orderItemPojo.getAmount());
        
        // Fetch product information using productId
        ProductPojo product = null;
        try {
            product = productApi.get(orderItemPojo.getProductId());
        } catch (Exception e) {
            // Product not found, continue with null values
        }
        
        if (product != null) {
            orderItemData.setBarcode(product.getBarcode());
            orderItemData.setProductName(product.getName());
            orderItemData.setImageUrl(product.getImageUrl());
            orderItemData.setClientId(product.getClientId());
            
            // Fetch client information
            if (product.getClientId() != null && product.getClientId() > 0) {
                try {
                    ClientPojo client = clientApi.get(product.getClientId());
                    if (client != null) {
                        orderItemData.setClientName(client.getClientName());
                    }
                } catch (Exception e) {
                    // Client not found, continue with null
                }
            }
        }
        
        // Get order date from the order
        try {
            OrderPojo order = api.get(orderItemPojo.getOrderId());
            orderItemData.setDateTime(TimeUtil.toIST(order.getDate()));
        } catch (Exception e) {
            // If order not found, set to null
            orderItemData.setDateTime(null);
        }
        
        return orderItemData;
    }
}
