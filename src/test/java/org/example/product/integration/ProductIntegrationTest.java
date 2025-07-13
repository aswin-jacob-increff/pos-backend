package org.example.product.integration;

import org.example.model.ProductForm;
import org.example.model.ProductData;
import org.example.model.ClientForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {org.example.config.SpringConfig.class})
@Transactional
public class ProductIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testAddAndGetProduct() throws Exception {
        // Add a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("ProductTestClient1");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Add product
        ProductForm form = new ProductForm();
        form.setClientName("ProductTestClient1");
        form.setName("Test Product 1");
        form.setBarcode("PROD123456");
        form.setMrp(99.99);
        String json = objectMapper.writeValueAsString(form);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Get product by barcode
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/products/barcode/PROD123456"))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        ProductData product = objectMapper.readValue(response, ProductData.class);
        assertThat(product.getName()).isEqualTo("Test Product 1");
        assertThat(product.getBarcode()).isEqualTo("PROD123456");
        assertThat(product.getMrp()).isEqualTo(99.99);
        assertThat(product.getClientName()).isEqualTo("producttestclient1");
    }

    @Test
    public void testAddDuplicateProduct() throws Exception {
        // Add a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("ProductTestClient2");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Add product
        ProductForm form = new ProductForm();
        form.setClientName("ProductTestClient2");
        form.setName("Test Product 2");
        form.setBarcode("PROD654321");
        form.setMrp(49.99);
        String json = objectMapper.writeValueAsString(form);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Add duplicate product (same barcode)
        ProductForm duplicateForm = new ProductForm();
        duplicateForm.setClientName("ProductTestClient2");
        duplicateForm.setName("Duplicate Product");
        duplicateForm.setBarcode("PROD654321"); // Same barcode
        duplicateForm.setMrp(29.99);
        String duplicateJson = objectMapper.writeValueAsString(duplicateForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetNonExistentProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/barcode/NO_SUCH_BARCODE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddProductForInactiveClient() throws Exception {
        // Add a client
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("InactiveClient");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Toggle client to inactive
        mockMvc.perform(MockMvcRequestBuilders.put("/api/clients/toggle")
                .param("name", "InactiveClient"))
                .andExpect(status().isOk());

        // Try to add product for inactive client
        ProductForm form = new ProductForm();
        form.setClientName("InactiveClient");
        form.setName("Inactive Product");
        form.setBarcode("INACTIVEPROD");
        form.setMrp(10.0);
        String json = objectMapper.writeValueAsString(form);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
} 