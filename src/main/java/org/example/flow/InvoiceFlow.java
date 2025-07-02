package org.example.flow;

import org.example.dao.InvoiceDao;
import org.example.dao.OrderDao;
import org.example.pojo.InvoicePojo;
import org.example.pojo.OrderPojo;
import org.example.service.InvoiceService;
import org.example.service.OrderService;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class InvoiceFlow {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private OrderService orderService;

    /**
     * Generate an invoice if one doesn't already exist for the order.
     */
    public InvoicePojo generateInvoice(Integer orderId) {
        // Check if order exists
        OrderPojo order = orderDao.select(orderId);
        if (order == null) {
            throw new ApiException("Order with ID " + orderId + " not found.");
        }

        // Return existing invoice if it already exists
        try {
            InvoicePojo existing = invoiceDao.selectByOrderId(orderId);
            if (existing != null) {
                return existing;
            }
        } catch (Exception e) {
            // No existing invoice found, continue with generation
        }

        // Generate new invoice (no inventory changes)
        InvoicePojo invoice = invoiceService.generateInvoice(orderId);
        
        // Update order status to INVOICED (no inventory impact)
        orderService.updateStatusToInvoiced(orderId);
        
        return invoice;
    }

    /**
     * Generate and return the PDF of an already-created invoice.
     */
    public byte[] generateInvoicePdf(Integer orderId) throws Exception {
        return invoiceService.generateInvoicePdf(orderId);
    }

    /**
     * Return all invoices.
     */
    public List<InvoicePojo> getAll() {
        return invoiceService.getAll();
    }

    /**
     * Return a specific invoice by its ID.
     */
    public InvoicePojo get(Integer id) {
        return invoiceService.get(id);
    }
}
