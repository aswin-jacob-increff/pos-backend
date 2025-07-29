package org.example.dao;

import org.example.pojo.InvoicePojo;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class InvoiceDao extends AbstractDao<InvoicePojo> {

    public InvoiceDao() {
        super(InvoicePojo.class);
    }

    public InvoicePojo selectByOrderId(Integer orderId) {
        List<InvoicePojo> results = getByParams("orderId", orderId);
        return results.isEmpty() ? null : results.get(0);
    }
    
    public List<InvoicePojo> selectAllByOrderId(Integer orderId) {
        return getByParams(new String[]{"orderId"}, new Object[]{orderId});
    }

    @Override
    protected void updateEntity(InvoicePojo existing, InvoicePojo updated) {
        existing.setOrderId(updated.getOrderId());
        existing.setFilePath(updated.getFilePath());
        existing.setInvoiceId(updated.getInvoiceId());
    }
} 