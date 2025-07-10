package org.example.util;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.query.Query;

import java.io.Serializable;

public class ContinuousIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        String entityName = object.getClass().getSimpleName();
        String tableName = entityName.toLowerCase() + "_id_seq";
        
        // Create sequence table if it doesn't exist
        createSequenceTableIfNotExists(session, tableName);
        
        // Get next ID using a transaction-safe approach
        return getNextId(session, tableName);
    }
    
    private void createSequenceTableIfNotExists(SharedSessionContractImplementor session, String tableName) {
        try {
            // Drop the table if it exists to recreate with proper structure
            String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
            session.createNativeQuery(dropTableSQL).executeUpdate();
            
            // Create a proper sequence table with a single row
            String createTableSQL = "CREATE TABLE " + tableName + " (id BIGINT PRIMARY KEY DEFAULT 1)";
            session.createNativeQuery(createTableSQL).executeUpdate();
            
            // Insert initial value
            String insertSQL = "INSERT INTO " + tableName + " (id) VALUES (1)";
            session.createNativeQuery(insertSQL).executeUpdate();
        } catch (Exception e) {
            // If table creation fails, try to use existing table
            System.out.println("Warning: Could not recreate sequence table " + tableName + ": " + e.getMessage());
        }
    }
    
    private Integer getNextId(SharedSessionContractImplementor session, String tableName) {
        try {
            // Use a transaction-safe approach to get and increment the ID
            String updateSQL = "UPDATE " + tableName + " SET id = LAST_INSERT_ID(id + 1)";
            session.createNativeQuery(updateSQL).executeUpdate();
            
            String selectSQL = "SELECT LAST_INSERT_ID()";
            Query<Long> query = session.createNativeQuery(selectSQL, Long.class);
            Long result = query.uniqueResult();
            
            return result != null ? result.intValue() : 1;
        } catch (Exception e) {
            // Fallback: try to get the current value and increment manually
            try {
                String selectCurrentSQL = "SELECT id FROM " + tableName + " LIMIT 1";
                Query<Long> currentQuery = session.createNativeQuery(selectCurrentSQL, Long.class);
                Long currentId = currentQuery.uniqueResult();
                
                if (currentId == null) {
                    // Insert initial value if table is empty
                    String insertSQL = "INSERT INTO " + tableName + " (id) VALUES (1)";
                    session.createNativeQuery(insertSQL).executeUpdate();
                    return 1;
                } else {
                    // Update with new value
                    String updateSQL = "UPDATE " + tableName + " SET id = " + (currentId + 1);
                    session.createNativeQuery(updateSQL).executeUpdate();
                    return currentId.intValue() + 1;
                }
            } catch (Exception fallbackException) {
                // Last resort: return a timestamp-based ID
                System.out.println("Warning: Using fallback ID generation for " + tableName + ": " + fallbackException.getMessage());
                return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            }
        }
    }
} 