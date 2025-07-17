package org.example.flow;

import org.example.pojo.ClientPojo;
import org.example.pojo.ProductPojo;
import org.example.api.ClientApi;
import org.example.api.ProductApi;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ClientFlow extends AbstractFlow<ClientPojo> {

    @Autowired
    private ClientApi api;

    @Autowired
    private ProductApi productApi;

    public ClientFlow() {
        super(ClientPojo.class);
    }

    @Override
    protected Integer getEntityId(ClientPojo pojo) {
        return pojo.getId();
    }

    @Override
    protected String getEntityName() {
        return "Client";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void update(Integer id, ClientPojo clientPojo) {
        if (Objects.isNull(id)) {
            throw new ApiException("Client ID cannot be null");
        }
        if (Objects.isNull(clientPojo)) {
            throw new ApiException("Client cannot be null");
        }
        
        // Get the existing client to compare the client name
        ClientPojo existingClient = api.get(id);
        if (existingClient == null) {
            throw new ApiException("Client with ID " + id + " not found");
        }
        
        // If the client name has changed, find all products with the old client name FIRST
        List<ProductPojo> productsToUpdate = null;
        // Compare the actual stored values (both should be in lowercase due to StringUtil.format)
        if (!Objects.equals(existingClient.getClientName(), clientPojo.getClientName())) {
            String oldClientName = existingClient.getClientName();
            System.out.println("Client name changed from '" + oldClientName + "' to '" + clientPojo.getClientName() + "'");
            productsToUpdate = productApi.getByClientName(oldClientName);
            System.out.println("Found " + (productsToUpdate != null ? productsToUpdate.size() : 0) + " products to update");
        }
        
        // Update the client
        api.update(id, clientPojo);
        
        // Now update all products with the new client name
        if (productsToUpdate != null && !productsToUpdate.isEmpty()) {
            String newClientName = clientPojo.getClientName();
            System.out.println("Updating " + productsToUpdate.size() + " products with new client name: '" + newClientName + "'");
            for (ProductPojo product : productsToUpdate) {
                System.out.println("Updating product ID " + product.getId() + " client name from '" + product.getClientName() + "' to '" + newClientName + "'");
                product.setClientName(newClientName);
                productApi.update(product.getId(), product);
            }
        }
    }

    public ClientPojo getByName(String name) {
        return api.getByName(name);
    }

    public ClientPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("Client ID cannot be null");
        }
        return api.get(id);
    }

    public void toggleStatus(Integer id) {
        ClientPojo pojo = api.get(id);
        if (pojo == null) {
            throw new ApiException("Client with ID '" + id + "' not found.");
        }
        api.toggleStatus(id);
    }

    public void toggleStatusByName(String name) {
        ClientPojo pojo = api.getByName(name);
        if (pojo == null) {
            throw new ApiException("Client with name '" + name + "' not found.");
        }
        api.toggleStatusByName(name);
    }

    public void createClient(String name) {
        ClientPojo pojo = new ClientPojo();
        pojo.setClientName(name);
        api.add(pojo);
    }
}
