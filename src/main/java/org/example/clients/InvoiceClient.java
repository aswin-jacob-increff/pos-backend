package org.example.clients;

import org.example.model.data.InvoiceAppForm;
import org.example.exception.ApiException;
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

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(INVOICE_APP_URL, entity, String.class);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Connection refused, timeout, or network issues
            throw new ApiException("Invoice service is not available. Please try again later. Error: " + e.getMessage());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // 4xx errors (client errors)
            throw new ApiException("Invoice service returned an error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // 5xx errors (server errors)
            throw new ApiException("Invoice service is experiencing issues. Please try again later. Error: " + e.getStatusCode());
        }
        return response.getBody();
    }
}
