package org.example.api;

import org.example.model.data.InvoiceAppForm;
import org.example.clients.InvoiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceClientApi {

    @Autowired
    private InvoiceClient invoiceClient;

    public String generateInvoice(InvoiceAppForm invoiceAppForm) {
        return invoiceClient.callInvoiceService(invoiceAppForm);
    }
}
