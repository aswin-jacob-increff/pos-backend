package org.example.dto;

import org.example.model.ClientForm;
import org.example.model.ClientData;
import org.example.pojo.ClientPojo;
import org.example.flow.ClientFlow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class ClientDto {

    @Autowired
    private ClientFlow clientFlow;

    public ClientData add(ClientForm clientForm) {
        validate(clientForm);
        ClientPojo clientPojo = convert(clientForm);
        clientFlow.add(clientPojo);
        return convert(clientPojo);
    }

    public ClientData get(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Integer ID cannot be null");
        }
        ClientPojo clientPojo = clientFlow.get(id);
        return convert(clientPojo);
    }

    public ClientData getByName(String clientName) {
        if (clientName.trim().isEmpty()) {
            throw new IllegalArgumentException("Client Name cannot be null");
        }
        ClientPojo clientPojo = clientFlow.getByName(clientName);
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

    public ClientData update(Integer id, ClientForm clientForm) {
        if (id == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }
        validate(clientForm);
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
        clientFlow.deleteClientByName(name);
    }

    public void uploadClientTsv(MultipartFile file) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.trim().split("\t");
                if (parts.length < 1) continue;

                String clientName = parts[0].trim().toLowerCase();
                if (clientName.isEmpty()) continue;

                clientFlow.createClient(clientName);
            }
        }
    }

    private void validate(ClientForm clientForm) {
        if(clientForm.getClientName() == null || clientForm.getClientName().trim().isEmpty()) {
            throw new RuntimeException("Client name cannot be empty");
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
