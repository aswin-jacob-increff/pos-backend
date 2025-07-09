package org.example.dto;

import org.example.flow.OrderFlow;
import org.example.flow.ProductFlow;
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
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InvoiceApi invoiceApi;

    public OrderData add(@Valid OrderForm orderForm) {
        OrderPojo orderPojo = orderFlow.add(convert(orderForm));
        return convert(orderPojo);
    }

    public OrderData get(Integer id) {
        validateOrderId(id);
        return convert(orderFlow.get(id));
    }

    public List<OrderData> getAll() {
        List<OrderPojo> orderPojoList = orderFlow.getAll();
        List<OrderData> orderDataList = new ArrayList<>();
        for (OrderPojo orderPojo : orderPojoList) {
            orderDataList.add(convert(orderPojo));
        }
        return orderDataList;
    }

    public OrderData update(Integer id, @Valid OrderForm orderForm) {
        validateOrderId(id);
        return convert(orderFlow.update(id, convert(orderForm)));
    }

    public void delete(Integer id) {
        validateOrderId(id);
        orderFlow.delete(id);
    }
    
    public void cancelOrder(Integer id) {
        validateOrderId(id);
        orderFlow.cancelOrder(id);
    }
    
    private void validateOrderId(Integer id) {
        if (id == null) {
            throw new ApiException("Order ID cannot be null");
        }
        if (id <= 0) {
            throw new ApiException("Order ID must be positive");
        }
    }

    private OrderPojo convert(OrderForm orderForm) {
        OrderPojo orderPojo = new OrderPojo();
        // Convert LocalDateTime (IST from frontend) to Instant (UTC) for DB storage
        orderPojo.setDate(orderForm.getDate() != null ? TimeUtil.toUTC(orderForm.getDate()) : null);
        orderPojo.setTotal(0.0);
        if (orderForm.getOrderItemFormList() != null) {
            List<OrderItemPojo> itemPojoList = new ArrayList<>();
            for (OrderItemForm itemForm : orderForm.getOrderItemFormList()) {
                OrderItemPojo itemPojo = new OrderItemPojo();
                itemPojo.setQuantity(itemForm.getQuantity());
                itemPojo.setSellingPrice(itemForm.getSellingPrice());
                itemPojo.setAmount(itemForm.getSellingPrice() * itemForm.getQuantity());
                ProductPojo product = productFlow.get(itemForm.getProductId());
                if (product == null) {
                    throw new ApiException("Product with ID " + itemForm.getProductId() + " not found");
                }
                itemPojo.setProduct(product);
                itemPojoList.add(itemPojo);
            }
            orderPojo.setOrderItems(itemPojoList);
        }
        return orderPojo;
    }

    private OrderData convert(OrderPojo orderPojo) {
        OrderData orderData = new OrderData();
        orderData.setId(orderPojo.getId());
        // Convert UTC Instant from DB to IST LocalDateTime for frontend
        orderData.setDate(TimeUtil.toIST(orderPojo.getDate()));
        orderData.setTotal(orderPojo.getTotal());
        orderData.setStatus(orderPojo.getStatus());

        if (orderPojo.getOrderItems() != null) {
            List<OrderItemData> orderItemDataList = new ArrayList<>();
            for (OrderItemPojo itemPojo : orderPojo.getOrderItems()) {
                OrderItemData itemData = new OrderItemData();
                itemData.setId(itemPojo.getId());
                itemData.setQuantity(itemPojo.getQuantity());
                itemData.setSellingPrice(itemPojo.getSellingPrice());
                itemData.setAmount(itemPojo.getAmount());
                itemData.setProductId(itemPojo.getProduct().getId());
                itemData.setProductName(itemPojo.getProduct().getName());
                // Convert UTC Instant from DB to IST LocalDateTime for frontend
                itemData.setDateTime(TimeUtil.toIST(orderPojo.getDate()));
                if (itemPojo.getProduct().getImageUrl() != null && !itemPojo.getProduct().getImageUrl().trim().isEmpty()) {
                    itemData.setImageUrl(itemPojo.getProduct().getImageUrl());
                }
                orderItemDataList.add(itemData);
            }
            orderData.setOrderItemDataList(orderItemDataList);
        }

        return orderData;
    }

    public org.springframework.core.io.Resource downloadInvoice(Integer orderId) {
        validateOrderId(orderId);
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
            
            // Create InvoiceData object to send to invoice service
            InvoiceData invoiceData = new InvoiceData();
            invoiceData.setOrderId(orderPojo.getId());
            invoiceData.setTotal(orderPojo.getTotal());
            invoiceData.setTotalQuantity(orderPojo.getOrderItems().stream()
                    .mapToInt(OrderItemPojo::getQuantity).sum());
            invoiceData.setId(orderPojo.getId());
            // Convert UTC Instant to IST String for invoice in dd/mm/yyyy hh:mm format
            invoiceData.setDate(orderPojo.getDate() != null ? 
                formatDateForInvoice(TimeUtil.toIST(orderPojo.getDate())) : null);

            // Convert order items to invoice items
            List<InvoiceItemData> invoiceItemDataList = new ArrayList<>();
            for (OrderItemPojo itemPojo : orderPojo.getOrderItems()) {
                InvoiceItemData itemData = new InvoiceItemData();
                itemData.setId(itemPojo.getId());
                itemData.setProductId(itemPojo.getProduct().getId());
                itemData.setProductName(itemPojo.getProduct().getName());
                itemData.setProductBarcode(itemPojo.getProduct().getBarcode());
                itemData.setClientId(itemPojo.getProduct().getClient().getId());
                itemData.setClientName(itemPojo.getProduct().getClient().getClientName());
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
