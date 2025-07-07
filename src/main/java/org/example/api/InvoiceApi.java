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
public class InvoiceApi {

    @Autowired
    private InvoiceDao invoiceDao;



    public InvoicePojo add(InvoicePojo invoice) {
        if (Objects.isNull(invoice)) {
            throw new ApiException("Invoice cannot be null");
        }
        if (Objects.isNull(invoice.getOrderId())) {
            throw new ApiException("Order ID cannot be null");
        }
        if (Objects.isNull(invoice.getFilePath()) || invoice.getFilePath().trim().isEmpty()) {
            throw new ApiException("File path cannot be null or empty");
        }
        
        invoiceDao.insert(invoice);
        return invoice;
    }

    public InvoicePojo get(Integer id) {
        InvoicePojo invoice = invoiceDao.select(id);
        if (Objects.isNull(invoice)) {
            throw new ApiException("Invoice with ID " + id + " not found");
        }
        return invoice;
    }

    public InvoicePojo getByOrderId(Integer orderId) {
        return invoiceDao.selectByOrderId(orderId);
    }

    public List<InvoicePojo> getAll() {
        return invoiceDao.selectAll();
    }

    public void update(Integer id, InvoicePojo updatedInvoice) {
        InvoicePojo existingInvoice = invoiceDao.select(id);
        if (Objects.isNull(existingInvoice)) {
            throw new ApiException("Invoice with ID " + id + " not found");
        }
        invoiceDao.update(id, updatedInvoice);
    }

    public void delete(Integer id) {
        InvoicePojo invoice = invoiceDao.select(id);
        if (Objects.isNull(invoice)) {
            throw new ApiException("Invoice with ID " + id + " not found");
        }
        invoiceDao.delete(id);
    }

    /**
     * Generate invoice for an order. This method is now deprecated - use OrderDto.downloadInvoice() instead.
     */
    @Deprecated
    public String generateInvoice(Object order) {
        throw new ApiException("Use OrderDto.downloadInvoice() instead");
    }


} 