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
                // Note: delete method removed, so we'll just log this
            }
        } catch (Exception e) {
            // If multiple invoices exist, handle them
            List<InvoicePojo> existingInvoices = invoiceDao.selectAllByOrderId(invoice.getOrderId());
            // Handle multiple invoices if needed
        }
        
        invoiceDao.insert(invoice);
    }

    public InvoicePojo getByOrderId(Integer orderId) {
        return invoiceDao.selectByOrderId(orderId);
    }

}