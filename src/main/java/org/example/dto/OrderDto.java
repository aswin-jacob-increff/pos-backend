package org.example.dto;

import org.example.pojo.OrderStatus;
import org.example.flow.OrderFlow;
import org.example.model.OrderData;
import org.example.model.OrderForm;
import org.example.model.OrderItemData;
import org.example.model.OrderItemForm;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.ProductPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private ProductDto productDto;

    public OrderData add(OrderForm orderForm) {
        if (orderForm.getOrderItemFormList() == null || orderForm.getOrderItemFormList().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one order item");
        }
        OrderPojo orderPojo = orderFlow.add(convert(orderForm));
        return convert(orderPojo);
    }

    public OrderData get(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
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

    public OrderData update(Integer id, OrderForm orderForm) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        return convert(orderFlow.update(id, convert(orderForm)));
    }

    public void delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        orderFlow.delete(id);
    }

    private OrderPojo convert(OrderForm orderForm) {
        OrderPojo orderPojo = new OrderPojo();
        orderPojo.setDate(orderForm.getDate());
        orderPojo.setStatus(orderForm.getStatus());
        orderPojo.setTotal(0.0);

        // Convert list of OrderItemForm to OrderItemPojo
        if (orderForm.getOrderItemFormList() != null) {
            List<OrderItemPojo> itemPojoList = new ArrayList<>();
            for (OrderItemForm itemForm : orderForm.getOrderItemFormList()) {
                OrderItemPojo itemPojo = new OrderItemPojo();
                itemPojo.setQuantity(itemForm.getQuantity());
                itemPojo.setSellingPrice(itemForm.getSellingPrice());
                itemPojo.setAmount(itemForm.getSellingPrice() * itemForm.getQuantity());

                // Assuming productId is provided in form and must be converted to a ProductPojo
                ProductPojo product = new ProductPojo();
                product.setId(itemForm.getProductId());
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
        orderData.setDate(orderPojo.getDate());
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

                // This can be expanded using productDto if needed
                itemData.setProductId(itemPojo.getProduct().getId());
                itemData.setProductName(itemPojo.getProduct().getName());

                orderItemDataList.add(itemData);
            }
            orderData.setOrderItemDataList(orderItemDataList);
        }

        return orderData;
    }
}
