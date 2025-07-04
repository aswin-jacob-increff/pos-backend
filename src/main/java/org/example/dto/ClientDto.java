package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.ClientForm;
import org.example.model.ClientData;
import org.example.pojo.ClientPojo;
import org.example.flow.ClientFlow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

@Component
public class ClientDto {

    @Autowired
    private ClientFlow clientFlow;

    public ClientData add(@Valid ClientForm clientForm) {
        preprocess(clientForm);
        ClientPojo clientPojo = convert(clientForm);
        clientFlow.add(clientPojo);
        return convert(clientPojo);
    }

    public ClientData get(Integer id) {
        if (id == null) {
            throw new ApiException("Integer ID cannot be null.");
        }
        ClientPojo clientPojo = clientFlow.get(id);
        return convert(clientPojo);
    }

    public ClientData getByName(String clientName) {
        if (clientName.trim().isEmpty()) {
            throw new ApiException("Client Name cannot be null.");
        }
        ClientPojo clientPojo = clientFlow.getByName(clientName.trim().toLowerCase());
        return convert(clientPojo);
    }

    public List<ClientData> getAll() {
        List<ClientPojo> clientPojoList = clientFlow.getAll();
        List<ClientData> clientDataList = new ArrayList<>();
        for (ClientPojo clientPojo : clientPojoList) {
            clientDataList.add(convert(clientPojo));
        }
        return clientDataList;
    }

    public ClientData update(Integer id, @Valid ClientForm clientForm) {
        if (id == null) {
            throw new ApiException("Client ID cannot be null.");
        }
        preprocess(clientForm);
        return convert(clientFlow.update(id, convert(clientForm)));
    }

    public void delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }
        clientFlow.delete(id);
    }

    public void deleteByName(String name) {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Client name cannot be null");
        }
        clientFlow.deleteClientByName(name.trim().toLowerCase());
    }

    private void preprocess(ClientForm clientForm) {
        if (clientForm.getClientName() != null) {
            clientForm.setClientName(clientForm.getClientName().trim().toLowerCase());
        }
    }

    private ClientPojo convert(ClientForm clientForm) {
        ClientPojo clientPojo = new ClientPojo();
        clientPojo.setClientName(clientForm.getClientName());
        return clientPojo;
    }

    private ClientData convert(ClientPojo clientPojo) {
        ClientData clientData = new ClientData();
        clientData.setId(clientPojo.getId());
        clientData.setClientName(clientPojo.getClientName());
        return clientData;
    }
}
