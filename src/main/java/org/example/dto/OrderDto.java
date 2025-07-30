package org.example.dto;

import org.example.flow.OrderFlow;
import org.example.api.InvoiceApi;
import org.example.api.ClientApi;
import org.example.api.ProductApi;
import org.example.api.OrderApi;
import org.example.api.InventoryApi;
import org.example.api.InvoiceClientApi;
import org.example.model.data.*;
import org.example.model.enums.OrderStatus;
import org.example.model.form.OrderForm;
import org.example.model.form.OrderItemForm;

import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.pojo.InvoicePojo;
import org.example.exception.ApiException;
import org.example.util.Base64ToPdfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.example.model.form.PaginationRequest;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Component
public class OrderDto extends AbstractDto<OrderPojo, OrderForm, OrderData> {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private InvoiceApi invoiceApi;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private InvoiceClientApi invoiceClientApi;

    @Override
    protected String getEntityName() {
        return "Order";
    }

    @Override
    protected OrderPojo convertFormToEntity(OrderForm orderForm) {
        if (orderForm == null) {
            throw new ApiException("Order form cannot be null");
        }

        OrderPojo orderPojo = new OrderPojo();
        orderPojo.setDate(orderForm.getDate());
        orderPojo.setTotal(0.0);
        orderPojo.setUserId(orderForm.getUserId());

        return orderPojo;
    }

    @Override
    protected OrderData convertEntityToData(OrderPojo orderPojo) {

        if (orderPojo == null) {
            throw new ApiException("Order cannot be null");
        }
        
        OrderData orderData = new OrderData();
        orderData.setId(orderPojo.getId());
        orderData.setDate(orderPojo.getDate());
        orderData.setTotal(orderPojo.getTotal());
        orderData.setStatus(orderPojo.getStatus());
        orderData.setUserId(orderPojo.getUserId());
        List<OrderItemData> orderItems = getOrderItemsByOrderId(orderPojo.getId());
        orderData.setOrderItemDataList(orderItems);
        
        return orderData;
    }

    public Resource downloadInvoice(Integer orderId) {

        validate(orderId);
        OrderPojo orderPojo = api.get(orderId);

        InvoicePojo existingInvoice = invoiceApi.getByOrderId(orderId);
        if (existingInvoice != null) {
            return orderFlow.getInvoiceFile(orderId);
        }
        
        List<OrderItemPojo> orderItems = ((OrderApi) api).getOrderItemsByOrderId(orderId);
        List<ProductPojo> products = new ArrayList<>();
        List<ClientPojo> clients = new ArrayList<>();
        
        for (OrderItemPojo itemPojo : orderItems) {
            ProductPojo product = productApi.get(itemPojo.getProductId());
            products.add(product);
            
            if (product != null && product.getClientId() != null) {
                ClientPojo client = clientApi.get(product.getClientId());
                clients.add(client);
            } else {
                clients.add(null);
            }
        }
        
        InvoiceAppForm invoiceAppForm = invoiceFormSetter(orderPojo, orderItems, products, clients);
        String base64Pdf = invoiceClientApi.generateInvoice(invoiceAppForm);
        return saveInvoice(orderId, base64Pdf);
    }

    @Override
    public OrderData add(@Valid OrderForm form) {
        // Validate order form
        validateOrderForm(form);
        
        // Convert form to entity
        OrderPojo orderPojo = convertFormToEntity(form);
        
        // Convert order item forms to pojos
        List<OrderItemPojo> orderItemPojos = convertOrderItemFormsToPojos(form.getOrderItemFormList());
        
        // Pass to flow layer for complete order creation
        OrderPojo createdOrder = orderFlow.createOrderWithItems(orderPojo, orderItemPojos);
        
        return convertEntityToData(createdOrder);
    }

    @Override
    public OrderData get(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order ID cannot be null");
        }
        return super.get(id);
    }

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

    private String formatDateForInvoice(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

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

    public List<OrderItemData> getOrderItemsByOrderId(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException("Order ID cannot be null");
        }
        if (orderId <= 0) {
            throw new ApiException("Order ID must be positive");
        }
        List<OrderItemPojo> orderItemPojoList = ((OrderApi) api).getOrderItemsByOrderId(orderId);
        List<OrderItemData> orderItemDataList = new ArrayList<>();
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            orderItemDataList.add(convertOrderItemPojoToData(orderItemPojo));
        }
        return orderItemDataList;
    }

    public OrderItemData getOrderItem(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("Order item ID cannot be null");
        }
        OrderItemPojo orderItemPojo = ((OrderApi) api).getOrderItem(id);
        return convertOrderItemPojoToData(orderItemPojo);
    }

    private OrderItemPojo convertOrderItemFormToPojo(OrderItemForm orderItemForm) {
        OrderItemPojo orderItemPojo = new OrderItemPojo();
        orderItemPojo.setOrderId(orderItemForm.getOrderId());
        orderItemPojo.setProductId(orderItemForm.getProductId());
        orderItemPojo.setQuantity(orderItemForm.getQuantity());
        
        ProductPojo product = productApi.get(orderItemForm.getProductId());
        if (product == null) {
            throw new ApiException("Product with ID " + orderItemForm.getProductId() + " not found");
        }
        
        orderItemPojo.setSellingPrice(product.getMrp());
        orderItemPojo.setAmount(product.getMrp() * orderItemForm.getQuantity());
        
        return orderItemPojo;
    }

    /**
     * Convert a list of OrderItemForm to a list of OrderItemPojo
     */
    private List<OrderItemPojo> convertOrderItemFormsToPojos(List<OrderItemForm> orderItemForms) {
        if (orderItemForms == null || orderItemForms.isEmpty()) {
            throw new ApiException("Order item list cannot be null or empty");
        }
        
        List<OrderItemPojo> orderItemPojos = new ArrayList<>();
        for (OrderItemForm form : orderItemForms) {
            orderItemPojos.add(convertOrderItemFormToPojo(form));
        }
        return orderItemPojos;
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
        ProductPojo product = productApi.get(orderItemPojo.getProductId());
        orderItemData.setBarcode(product.getBarcode());
        orderItemData.setProductName(product.getName());
        orderItemData.setImageUrl(product.getImageUrl());
        orderItemData.setClientId(product.getClientId());
        ClientPojo client = clientApi.get(product.getClientId());
        orderItemData.setClientName(client.getClientName());
        OrderPojo order = api.get(orderItemPojo.getOrderId());
        orderItemData.setDateTime(order.getDate());
        
        return orderItemData;
    }

    private InvoiceAppForm invoiceFormSetter(OrderPojo orderPojo,
                                             List<OrderItemPojo> orderItems,
                                             List<ProductPojo> products,
                                             List<ClientPojo> clients) {
        
        InvoiceAppForm invoiceAppForm = new InvoiceAppForm();
        invoiceAppForm.setOrderId(orderPojo.getId());
        invoiceAppForm.setTotal(orderPojo.getTotal());
        invoiceAppForm.setTotalQuantity(orderItems.stream()
                .mapToInt(OrderItemPojo::getQuantity).sum());
        invoiceAppForm.setId(orderPojo.getId());
        invoiceAppForm.setDate(formatDateForInvoice(orderPojo.getDate()));
        List<InvoiceAppFormItem> invoiceAppFormItemList = new ArrayList<>();
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItemPojo itemPojo = orderItems.get(i);
            ProductPojo product = products.get(i);
            ClientPojo client = clients.get(i);
            invoiceAppFormItemList.add(invoiceAppFormItemSetter(itemPojo, product, client));
        }
        invoiceAppForm.setInvoiceItemPojoList(invoiceAppFormItemList);
        return invoiceAppForm;
    }

    private static InvoiceAppFormItem invoiceAppFormItemSetter(OrderItemPojo itemPojo,
                                                               ProductPojo product,
                                                               ClientPojo client) {

        InvoiceAppFormItem itemData = new InvoiceAppFormItem();
        itemData.setId(itemPojo.getId());
        itemData.setProductId(itemPojo.getProductId());
        itemData.setProductName(product.getName());
        itemData.setProductBarcode(product.getBarcode());
        itemData.setClientId(product.getClientId());
        itemData.setClientName(client.getClientName());
        itemData.setPrice(itemPojo.getSellingPrice());
        itemData.setQuantity(itemPojo.getQuantity());
        itemData.setAmount(itemPojo.getAmount());
        return itemData;
    }

    private Resource saveInvoice (Integer orderId, String base64Pdf) {

        String fileName = "order-" + orderId + ".pdf";
        String savePath = "src/main/resources/invoice/" + fileName;

        try {
            Base64ToPdfUtil.saveBase64AsPdf(base64Pdf, savePath);
        } catch (Exception e) {
            throw new ApiException("Failed to save invoice PDF: " + e.getMessage());
        }

        InvoicePojo invoicePojo = new InvoicePojo();
        invoicePojo.setOrderId(orderId);
        invoicePojo.setFilePath("/invoice/" + fileName);
        invoicePojo.setInvoiceId(orderId.toString());
        invoiceApi.add(invoicePojo);

        orderFlow.updateStatus(orderId, OrderStatus.INVOICED);

        byte[] pdfBytes = java.util.Base64.getDecoder().decode(base64Pdf);

        return new org.springframework.core.io.ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }

    private void validateOrderForm(OrderForm form) {
        if (Objects.isNull(form)) {
            throw new ApiException("Order form cannot be null");
        }
        
        if (Objects.isNull(form.getUserId()) || form.getUserId().trim().isEmpty()) {
            throw new ApiException("User ID cannot be null or empty");
        }
        
        if (Objects.isNull(form.getOrderItemFormList()) || form.getOrderItemFormList().isEmpty()) {
            throw new ApiException("Order must contain at least one item");
        }
        
        // Validate each order item
        for (OrderItemForm itemForm : form.getOrderItemFormList()) {
            validateOrderItemForm(itemForm);
        }
    }
    
    private void validateOrderItemForm(OrderItemForm itemForm) {
        if (Objects.isNull(itemForm)) {
            throw new ApiException("Order item form cannot be null");
        }
        
        if (Objects.isNull(itemForm.getProductId())) {
            throw new ApiException("Product ID is required for all order items");
        }
        
        if (Objects.isNull(itemForm.getQuantity()) || itemForm.getQuantity() <= 0) {
            throw new ApiException("Quantity must be positive for all order items");
        }
        
        if (Objects.isNull(itemForm.getSellingPrice()) || itemForm.getSellingPrice() <= 0) {
            throw new ApiException("Selling price must be positive for all order items");
        }
    }

    private void validate (Integer orderId) {
        if (orderId == null) {
            throw new ApiException("Order ID cannot be null");
        }
        OrderPojo orderPojo = api.get(orderId);
        if (orderPojo == null) {
            throw new ApiException("Order not found with ID: " + orderId);
        }
    }
}
