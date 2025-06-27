package org.example.service;

import org.example.dao.InvoiceDao;
import org.example.dao.InvoiceItemDao;
import org.example.dao.OrderDao;
import org.example.pojo.InvoiceItemPojo;
import org.example.pojo.InvoicePojo;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.util.InvoicePdfGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InvoiceService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private InvoiceItemDao invoiceItemDao;

    @Autowired
    private OrderItemService orderItemService;

    public InvoicePojo generateInvoice(Integer orderId) {
        OrderPojo order = orderDao.select(orderId);
        InvoicePojo invoice = new InvoicePojo();
        invoice.setOrder(order);
        invoice.setTotalQuantity(0);
        invoice.setTotal(0.0);

        // 🔥 First insert invoice to generate ID
        invoiceDao.insert(invoice);

        List<OrderItemPojo> orderItemPojoList = orderItemService.getByOrderId(orderId);
        List<InvoiceItemPojo> invoiceItemPojoList = new ArrayList<>();

        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            InvoiceItemPojo invoiceItemPojo = new InvoiceItemPojo();
            invoiceItemPojo.setOrderItem(orderItemPojo);
            invoiceItemPojo.setInvoice(invoice); // 🔥 Set parent invoice reference
            invoiceItemPojo.setName(orderItemPojo.getProduct().getName());
            invoiceItemPojo.setPrice(orderItemPojo.getSellingPrice());
            invoiceItemPojo.setQuantity(orderItemPojo.getQuantity());
            invoiceItemPojo.setAmount(invoiceItemPojo.getPrice() * invoiceItemPojo.getQuantity());

            invoiceItemDao.insert(invoiceItemPojo);
            invoiceItemPojoList.add(invoiceItemPojo);

            invoice.setTotal(invoice.getTotal() + invoiceItemPojo.getAmount());
            invoice.setTotalQuantity(invoice.getTotalQuantity() + invoiceItemPojo.getQuantity());
        }

        invoice.setInvoiceItemList(invoiceItemPojoList); // optional — used only for DTO

        return invoice;
    }


    public byte[] generateInvoicePdf(Integer orderId) throws Exception {
        // Step 1: Generate or fetch the InvoicePojo
        InvoicePojo invoice = invoiceDao.selectWithItems(orderId); // your existing method

        // Step 2: Generate PDF using FOP utility
        return InvoicePdfGenerator.generatePdf(invoice);
    }

    public InvoicePojo get(Integer id) {
        return invoiceDao.selectWithItems(id);
    }

    public List<InvoicePojo> getAll() {
        return invoiceDao.selectAllWithItems();
    }

}
