package org.example.api;

import org.springframework.transaction.annotation.Transactional;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.dao.ClientDao;
import org.example.pojo.ClientPojo;
import org.example.util.StringUtil;
import java.util.Objects;
import java.util.List;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;

@Service
@Transactional
public class ClientApi extends AbstractApi<ClientPojo> {

    @Autowired
    private ProductApi productApi;

    public ClientApi() {
        super(ClientPojo.class);
    }

    public ClientPojo getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        // Format the name consistently with how it's stored
        String formattedName = StringUtil.format(name);
        return findByField("clientName", formattedName);
    }

    public List<ClientPojo> getByNameLike(String name) {
        return getByFieldLikeWithValidation("clientName", name, "Client name");
    }

    public void toggleStatus(Integer id) {
        ClientPojo client = get(id);
        
        // Check if we're trying to set status to false (inactive)
        if (client.getStatus()) {
            // Client is currently active, check if it has products before deactivating
            if (productApi.hasProductsByClientId(client.getId())) {
                throw new ApiException("Client status toggle failed. Client has products.");
            }
        }
        
        ((ClientDao) dao).toggleStatus(id);
    }

    public void toggleStatusByName(String name) {
        ClientPojo client = getByName(name);
        if (client == null) {
            throw new ApiException("Client with name '" + name + "' not found");
        }
        
        // Check if we're trying to set status to false (inactive)
        if (client.getStatus()) {
            // Client is currently active, check if it has products before deactivating
            if (productApi.hasProductsByClientId(client.getId())) {
                throw new ApiException("Client status toggle failed. Client has products.");
            }
        }
        
        ((ClientDao) dao).toggleStatusByName(name);
    }

    public PaginationResponse<ClientPojo> getByNameLikePaginated(String name, PaginationRequest request) {
        return getByFieldLikePaginatedWithValidation("clientName", name, request, "Client name");
    }
} 