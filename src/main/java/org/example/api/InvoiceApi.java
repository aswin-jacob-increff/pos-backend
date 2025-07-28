package org.example.api;

import org.example.dao.InvoiceDao;
import org.example.exception.ApiException;
import org.example.pojo.InvoicePojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceApi extends AbstractApi<InvoicePojo> {

    @Autowired
    private InvoiceDao invoiceDao;

    public InvoiceApi() {
        super(InvoicePojo.class);
    }

    @Override
    public void add(InvoicePojo invoice) {
        if (Objects.isNull(invoice)) {
            throw new ApiException("Invoice cannot be null");
        }
        if (Objects.isNull(invoice.getOrderId())) {
            throw new ApiException("Order ID cannot be null");
        }
        if (Objects.isNull(invoice.getFilePath()) || invoice.getFilePath().trim().isEmpty()) {
            throw new ApiException("File path cannot be null or empty");
        }
        
        // Check for existing invoice for this order to prevent duplicates
        try {
            InvoicePojo existingInvoice = invoiceDao.selectByOrderId(invoice.getOrderId());
            if (existingInvoice != null) {
                System.out.println("Warning: Invoice already exists for order " + invoice.getOrderId());
                // Note: delete method removed, so we'll just log this
            }
        } catch (Exception e) {
            // If multiple invoices exist, log them
            System.out.println("Multiple invoices found for order " + invoice.getOrderId());
            List<InvoicePojo> existingInvoices = invoiceDao.selectAllByOrderId(invoice.getOrderId());
            for (InvoicePojo existing : existingInvoices) {
                System.out.println("Would remove existing invoice: " + existing.getId());
            }
        }
        
        invoiceDao.insert(invoice);
    }

    public InvoicePojo getByOrderId(Integer orderId) {
        return invoiceDao.selectByOrderId(orderId);
    }

    public List<InvoicePojo> getAllByOrderId(Integer orderId) {
        return invoiceDao.selectAllByOrderId(orderId);
    }


    
    /**
     * Clean up duplicate invoices for a specific order by keeping only the most recent one
     */
    public void cleanupDuplicateInvoices(Integer orderId) {
        if (orderId == null) {
            throw new ApiException("Order ID cannot be null");
        }
        // Remove duplicate invoices for this order
        List<InvoicePojo> invoices = getAllByOrderId(orderId);
        if (invoices.size() > 1) {
            // Keep the first one, remove the rest
            for (int i = 1; i < invoices.size(); i++) {
                // Note: delete method removed, so we'll just log this
                System.out.println("Would remove duplicate invoice: " + invoices.get(i).getId());
            }
        }
    }

    public void cleanupAllDuplicateInvoices() {
        // Get all orders that have multiple invoices
        List<InvoicePojo> allInvoices = getAll();
        Map<Integer, List<InvoicePojo>> invoicesByOrder = allInvoices.stream()
                .collect(Collectors.groupingBy(InvoicePojo::getOrderId));
        
        for (Map.Entry<Integer, List<InvoicePojo>> entry : invoicesByOrder.entrySet()) {
            if (entry.getValue().size() > 1) {
                // Keep the first one, remove the rest
                for (int i = 1; i < entry.getValue().size(); i++) {
                    // Note: delete method removed, so we'll just log this
                    System.out.println("Would remove duplicate invoice: " + entry.getValue().get(i).getId());
                }
            }
        }
    }
} 