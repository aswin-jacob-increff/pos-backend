package org.example.util;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.query.Query;

import java.io.Serializable;

public class CentralizedIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        String entityName = object.getClass().getSimpleName();
        
        // Create centralized sequence table if it doesn't exist
        createSequenceTableIfNotExists(session);
        
        // Get next ID for this entity
        return getNextId(session, entityName);
    }
    
    private void createSequenceTableIfNotExists(SharedSessionContractImplementor session) {
        try {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS entity_sequences (" +
                "entity_name VARCHAR(100) PRIMARY KEY, " +
                "next_id INT NOT NULL DEFAULT 1)";
            session.createNativeQuery(createTableSQL).executeUpdate();
        } catch (Exception e) {
            // Table might already exist, continue
        }
    }
    
    private Integer getNextId(SharedSessionContractImplementor session, String entityName) {
        // Insert entity if it doesn't exist
        String insertSQL = "INSERT IGNORE INTO entity_sequences (entity_name, next_id) VALUES (?, 1)";
        session.createNativeQuery(insertSQL)
               .setParameter(1, entityName)
               .executeUpdate();
        
        // Update and get the next ID atomically
        String updateSQL = "UPDATE entity_sequences SET next_id = LAST_INSERT_ID(next_id + 1) WHERE entity_name = ?";
        session.createNativeQuery(updateSQL)
               .setParameter(1, entityName)
               .executeUpdate();
        
        String selectSQL = "SELECT LAST_INSERT_ID()";
        Query<Long> query = session.createNativeQuery(selectSQL, Long.class);
        Long result = query.uniqueResult();
        
        return result != null ? result.intValue() : 1;
    }
} 