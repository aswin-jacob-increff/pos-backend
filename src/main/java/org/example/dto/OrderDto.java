package org.example.dto;

import org.example.flow.OrderFlow;
import org.example.flow.ProductFlow;
import org.example.flow.OrderItemFlow;
import org.example.api.InvoiceApi;
import org.example.model.OrderData;
import org.example.model.OrderForm;
import org.example.model.OrderItemData;
import org.example.model.OrderItemForm;
import org.example.model.InvoiceData;
import org.example.model.InvoiceItemData;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.ProductPojo;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.example.util.TimeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.format.DateTimeFormatter;

@Component
public class OrderDto extends AbstractDto<OrderPojo, OrderForm, OrderData> {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InvoiceApi invoiceApi;
    
    @Autowired
    private org.example.api.OrderItemApi orderItemApi;
    
    @Autowired
    private org.example.dto.OrderItemDto orderItemDto;
    
    @Autowired
    private org.example.flow.OrderItemFlow orderItemFlow;

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
        OrderData orderData = new OrderData();
        orderData.setId(orderPojo.getId());
        // Convert UTC Instant from DB to IST LocalDateTime for frontend
        orderData.setDate(TimeUtil.toIST(orderPojo.getDate()));
        orderData.setTotal(orderPojo.getTotal());
        orderData.setStatus(orderPojo.getStatus());
        orderData.setUserId(orderPojo.getUserId());
        
        // Fetch order items for this order using OrderItemDto
        try {
            System.out.println("Fetching order items for order ID: " + orderPojo.getId());
            List<OrderItemData> orderItems = orderItemDto.getByOrderId(orderPojo.getId());
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
    public void cancelOrder(Integer id) {
        if (id == null) {
            throw new ApiException("Order ID cannot be null");
        }
        orderFlow.cancelOrder(id);
    }

    public org.springframework.core.io.Resource downloadInvoice(Integer orderId) {
        if (orderId == null) {
            throw new ApiException("Order ID cannot be null");
        }
        OrderPojo orderPojo = orderFlow.get(orderId);
        
        try {
            System.out.println("Starting invoice download for order: " + orderId);
            System.out.println("Current order status: " + orderPojo.getStatus());
            
            // Check if invoice already exists in database
            org.example.pojo.InvoicePojo existingInvoice = invoiceApi.getByOrderId(orderId);
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
            List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(orderId);
            
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
                itemData.setProductId(null); // No longer have product ID reference
                itemData.setProductName(itemPojo.getProductName());
                itemData.setProductBarcode(itemPojo.getProductBarcode());
                itemData.setClientId(null); // No longer have client ID reference
                itemData.setClientName(itemPojo.getClientName());
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
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error calling invoice service: " + e.getMessage());
                throw new ApiException("Failed to connect to invoice service at " + invoiceAppUrl + ": " + e.getMessage());
            }

            if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("Invoice service call successful, saving PDF...");
                // Save PDF file
                String fileName = "order-" + orderId + ".pdf";
                String savePath = "src/main/resources/invoice/" + fileName;
                
                try {
                    org.example.util.Base64ToPdfUtil.saveBase64AsPdf(response.getBody(), savePath);
                    System.out.println("PDF saved successfully to: " + savePath);
                } catch (Exception e) {
                    System.out.println("Error saving PDF: " + e.getMessage());
                    throw new ApiException("Failed to save invoice PDF: " + e.getMessage());
                }

                System.out.println("Saving invoice to database...");
                // Create and save invoice entity to database using InvoiceApi
                org.example.pojo.InvoicePojo invoicePojo = new org.example.pojo.InvoicePojo();
                invoicePojo.setOrderId(orderId);
                invoicePojo.setFilePath("/invoice/" + fileName);
                invoicePojo.setInvoiceId(orderId.toString());
                invoiceApi.add(invoicePojo);
                System.out.println("Invoice saved to database successfully");

                System.out.println("Updating order status to INVOICED...");
                // Update order status to INVOICED
                orderFlow.updateStatus(orderId, org.example.pojo.OrderStatus.INVOICED);

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
                
                // Create order item using OrderItemDto
                OrderItemPojo orderItemPojo = orderItemDto.convertFormToEntity(itemForm);
                orderItemFlow.add(orderItemPojo);
                
                totalAmount += orderItemPojo.getAmount();
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
        return super.update(id, form);
    }


    
    /**
     * Get orders within a date range
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of order data within the date range
     */
    public List<OrderData> getOrdersByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
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
        List<OrderPojo> orderPojos = orderFlow.getOrdersByUserId(userId);
        List<OrderData> orderDataList = new ArrayList<>();
        for (OrderPojo orderPojo : orderPojos) {
            orderDataList.add(convertEntityToData(orderPojo));
        }
        return orderDataList;
    }

    /**
     * Format date for invoice in "Date: dd/mm/yyyy. Time: hh:mm" format
     */
    private String formatDateForInvoice(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return null;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return "Date: " + dateTime.format(dateFormatter) + ". Time: " + dateTime.format(timeFormatter);
    }
}
