package org.example.dto;

import org.example.flow.InvoiceFlow;
import org.example.model.InvoiceData;
import org.example.model.InvoiceItemData;
import org.example.pojo.InvoiceItemPojo;
import org.example.pojo.InvoicePojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InvoiceDto {

    @Autowired
    private InvoiceFlow invoiceFlow;

    public InvoiceData generateInvoice(Integer orderId) {
        InvoicePojo pojo = invoiceFlow.generateInvoice(orderId);
        return convert(pojo);
    }

    public InvoiceData getInvoice(Integer id) {
        InvoicePojo pojo = invoiceFlow.get(id);
        return convert(pojo);
    }

    public List<InvoiceData> getAllInvoices() {
        List<InvoicePojo> pojoList = invoiceFlow.getAll();
        List<InvoiceData> dataList = new ArrayList<>();
        for (InvoicePojo pojo : pojoList) {
            dataList.add(convert(pojo));
        }
        return dataList;
    }

    // Optional: Used only if controller handles PDF streaming
    public byte[] generateInvoicePdf(Integer orderId) throws Exception {
        return invoiceFlow.generateInvoicePdf(orderId);
    }

    private InvoiceData convert(InvoicePojo pojo) {
        InvoiceData data = new InvoiceData();
        data.setId(pojo.getId());

        // 🔥 Trigger loading of lazy collection before session closes
        List<InvoiceItemPojo> invoiceItemPojoList = pojo.getInvoiceItemList();
        List<InvoiceItemData> invoiceItemDataList = new ArrayList<>();
        for (InvoiceItemPojo invoiceItemPojo : invoiceItemPojoList) {
            InvoiceItemData invoiceItemData = convert(invoiceItemPojo);
            invoiceItemDataList.add(invoiceItemData);
        }
        data.setInvoiceItemPojoList(invoiceItemDataList);
        data.setTotal(pojo.getTotal());
        data.setOrderId(pojo.getOrder().getId());
        data.setDateTime(pojo.getOrder().getDate());
        data.setTotalQuantity(pojo.getTotalQuantity());
        return data;
    }

    private InvoiceItemData convert(InvoiceItemPojo invoiceItemPojo) {
        InvoiceItemData invoiceItemData = new InvoiceItemData();
        invoiceItemData.setId(invoiceItemPojo.getId());
        invoiceItemData.setProductId(invoiceItemPojo.getOrderItem().getProduct().getId());
        invoiceItemData.setProductBarcode(invoiceItemPojo.getOrderItem().getProduct().getBarcode());
        invoiceItemData.setProductName(invoiceItemPojo.getOrderItem().getProduct().getName());
        invoiceItemData.setClientId(invoiceItemPojo.getOrderItem().getProduct().getClient().getId());
        invoiceItemData.setClientName(invoiceItemPojo.getOrderItem().getProduct().getClient().getClientName());
        invoiceItemData.setQuantity(invoiceItemPojo.getQuantity());
        invoiceItemData.setPrice(invoiceItemPojo.getPrice());
        invoiceItemData.setAmount(invoiceItemPojo.getAmount());
        return invoiceItemData;
    }


}
