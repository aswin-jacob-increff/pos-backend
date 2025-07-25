package org.example.dao;

import jakarta.transaction.Transactional;
import org.example.pojo.ClientPojo;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@Transactional
public class ClientDao extends AbstractDao<ClientPojo> {

    public ClientDao() {
        super(ClientPojo.class);
    }

    public ClientPojo selectByName(String name) {
        return selectByField("clientName", name);
    }

    public List<ClientPojo> selectByNameLike(String name) {
        return selectByFieldLike("clientName", name);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all clients with pagination support.
     */
    public PaginationResponse<ClientPojo> getAllPaginated(PaginationRequest request) {
        return selectAllPaginated(request);
    }

    /**
     * Get clients by name with pagination support.
     */
    public PaginationResponse<ClientPojo> selectByNamePaginated(String name, PaginationRequest request) {
        return selectByFieldPaginated("clientName", name, request);
    }

    /**
     * Get clients by name with partial matching and pagination support.
     */
    public PaginationResponse<ClientPojo> selectByNameLikePaginated(String name, PaginationRequest request) {
        return selectByFieldLikePaginated("clientName", name, request);
    }

    // ========== COUNT METHODS ==========

    /**
     * Count clients by name.
     */
    public long countByName(String name) {
        return countByField("clientName", name);
    }

    /**
     * Count clients by name with partial matching.
     */
    public long countByNameLike(String name) {
        return countByFieldLike("clientName", name);
    }

    @Override
    protected void updateEntity(ClientPojo existing, ClientPojo updated) {
        existing.setClientName(updated.getClientName());
        existing.setStatus(updated.getStatus());
    }

    public void toggleStatus(Integer id) {
        ClientPojo client = select(id);
        if (client != null) {
            client.setStatus(!client.getStatus());
            update(id, client);
        }
    }

    public void toggleStatusByName(String name) {
        ClientPojo client = selectByName(name);
        if (client != null) {
            client.setStatus(!client.getStatus());
            update(client.getId(), client);
        }
    }
}
