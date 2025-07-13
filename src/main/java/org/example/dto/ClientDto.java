package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.ClientForm;
import org.example.model.ClientData;
import org.example.pojo.ClientPojo;
import org.example.flow.ClientFlow;
import org.example.api.ClientApi;
import org.example.util.StringUtil;
import org.example.util.FileValidationUtil;
import org.example.util.ClientTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

@Component
public class ClientDto extends AbstractDto<ClientPojo, ClientForm, ClientData> {

    @Autowired
    private ClientFlow flow;

    @Override
    protected String getEntityName() {
        return "Client";
    }

    @Override
    protected ClientPojo convertFormToEntity(ClientForm form) {
        ClientPojo pojo = new ClientPojo();
        pojo.setClientName(StringUtil.format(form.getClientName()));
        pojo.setStatus(form.getStatus() != null ? form.getStatus() : true); // Default to true if null
        return pojo;
    }

    @Override
    protected ClientData convertEntityToData(ClientPojo clientPojo) {
        ClientData clientData = new ClientData();
        clientData.setId(clientPojo.getId());
        clientData.setClientName(clientPojo.getClientName());
        clientData.setStatus(clientPojo.getStatus());
        return clientData;
    }

    protected ClientPojo convertDataToEntity(ClientData clientData) {
        ClientPojo clientPojo = new ClientPojo();
        clientPojo.setId(clientData.getId());
        clientPojo.setClientName(clientData.getClientName());
        clientPojo.setStatus(clientData.getStatus());
        return clientPojo;
    }

    @Override
    protected void preprocess(ClientForm clientForm) {
        // Validate clientName is provided
        if (clientForm == null) {
            throw new ApiException("Client form cannot be null");
        }
        if (clientForm.getClientName() == null || clientForm.getClientName().trim().isEmpty()) {
            throw new ApiException("Client name is required");
        }
    }

    @Override
//    @org.springframework.transaction.annotation.Transactional
    public ClientData update(Integer id, @Valid ClientForm form) {
        return super.update(id, form);
    }

    public List<ClientData> getAll() {
        return flow.getAll().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }

    // Custom methods that don't fit the generic pattern

    public void toggleStatus(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("Client ID cannot be null");
        }
        flow.toggleStatus(id);
    }

    public void toggleStatusByName(String name) {
        if (Objects.isNull(name) || name.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        flow.toggleStatusByName(StringUtil.format(name));
    }

    public ClientData getByNameOrId(Integer id, String name) {
        name = StringUtil.format(name);
        if (Objects.nonNull(id) && Objects.nonNull(name)) {
            try {
                ClientPojo idPojo = api.get(id);
                ClientPojo namePojo = ((ClientApi) api).getByName(name);
                if (idPojo.equals(namePojo)) {
                    return convertEntityToData(idPojo);
                } else {
                    throw new ApiException("Client Name and Client ID does not match");
                }
            } catch (ApiException e) {
                throw new ApiException(e.getMessage());
            }
        } else if (Objects.nonNull(id)) {
            return convertEntityToData(api.get(id));
        } else {
            return convertEntityToData(((ClientApi) api).getByName(name));
        }
    }

    public String uploadClientsFromTsv(MultipartFile file) {
        // Validate file
        FileValidationUtil.validateTsvFile(file);
        try {
            List<ClientForm> forms = ClientTsvParser.parse(file.getInputStream());
            FileValidationUtil.validateFileSize(forms.size());
            // Only add if all are valid
            for (ClientForm form : forms) {
                add(form);
            }
            return "Uploaded " + forms.size() + " clients";
        } catch (ApiException e) {
            // Propagate parser validation errors
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Error while processing file: " + e.getMessage());
        }
    }

    public void toggleStatus(Integer id, String name) {
        if (id != null) {
            toggleStatus(id);
        } else if (name != null) {
            toggleStatusByName(name);
        } else {
            throw new ApiException("Either 'id' or 'name' must be provided for status toggle");
        }
    }
}
