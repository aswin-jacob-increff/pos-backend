package org.example.util;

import org.example.model.data.TsvUploadResult;
import org.example.model.form.ClientForm;
import org.example.model.form.ProductForm;
import org.example.model.form.InventoryForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TsvParserTest {

    @Test
    void testClientTsvParserWithDuplicates() throws Exception {
        // Create TSV content with duplicates
        String tsvContent = "clientName\n" +
                           "client1\n" +
                           "client2\n" +
                           "client1\n" +  // Duplicate
                           "client3\n" +
                           "client2\n";   // Duplicate

        InputStream inputStream = new ByteArrayInputStream(tsvContent.getBytes(StandardCharsets.UTF_8));
        
        TsvUploadResult result = ClientTsvParser.parseWithDuplicateDetection(inputStream);
        
        // Verify results
        assertEquals(5, result.getTotalRows());
        assertEquals(3, result.getSuccessfulRows()); // Only unique clients
        assertEquals(2, result.getFailedRows());     // Duplicates
        assertEquals(2, result.getWarnings().size());
        assertTrue(result.getWarnings().get(0).contains("duplicate client name"));
        assertTrue(result.getWarnings().get(1).contains("duplicate client name"));
        
        // Verify parsed forms
        List<ClientForm> forms = result.getParsedForms();
        assertEquals(3, forms.size());
        assertEquals("client1", forms.get(0).getClientName());
        assertEquals("client2", forms.get(1).getClientName());
        assertEquals("client3", forms.get(2).getClientName());
    }

    @Test
    void testProductTsvParserWithDuplicates() throws Exception {
        // Create TSV content with duplicates
        String tsvContent = "barcode\tclientname\tproductname\tmrp\timageurl\n" +
                           "BAR001\tclient1\tProduct1\t10.50\thttp://image1.jpg\n" +
                           "BAR002\tclient1\tProduct2\t15.75\thttp://image2.jpg\n" +
                           "BAR001\tclient1\tProduct1\t10.50\thttp://image1.jpg\n" +  // Duplicate barcode
                           "BAR003\tclient2\tProduct3\t20.00\thttp://image3.jpg\n" +
                           "BAR002\tclient1\tProduct2\t15.75\thttp://image2.jpg\n";   // Duplicate barcode

        InputStream inputStream = new ByteArrayInputStream(tsvContent.getBytes(StandardCharsets.UTF_8));
        
        TsvUploadResult result = ProductTsvParser.parseWithDuplicateDetection(inputStream);
        
        // Verify results
        assertEquals(5, result.getTotalRows());
        assertEquals(3, result.getSuccessfulRows()); // Only unique products
        assertEquals(2, result.getFailedRows());     // Duplicates
        assertEquals(2, result.getWarnings().size());
        assertTrue(result.getWarnings().get(0).contains("duplicate barcode"));
        assertTrue(result.getWarnings().get(1).contains("duplicate barcode"));
        
        // Verify parsed forms
        List<ProductForm> forms = result.getParsedForms();
        assertEquals(3, forms.size());
        assertEquals("bar001", forms.get(0).getBarcode());
        assertEquals("bar002", forms.get(1).getBarcode());
        assertEquals("bar003", forms.get(2).getBarcode());
    }

    @Test
    void testInventoryTsvParserWithDuplicates() throws Exception {
        // Create TSV content with duplicates
        String tsvContent = "barcode\tquantity\n" +
                           "BAR001\t50\n" +
                           "BAR002\t25\n" +
                           "BAR001\t30\n" +  // Duplicate barcode
                           "BAR003\t100\n" +
                           "BAR002\t75\n";   // Duplicate barcode

        InputStream inputStream = new ByteArrayInputStream(tsvContent.getBytes(StandardCharsets.UTF_8));
        
        TsvUploadResult result = InventoryTsvParser.parseWithDuplicateDetection(inputStream);
        
        // Verify results
        assertEquals(5, result.getTotalRows());
        assertEquals(3, result.getSuccessfulRows()); // Only unique inventory items
        assertEquals(2, result.getFailedRows());     // Duplicates
        assertEquals(2, result.getWarnings().size());
        assertTrue(result.getWarnings().get(0).contains("duplicate barcode"));
        assertTrue(result.getWarnings().get(1).contains("duplicate barcode"));
        
        // Verify parsed forms
        List<InventoryForm> forms = result.getParsedForms();
        assertEquals(3, forms.size());
        assertEquals("bar001", forms.get(0).getBarcode());
        assertEquals(50, forms.get(0).getQuantity());
        assertEquals("bar002", forms.get(1).getBarcode());
        assertEquals(25, forms.get(1).getQuantity());
        assertEquals("bar003", forms.get(2).getBarcode());
        assertEquals(100, forms.get(2).getQuantity());
    }

    @Test
    void testClientTsvParserWithValidationErrors() throws Exception {
        // Create TSV content with validation errors
        String tsvContent = "clientName\n" +
                           "client1\n" +
                           "\n" +           // Empty client name
                           "client2\n" +
                           "client3\n" +
                           "client4\n";

        InputStream inputStream = new ByteArrayInputStream(tsvContent.getBytes(StandardCharsets.UTF_8));
        
        TsvUploadResult result = ClientTsvParser.parseWithDuplicateDetection(inputStream);
        
        // Verify results
        assertEquals(5, result.getTotalRows());
        assertEquals(4, result.getSuccessfulRows());
        assertEquals(1, result.getFailedRows());     // Empty client name
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Client name cannot be empty"));
        
        // Verify parsed forms
        List<ClientForm> forms = result.getParsedForms();
        assertEquals(4, forms.size());
    }

    @Test
    void testProductTsvParserWithValidationErrors() throws Exception {
        // Create TSV content with validation errors
        String tsvContent = "barcode\tclientname\tproductname\tmrp\timageurl\n" +
                           "BAR001\tclient1\tProduct1\t10.50\thttp://image1.jpg\n" +
                           "\tclient1\tProduct2\t15.75\thttp://image2.jpg\n" +  // Empty barcode
                           "BAR002\tclient1\tProduct3\tinvalid\thttp://image3.jpg\n" +  // Invalid MRP
                           "BAR003\tclient2\tProduct4\t20.00\thttp://image4.jpg\n";

        InputStream inputStream = new ByteArrayInputStream(tsvContent.getBytes(StandardCharsets.UTF_8));
        
        TsvUploadResult result = ProductTsvParser.parseWithDuplicateDetection(inputStream);
        
        // Verify results
        assertEquals(4, result.getTotalRows());
        assertEquals(2, result.getSuccessfulRows());
        assertEquals(2, result.getFailedRows());     // Empty barcode + invalid MRP
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Barcode cannot be empty"));
        assertTrue(result.getErrors().get(1).contains("Invalid MRP value"));
        
        // Verify parsed forms
        List<ProductForm> forms = result.getParsedForms();
        assertEquals(2, forms.size());
    }

    @Test
    void testInventoryTsvParserWithValidationErrors() throws Exception {
        // Create TSV content with validation errors
        String tsvContent = "barcode\tquantity\n" +
                           "BAR001\t50\n" +
                           "\t25\n" +           // Empty barcode
                           "BAR002\t-10\n" +    // Negative quantity
                           "BAR003\tinvalid\n" + // Invalid quantity
                           "BAR004\t100\n";

        InputStream inputStream = new ByteArrayInputStream(tsvContent.getBytes(StandardCharsets.UTF_8));
        
        TsvUploadResult result = InventoryTsvParser.parseWithDuplicateDetection(inputStream);
        
        // Verify results
        assertEquals(5, result.getTotalRows());
        assertEquals(2, result.getSuccessfulRows());
        assertEquals(3, result.getFailedRows());     // Empty barcode + negative quantity + invalid quantity
        assertEquals(3, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Barcode cannot be empty"));
        assertTrue(result.getErrors().get(1).contains("Quantity cannot be negative"));
        assertTrue(result.getErrors().get(2).contains("Invalid quantity value"));
        
        // Verify parsed forms
        List<InventoryForm> forms = result.getParsedForms();
        assertEquals(2, forms.size());
    }
} 