package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.data.TsvUploadResult;
import org.example.model.form.ClientForm;
import org.example.model.data.ClientData;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.pojo.ClientPojo;
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

    @Override
    protected String getEntityName() {
        return "Client";
    }

    @Override
    protected ClientPojo convertFormToEntity(ClientForm form) {
        ClientPojo pojo = new ClientPojo();
        pojo.setClientName(StringUtil.normalise(form.getClientName()));
        pojo.setStatus(form.getStatus() != null ? form.getStatus() : true);
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

    @Override
    protected void preprocess(ClientForm clientForm) {
        if (clientForm == null) {
            throw new ApiException("Client form cannot be null");
        }
        if (clientForm.getClientName() == null || clientForm.getClientName().trim().isEmpty()) {
            throw new ApiException("Client name is required");
        }
    }

    // ========== CUSTOM METHODS ==========

    public void toggleStatus(Integer id) {
        validateId(id);
        ((org.example.api.ClientApi) api).toggleStatus(id);
    }

    public void toggleStatusByName(String name) {
        if (Objects.isNull(name) || name.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        ((org.example.api.ClientApi) api).toggleStatusByName(StringUtil.normalise(name));
    }

    public ClientData getByNameOrId(Integer id, String name) {
                    name = StringUtil.normalise(name);
        if (Objects.nonNull(id) && Objects.nonNull(name)) {
            try {
                ClientPojo idPojo = api.get(id);
                ClientPojo namePojo = ((org.example.api.ClientApi) api).getByName(name);
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
            return convertEntityToData(((org.example.api.ClientApi) api).getByName(name));
        }
    }

    public List<ClientData> getByNameLike(String name) {
        return getByFieldLike("clientName", name);
    }

    public PaginationResponse<ClientData> getByNameLikePaginated(String name, PaginationRequest request) {
        return getByFieldLikePaginated("clientName", name, request);
    }

    public TsvUploadResult uploadClientsFromTsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("File cannot be null or empty");
        }

        FileValidationUtil.validateTsvFile(file);

        try {
            TsvUploadResult result = ClientTsvParser.parseWithDuplicateDetection(file.getInputStream());
            
            // Process the parsed forms
            List<ClientForm> forms = result.getParsedForms();
            if (forms != null) {
                for (ClientForm form : forms) {
                    try {
                        add(form);
                        result.incrementSuccessful();
                    } catch (Exception e) {
                        result.addError("Failed to add client '" + form.getClientName() + "': " + e.getMessage());
                        result.incrementFailed();
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            throw new ApiException("Failed to process TSV file: " + e.getMessage());
        }
    }

    public void toggleStatus(Integer id, String name) {
        if (Objects.nonNull(id)) {
            toggleStatus(id);
        } else if (Objects.nonNull(name) && !name.trim().isEmpty()) {
            toggleStatusByName(name);
        } else {
            throw new ApiException("Either ID or name must be provided");
        }
    }
}
