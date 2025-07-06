package org.example.dto;

import org.example.flow.OrderFlow;
import org.example.flow.ProductFlow;
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

@Component
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private RestTemplate restTemplate;

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
        // Convert LocalDateTime (IST) to Instant (UTC) for DB
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
        // Convert UTC Instant to IST LocalDateTime for frontend
        orderData.setDate(TimeUtil.toIST(orderPojo.getDate()));
        orderData.setTotal(orderPojo.getTotal());

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
                // Convert UTC Instant to IST LocalDateTime for frontend
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

    public String generateInvoice(Integer orderId) throws Exception {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        return orderFlow.generateInvoice(orderId);
    }

    public org.springframework.core.io.Resource getInvoiceFile(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        return orderFlow.getInvoiceFile(orderId);
    }

    public org.springframework.core.io.Resource generateAndGetInvoiceResource(Integer orderId) {
        validateOrderId(orderId);
        OrderPojo orderPojo = orderFlow.get(orderId);
        
        try {
            // Create InvoiceData object to send to invoice service
            InvoiceData invoiceData = new InvoiceData();
            invoiceData.setOrderId(orderPojo.getId());
            invoiceData.setTotal(orderPojo.getTotal());
            invoiceData.setTotalQuantity(orderPojo.getOrderItems().stream()
                    .mapToInt(OrderItemPojo::getQuantity).sum());
            invoiceData.setId(orderPojo.getId());

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

            // Call invoice service
            String invoiceAppUrl = "http://localhost:8081/api/invoice";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<InvoiceData> entity = new HttpEntity<>(invoiceData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(invoiceAppUrl, entity, String.class);

            if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Convert base64 to PDF bytes
                byte[] pdfBytes = java.util.Base64.getDecoder().decode(response.getBody());
                
                // Create a resource from the byte array
                return new org.springframework.core.io.ByteArrayResource(pdfBytes) {
                    @Override
                    public String getFilename() {
                        return "order-" + orderId + ".pdf";
                    }
                };
            } else {
                throw new ApiException("Failed to fetch invoice from invoice service");
            }
        } catch (Exception e) {
            throw new ApiException("Failed to generate invoice: " + e.getMessage());
        }
    }

    public String fetchInvoiceForOrder(Integer orderId) {
        validateOrderId(orderId);
        OrderPojo orderPojo = orderFlow.get(orderId);
        
        try {
            // Create InvoiceData object to send to invoice service
            InvoiceData invoiceData = new InvoiceData();
            invoiceData.setOrderId(orderPojo.getId());
            invoiceData.setTotal(orderPojo.getTotal());
            invoiceData.setTotalQuantity(orderPojo.getOrderItems().stream()
                    .mapToInt(OrderItemPojo::getQuantity).sum());
            invoiceData.setId(orderPojo.getId());

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

            // Call invoice service
            String invoiceAppUrl = "http://localhost:8081/api/invoice";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<InvoiceData> entity = new HttpEntity<>(invoiceData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(invoiceAppUrl, entity, String.class);

            if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new ApiException("Failed to fetch invoice from invoice service");
            }
        } catch (Exception e) {
            throw new ApiException("Failed to fetch invoice from invoice service: " + e.getMessage());
        }
    }

    private void fetchInvoiceData(OrderData orderData, OrderPojo orderPojo) {
        try {
            // Create InvoiceData object to send to invoice service
            InvoiceData invoiceData = new InvoiceData();
            invoiceData.setOrderId(orderPojo.getId());
            invoiceData.setTotal(orderPojo.getTotal());
            invoiceData.setTotalQuantity(orderPojo.getOrderItems().stream()
                    .mapToInt(OrderItemPojo::getQuantity).sum());
            invoiceData.setId(orderPojo.getId());

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

            // Call invoice service
            String invoiceAppUrl = "http://localhost:8081/api/invoice";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<InvoiceData> entity = new HttpEntity<>(invoiceData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(invoiceAppUrl, entity, String.class);

            if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                orderData.setInvoiceBase64(response.getBody());
                orderData.setInvoiceUrl("/api/orders/" + orderPojo.getId() + "/invoice");
            }
        } catch (Exception e) {
            throw new ApiException("Failed to fetch invoice from invoice service: " + e.getMessage());
        }
    }
}
