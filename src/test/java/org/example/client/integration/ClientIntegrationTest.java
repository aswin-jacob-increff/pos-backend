package org.example.client.integration;

import org.example.model.ClientForm;
import org.example.model.ClientData;
import org.example.pojo.ClientPojo;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import org.hamcrest.Matchers;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {org.example.config.SpringConfig.class})
@Transactional
public class ClientIntegrationTest {

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
    public void testAddAndGetClient() throws Exception {
        ClientForm form = new ClientForm();
        form.setClientName("UniqueTestClient123");
        String json = objectMapper.writeValueAsString(form);

        // Add client
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Get client by name
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/clients/search")
                .param("name", "UniqueTestClient123"))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        ClientData client = objectMapper.readValue(response, ClientData.class);
        assertThat(client.getClientName()).isEqualTo("uniquetestclient123");
        assertThat(client.getStatus()).isTrue();
    }

    @Test
    @Transactional
    public void testToggleStatusAndBusinessRule() throws Exception {
        // First add a client
        ClientForm form = new ClientForm();
        form.setClientName("Toggle Test Client");
        String json = objectMapper.writeValueAsString(form);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Toggle status to false
        mockMvc.perform(MockMvcRequestBuilders.put("/api/clients/toggle")
                .param("name", "Toggle Test Client"))
                .andExpect(status().isOk());

        // Verify status is now false
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/clients/search")
                .param("name", "Toggle Test Client"))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        ClientData client = objectMapper.readValue(response, ClientData.class);
        assertThat(client.getStatus()).isFalse();
    }

    @Test
    @Transactional
    public void testAddDuplicateClient() throws Exception {
        ClientForm form = new ClientForm();
        form.setClientName("Duplicate Test Client");
        String json = objectMapper.writeValueAsString(form);

        // First add should succeed
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // Second add with same name should fail
        mockMvc.perform(MockMvcRequestBuilders.post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void testGetNonExistentClient() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clients/search")
                .param("name", "NoSuchClient"))
                .andExpect(status().isBadRequest());
    }
} 