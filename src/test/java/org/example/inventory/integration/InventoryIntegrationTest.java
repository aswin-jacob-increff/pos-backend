package org.example.inventory.integration;

import org.example.model.InventoryForm;
import org.example.model.InventoryData;
import org.example.model.ClientForm;
import org.example.model.ProductForm;
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
import org.hamcrest.Matchers;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {org.example.config.SpringConfig.class})
@Transactional
public class InventoryIntegrationTest {

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
    public void testAddAndGetInventory() throws Exception {
        // Add a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("InventoryTestClient1");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Add a product first
        ProductForm productForm = new ProductForm();
        productForm.setClientName("InventoryTestClient1");
        productForm.setName("Test Product 1");
        productForm.setBarcode("INV123456");
        productForm.setMrp(100.0);
        String productJson = objectMapper.writeValueAsString(productForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isOk());

        // Add inventory
        InventoryForm form = new InventoryForm();
        form.setBarcode("INV123456");
        form.setProductName("Test Product 1");
        form.setClientName("InventoryTestClient1");
        form.setQuantity(50);
        form.setMrp(100.0);
        String json = objectMapper.writeValueAsString(form);

        // Add inventory
        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Get inventory by product barcode
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/product/INV123456"))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        InventoryData inventory = objectMapper.readValue(response, InventoryData.class);
        assertThat(inventory.getBarcode()).isEqualTo("INV123456");
        assertThat(inventory.getQuantity()).isEqualTo(50);
        assertThat(inventory.getMrp()).isEqualTo(100.0);
    }

    @Test
    public void testAddInventoryForInactiveClient() throws Exception {
        // Add a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("InactiveInventoryClient");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Deactivate the client first
        mockMvc.perform(MockMvcRequestBuilders.put("/api/clients/toggle")
                .param("name", "InactiveInventoryClient"))
                .andExpect(status().isOk());

        // Try to add inventory for a non-existent product (should fail due to inactive client)
        InventoryForm form = new InventoryForm();
        form.setBarcode("NONEXISTENT123");
        form.setQuantity(10);
        String json = objectMapper.writeValueAsString(form);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetNonExistentInventory() throws Exception {
        // Try to get inventory for non-existent product
        mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/product/NO_SUCH_BARCODE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddStock() throws Exception {
        // Add a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("StockTestClient");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Add a product first
        ProductForm productForm = new ProductForm();
        productForm.setClientName("StockTestClient");
        productForm.setName("Stock Test Product");
        productForm.setBarcode("STOCK123");
        productForm.setMrp(200.0);
        String productJson = objectMapper.writeValueAsString(productForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isOk());

        // Add inventory
        InventoryForm form = new InventoryForm();
        form.setBarcode("STOCK123");
        form.setProductName("Stock Test Product");
        form.setClientName("StockTestClient");
        form.setQuantity(100);
        form.setMrp(200.0);
        String json = objectMapper.writeValueAsString(form);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Add stock
        mockMvc.perform(MockMvcRequestBuilders.put("/api/inventory/STOCK123/addStock")
                .param("quantity", "50"))
                .andExpect(status().isOk());

        // Verify stock was added
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/product/STOCK123"))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        InventoryData inventory = objectMapper.readValue(response, InventoryData.class);
        assertThat(inventory.getQuantity()).isEqualTo(150);
    }

    @Test
    public void testRemoveStock() throws Exception {
        // Add a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("RemoveStockClient");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Add a product first
        ProductForm productForm = new ProductForm();
        productForm.setClientName("RemoveStockClient");
        productForm.setName("Remove Stock Product");
        productForm.setBarcode("REMOVE123");
        productForm.setMrp(300.0);
        String productJson = objectMapper.writeValueAsString(productForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isOk());

        // Add inventory
        InventoryForm form = new InventoryForm();
        form.setBarcode("REMOVE123");
        form.setProductName("Remove Stock Product");
        form.setClientName("RemoveStockClient");
        form.setQuantity(200);
        form.setMrp(300.0);
        String json = objectMapper.writeValueAsString(form);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Remove stock
        mockMvc.perform(MockMvcRequestBuilders.put("/api/inventory/REMOVE123/removeStock")
                .param("quantity", "75"))
                .andExpect(status().isOk());

        // Verify stock was removed
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/product/REMOVE123"))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        InventoryData inventory = objectMapper.readValue(response, InventoryData.class);
        assertThat(inventory.getQuantity()).isEqualTo(125);
    }

    @Test
    public void testRemoveStockInsufficientQuantity() throws Exception {
        // Add a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("InsufficientStockClient");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Add a product first
        ProductForm productForm = new ProductForm();
        productForm.setClientName("InsufficientStockClient");
        productForm.setName("Insufficient Stock Product");
        productForm.setBarcode("INSUFF123");
        productForm.setMrp(400.0);
        String productJson = objectMapper.writeValueAsString(productForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isOk());

        // Add inventory with low quantity
        InventoryForm form = new InventoryForm();
        form.setBarcode("INSUFF123");
        form.setProductName("Insufficient Stock Product");
        form.setClientName("InsufficientStockClient");
        form.setQuantity(10);
        form.setMrp(400.0);
        String json = objectMapper.writeValueAsString(form);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Try to remove more stock than available (should fail)
        mockMvc.perform(MockMvcRequestBuilders.put("/api/inventory/INSUFF123/removeStock")
                .param("quantity", "25"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSetStock() throws Exception {
        // Add a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("SetStockClient");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Add a product first
        ProductForm productForm = new ProductForm();
        productForm.setClientName("SetStockClient");
        productForm.setName("Set Stock Product");
        productForm.setBarcode("SET123");
        productForm.setMrp(500.0);
        String productJson = objectMapper.writeValueAsString(productForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isOk());

        // Add inventory
        InventoryForm form = new InventoryForm();
        form.setBarcode("SET123");
        form.setProductName("Set Stock Product");
        form.setClientName("SetStockClient");
        form.setQuantity(100);
        form.setMrp(500.0);
        String json = objectMapper.writeValueAsString(form);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Set stock to new quantity
        mockMvc.perform(MockMvcRequestBuilders.put("/api/inventory/SET123/setStock")
                .param("quantity", "250"))
                .andExpect(status().isOk());

        // Verify stock was set
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/product/SET123"))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        InventoryData inventory = objectMapper.readValue(response, InventoryData.class);
        assertThat(inventory.getQuantity()).isEqualTo(250);
    }

    @Test
    public void testSetStockNegativeQuantity() throws Exception {
        // Add a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("NegativeStockClient");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Add a product first
        ProductForm productForm = new ProductForm();
        productForm.setClientName("NegativeStockClient");
        productForm.setName("Negative Stock Product");
        productForm.setBarcode("NEG123");
        productForm.setMrp(600.0);
        String productJson = objectMapper.writeValueAsString(productForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isOk());

        // Add inventory
        InventoryForm form = new InventoryForm();
        form.setBarcode("NEG123");
        form.setProductName("Negative Stock Product");
        form.setClientName("NegativeStockClient");
        form.setQuantity(100);
        form.setMrp(600.0);
        String json = objectMapper.writeValueAsString(form);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Try to set negative stock (should fail)
        mockMvc.perform(MockMvcRequestBuilders.put("/api/inventory/NEG123/setStock")
                .param("quantity", "-50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInventoryByProductName() throws Exception {
        // Add a client first
        ClientForm clientForm = new ClientForm();
        clientForm.setClientName("NameSearchClient");
        String clientJson = objectMapper.writeValueAsString(clientForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
                .andExpect(status().isOk());

        // Add a product first
        ProductForm productForm = new ProductForm();
        productForm.setClientName("NameSearchClient");
        productForm.setName("Name Search Product");
        productForm.setBarcode("NAME123");
        productForm.setMrp(700.0);
        String productJson = objectMapper.writeValueAsString(productForm);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isOk());

        // Add inventory
        InventoryForm form = new InventoryForm();
        form.setBarcode("NAME123");
        form.setProductName("Name Search Product");
        form.setClientName("NameSearchClient");
        form.setQuantity(300);
        form.setMrp(700.0);
        String json = objectMapper.writeValueAsString(form);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Get inventory by product name
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory/byProduct")
                .param("name", "Name Search Product"))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        InventoryData inventory = objectMapper.readValue(response, InventoryData.class);
        assertThat(inventory.getProductName()).isEqualTo("Name Search Product");
        assertThat(inventory.getQuantity()).isEqualTo(300);
    }
} 