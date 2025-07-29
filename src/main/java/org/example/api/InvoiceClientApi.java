package org.example.api;

import org.example.model.data.InvoiceAppForm;
import org.example.clients.InvoiceClient;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceClientApi {

    @Autowired
    private InvoiceClient invoiceClient;

    private static final String INVOICE_APP_URL = "http://localhost:8081/api/invoice";

    public String generateInvoice(InvoiceAppForm invoiceAppForm) {
        try {
            return invoiceClient.callInvoiceService(invoiceAppForm);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Connection refused, timeout, or network issues
            throw new ApiException("Invoice service is not available. Please try again later. Error: " + e.getMessage());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // 4xx errors (client errors)
            throw new ApiException("Invoice service returned an error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // 5xx errors (server errors)
            throw new ApiException("Invoice service is experiencing issues. Please try again later. Error: " + e.getStatusCode());
        } catch (org.springframework.web.client.RestClientException e) {
            // Other RestTemplate exceptions
            throw new ApiException("Failed to communicate with invoice service: " + e.getMessage());
        } catch (Exception e) {
            // Any other unexpected exceptions
            throw new ApiException("Failed to connect to invoice service at " + INVOICE_APP_URL + ": " + e.getMessage());
        }
    }
}
