package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.ClientForm;
import org.example.model.ClientData;
import org.example.pojo.ClientPojo;
import org.example.flow.ClientFlow;
import org.example.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        if (Objects.isNull(id)) {
            throw new ApiException("Integer ID cannot be null.");
        }
        ClientPojo clientPojo = clientFlow.get(id);
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
        if (Objects.isNull(id)) {
            throw new ApiException("Client ID cannot be null.");
        }
        preprocess(clientForm);
        return convert(clientFlow.update(id, convert(clientForm)));
    }

    public void delete(Integer id) {
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }
        clientFlow.delete(id);
    }

    public void deleteByName(String name) {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Client name cannot be null");
        }
        clientFlow.deleteClientByName(StringUtil.format(name));
    }

    public void toggleStatus(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("Client ID cannot be null");
        }
        clientFlow.toggleStatus(id);
    }

    public void toggleStatusByName(String name) {
        if (Objects.isNull(name) || name.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        clientFlow.toggleStatusByName(StringUtil.format(name));
    }

    private void preprocess(ClientForm clientForm) {
        if (clientForm.getClientName() != null) {
            clientForm.setClientName(StringUtil.format(clientForm.getClientName()));
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

    public ClientData getByNameOrId(Integer id, String name) {
        name = StringUtil.format(name);
        if (Objects.nonNull(id) && Objects.nonNull(name)) {
            try {
                ClientPojo idPojo = clientFlow.get(id);
                ClientPojo namePojo = clientFlow.getByName(name);
                if (idPojo.equals(namePojo)) {
                    return convert(idPojo);
                } else {
                    throw new ApiException("Client Name and Client ID does not match");
                }
            } catch (ApiException e) {
                throw new ApiException(e.getMessage());
            }
        } else if (Objects.nonNull(id)) {
            return convert(clientFlow.get(id));
        } else {
            return convert(clientFlow.getByName(name));
        }
    }
}
