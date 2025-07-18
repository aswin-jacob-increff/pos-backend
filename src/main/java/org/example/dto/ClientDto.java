package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.form.ClientForm;
import org.example.model.data.ClientData;
import org.example.model.data.TsvUploadResult;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
    @org.springframework.transaction.annotation.Transactional
    public ClientData update(Integer id, @Valid ClientForm form) {
        validateId(id);
        preprocess(form);
        ClientPojo entity = convertFormToEntity(form);
        // Use the flow instead of the API to ensure products are updated
        flow.update(id, entity);
        return convertEntityToData(api.get(id));
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

    public TsvUploadResult uploadClientsFromTsv(MultipartFile file) {
        System.out.println("ClientDto.uploadClientsFromTsv - Starting");
        // Validate file
        FileValidationUtil.validateTsvFile(file);
        System.out.println("ClientDto.uploadClientsFromTsv - File validation passed");
        
        TsvUploadResult result;
        try {
            System.out.println("ClientDto.uploadClientsFromTsv - Starting parse with complete validation");
            result = ClientTsvParser.parseWithCompleteValidation(file.getInputStream(), (ClientApi) api);
            System.out.println("ClientDto.uploadClientsFromTsv - Parse completed. Total: " + result.getTotalRows() + ", Successful: " + result.getSuccessfulRows() + ", Failed: " + result.getFailedRows());
        } catch (Exception e) {
            System.out.println("ClientDto.uploadClientsFromTsv - Parse failed: " + e.getMessage());
            e.printStackTrace();
            result = new TsvUploadResult();
            result.addError("Failed to parse file: " + e.getMessage());
            return result;
        }
        
        // Check if we have any forms to process
        if (result.getSuccessfulRows() == 0) {
            System.out.println("ClientDto.uploadClientsFromTsv - No successful rows to process");
            return result;
        }
        
        // Validate file size
        try {
            FileValidationUtil.validateFileSize(result.getSuccessfulRows());
            System.out.println("ClientDto.uploadClientsFromTsv - File size validation passed");
        } catch (ApiException e) {
            System.out.println("ClientDto.uploadClientsFromTsv - File size validation failed: " + e.getMessage());
            result.addError("File size validation failed: " + e.getMessage());
            return result;
        }
        
        // Get the parsed forms from the result
        List<ClientForm> forms = result.getParsedForms();
        if (forms == null || forms.isEmpty()) {
            System.out.println("ClientDto.uploadClientsFromTsv - No valid forms found to process");
            result.addError("No valid forms found to process");
            return result;
        }
        
        System.out.println("ClientDto.uploadClientsFromTsv - Processing " + forms.size() + " forms");
        
        // Reset counters for actual processing
        result.setSuccessfulRows(0);
        
        // Process only the valid forms (already validated by parser)
        for (ClientForm form : forms) {
            try {
                System.out.println("ClientDto.uploadClientsFromTsv - Adding client: " + form.getClientName());
                // Use the flow directly since validation is already done
                ClientPojo entity = convertFormToEntity(form);
                flow.add(entity);
                result.incrementSuccessful();
                System.out.println("ClientDto.uploadClientsFromTsv - Successfully added client: " + form.getClientName());
            } catch (Exception e) {
                System.out.println("ClientDto.uploadClientsFromTsv - Unexpected error adding client '" + form.getClientName() + "': " + e.getMessage());
                result.addError("Unexpected error adding client '" + form.getClientName() + "': " + e.getMessage());
                result.incrementFailed();
            }
        }
        
        System.out.println("ClientDto.uploadClientsFromTsv - Final result: " + result.getSummary());
        return result;
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
