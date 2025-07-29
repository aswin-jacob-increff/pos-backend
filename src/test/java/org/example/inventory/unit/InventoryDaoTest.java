package org.example.inventory.unit;

import org.example.dao.InventoryDao;
import org.example.pojo.InventoryPojo;
import org.example.pojo.ProductPojo;
import org.example.dao.ProductDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryDaoTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<InventoryPojo> criteriaQuery;

    @Mock
    private Root<InventoryPojo> root;

    @InjectMocks
    private InventoryDao inventoryDao;

    private InventoryPojo testInventory;

    @BeforeEach
    void setUp() {
        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1);
        testInventory.setQuantity(10);
    }

    @Test
    void testGetByProductId_Success() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(InventoryPojo.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(InventoryPojo.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(mock(jakarta.persistence.TypedQuery.class));
        when(entityManager.createQuery(criteriaQuery).getResultList()).thenReturn(Arrays.asList(testInventory));

        // Act
        InventoryPojo result = inventoryDao.getByProductId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
    }

    @Test
    void testGetByProductId_NotFound() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(InventoryPojo.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(InventoryPojo.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(mock(jakarta.persistence.TypedQuery.class));
        when(entityManager.createQuery(criteriaQuery).getResultList()).thenReturn(Arrays.asList());

        // Act
        InventoryPojo result = inventoryDao.getByProductId(1);

        // Assert
        assertNull(result);
    }

    @Test
    void testInsert_Success() {
        // Act
        inventoryDao.insert(testInventory);

        // Assert
        verify(entityManager).persist(testInventory);
    }

    @Test
    void testSelect_Success() {
        // Arrange
        when(entityManager.find(InventoryPojo.class, 1)).thenReturn(testInventory);

        // Act
        InventoryPojo result = inventoryDao.select(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
        verify(entityManager).find(InventoryPojo.class, 1);
    }

    @Test
    void testSelect_NotFound() {
        // Arrange
        when(entityManager.find(InventoryPojo.class, 1)).thenReturn(null);

        // Act
        InventoryPojo result = inventoryDao.select(1);

        // Assert
        assertNull(result);
        verify(entityManager).find(InventoryPojo.class, 1);
    }

    @Test
    void testSelectAll_Success() {
        // Arrange
        InventoryPojo secondInventory = new InventoryPojo();
        secondInventory.setId(2);
        secondInventory.setProductId(2);
        secondInventory.setQuantity(20);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(InventoryPojo.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(InventoryPojo.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(mock(jakarta.persistence.TypedQuery.class));
        when(entityManager.createQuery(criteriaQuery).getResultList()).thenReturn(Arrays.asList(testInventory, secondInventory));

        // Act
        List<InventoryPojo> result = inventoryDao.selectAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
    }

    @Test
    void testSelectAll_Empty() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(InventoryPojo.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(InventoryPojo.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(mock(jakarta.persistence.TypedQuery.class));
        when(entityManager.createQuery(criteriaQuery).getResultList()).thenReturn(Arrays.asList());

        // Act
        List<InventoryPojo> result = inventoryDao.selectAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(entityManager.find(InventoryPojo.class, 1)).thenReturn(testInventory);

        // Act
        inventoryDao.update(1, testInventory);

        // Assert
        verify(entityManager).find(InventoryPojo.class, 1);
        verify(entityManager).merge(testInventory);
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(entityManager.find(InventoryPojo.class, 1)).thenReturn(null);

        // Act
        inventoryDao.update(1, testInventory);

        // Assert
        verify(entityManager).find(InventoryPojo.class, 1);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void testDelete_Success() {
        // Arrange
        when(entityManager.find(InventoryPojo.class, 1)).thenReturn(testInventory);

        // Act
        inventoryDao.delete(1);

        // Assert
        verify(entityManager).find(InventoryPojo.class, 1);
        verify(entityManager).remove(testInventory);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(entityManager.find(InventoryPojo.class, 1)).thenReturn(null);

        // Act
        inventoryDao.delete(1);

        // Assert
        verify(entityManager).find(InventoryPojo.class, 1);
        verify(entityManager, never()).remove(any());
    }

    @Test
    void testGetByParams_Success() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(InventoryPojo.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(InventoryPojo.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        lenient().when(criteriaQuery.where(any(jakarta.persistence.criteria.Predicate.class))).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(mock(jakarta.persistence.TypedQuery.class));
        when(entityManager.createQuery(criteriaQuery).getResultList()).thenReturn(Arrays.asList(testInventory));

        // Act
        List<InventoryPojo> result = inventoryDao.getByParams("productId", 1);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
    }

    @Test
    void testGetByParams_Empty() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(InventoryPojo.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(InventoryPojo.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        lenient().when(criteriaQuery.where(any(jakarta.persistence.criteria.Predicate.class))).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(mock(jakarta.persistence.TypedQuery.class));
        when(entityManager.createQuery(criteriaQuery).getResultList()).thenReturn(Arrays.asList());

        // Act
        List<InventoryPojo> result = inventoryDao.getByParams("productId", 1);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByParams_NullFieldName() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryDao.getByParams(null, 1);
        });
    }

    @Test
    void testGetByParams_NullValue() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryDao.getByParams("productId", null);
        });
    }
} 