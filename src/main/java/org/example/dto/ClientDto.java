package org.example.dto;

import org.example.exception.ApiException;
import org.example.model.data.TsvUploadResult;
import org.example.model.form.ClientForm;
import org.example.model.data.ClientData;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
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

    // Custom methods that don't fit the generic pattern

    public void toggleStatus(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("Client ID cannot be null");
        }
        ((org.example.api.ClientApi) api).toggleStatus(id);
    }

    public void toggleStatusByName(String name) {
        if (Objects.isNull(name) || name.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        ((org.example.api.ClientApi) api).toggleStatusByName(StringUtil.format(name));
    }

    public ClientData getByNameOrId(Integer id, String name) {
        name = StringUtil.format(name);
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
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Client name cannot be null or empty");
        }
        List<ClientPojo> clients = ((org.example.api.ClientApi) api).getByNameLike(name);
        return clients.stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
    }



    public PaginationResponse<ClientData> getByNameLikePaginated(String name, PaginationRequest request) {
        PaginationResponse<ClientPojo> paginatedEntities = ((org.example.api.ClientApi) api).getByNameLikePaginated(name, request);
        
        List<ClientData> dataList = paginatedEntities.getContent().stream()
                .map(this::convertEntityToData)
                .collect(Collectors.toList());
        
        return new PaginationResponse<>(
            dataList,
            paginatedEntities.getTotalElements(),
            paginatedEntities.getCurrentPage(),
            paginatedEntities.getPageSize()
        );
    }

    public TsvUploadResult uploadClientsFromTsv(MultipartFile file) {
        System.out.println("ClientDto.uploadClientsFromTsv - Starting");
        // Validate file
        FileValidationUtil.validateTsvFile(file);
        System.out.println("ClientDto.uploadClientsFromTsv - File validation passed");
        
        TsvUploadResult result;
        try {
            System.out.println("ClientDto.uploadClientsFromTsv - Starting parse with complete validation");
            result = ClientTsvParser.parseWithCompleteValidation(file.getInputStream(), (org.example.api.ClientApi) api);
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
                api.add(entity);
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
        if (Objects.isNull(id) && Objects.isNull(name)) {
            throw new ApiException("Either ID or name must be provided for status toggle");
        }
        
        if (Objects.nonNull(id)) {
            // Use ID-based toggle
            ((org.example.api.ClientApi) api).toggleStatus(id);
        } else if (Objects.nonNull(name) && !name.trim().isEmpty()) {
            // Use name-based toggle
            ((org.example.api.ClientApi) api).toggleStatusByName(StringUtil.format(name));
        } else {
            throw new ApiException("Valid ID or name must be provided for status toggle");
        }
    }
}
