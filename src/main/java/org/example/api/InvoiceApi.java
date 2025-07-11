package org.example.api;

import org.example.dao.InvoiceDao;
import org.example.exception.ApiException;
import org.example.pojo.InvoicePojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class InvoiceApi extends AbstractApi<InvoicePojo> {

    @Autowired
    private InvoiceDao invoiceDao;

    @Override
    protected String getEntityName() {
        return "Invoice";
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
        
        // Delete any existing invoice for this order to prevent duplicates
        try {
            InvoicePojo existingInvoice = invoiceDao.selectByOrderId(invoice.getOrderId());
            if (existingInvoice != null) {
                System.out.println("Deleting existing invoice for order " + invoice.getOrderId() + " before adding new one");
                invoiceDao.delete(existingInvoice.getId());
            }
        } catch (Exception e) {
            // If multiple invoices exist, delete all of them
            System.out.println("Multiple invoices found for order " + invoice.getOrderId() + ", deleting all existing invoices");
            List<InvoicePojo> existingInvoices = invoiceDao.selectAllByOrderId(invoice.getOrderId());
            for (InvoicePojo existing : existingInvoices) {
                invoiceDao.delete(existing.getId());
            }
        }
        
        invoiceDao.insert(invoice);
    }

    public InvoicePojo getByOrderId(Integer orderId) {
        return invoiceDao.selectByOrderId(orderId);
    }

    /**
     * Generate invoice for an order. This method is now deprecated - use OrderDto.downloadInvoice() instead.
     */
    @Deprecated
    public String generateInvoice(Object order) {
        throw new ApiException("Use OrderDto.downloadInvoice() instead");
    }
    
    /**
     * Clean up duplicate invoices for a specific order by keeping only the most recent one
     */
    public void cleanupDuplicateInvoices(Integer orderId) {
        List<InvoicePojo> invoices = invoiceDao.selectAllByOrderId(orderId);
        if (invoices.size() > 1) {
            System.out.println("Found " + invoices.size() + " invoices for order " + orderId + ", keeping only the most recent one");
            
            // Sort by creation date (assuming newer invoices have higher IDs)
            invoices.sort((a, b) -> b.getId().compareTo(a.getId()));
            
            // Keep the first one (most recent) and delete the rest
            for (int i = 1; i < invoices.size(); i++) {
                System.out.println("Deleting duplicate invoice ID: " + invoices.get(i).getId());
                invoiceDao.delete(invoices.get(i).getId());
            }
        }
    }
} 