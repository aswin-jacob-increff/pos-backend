package org.example.clients;

import org.example.model.data.InvoiceAppForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Component
public class InvoiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String INVOICE_APP_URL = "http://localhost:8081/api/invoice";

    public String callInvoiceService(InvoiceAppForm invoiceAppForm) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<InvoiceAppForm> entity = new HttpEntity<>(invoiceAppForm, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(INVOICE_APP_URL, entity, String.class);
        return response.getBody();
    }
}
