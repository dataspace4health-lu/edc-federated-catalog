package com.nttdata.dataspace.edc.fc.store.sql.schema.postgres;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PostgresDialectStatements.
 */
class PostgresDialectStatementsTest {

    private PostgresDialectStatements statements;

    @BeforeEach
    void setUp() {
        statements = new PostgresDialectStatements();
    }

    @Test
    void getInsertTemplate_shouldReturnValidInsertStatement() {
        String insertTemplate = statements.getInsertTemplate();
        
        assertNotNull(insertTemplate);
        assertTrue(insertTemplate.startsWith("INSERT INTO edc_participant"));
        assertTrue(insertTemplate.contains("participant_id"));
        assertTrue(insertTemplate.contains("name"));
        assertTrue(insertTemplate.contains("endpoint"));
        assertTrue(insertTemplate.contains("supported_protocols"));
        assertTrue(insertTemplate.contains("created_at"));
        assertTrue(insertTemplate.contains("updated_at"));
        assertTrue(insertTemplate.contains("VALUES"));
        // Check for 6 placeholders
        assertEquals(6, insertTemplate.chars().filter(ch -> ch == '?').count());
    }

    @Test
    void getUpdateTemplate_shouldReturnValidUpdateStatement() {
        String updateTemplate = statements.getUpdateTemplate();
        
        assertNotNull(updateTemplate);
        assertTrue(updateTemplate.startsWith("UPDATE edc_participant"));
        assertTrue(updateTemplate.contains("SET"));
        assertTrue(updateTemplate.contains("name = ?"));
        assertTrue(updateTemplate.contains("endpoint = ?"));
        assertTrue(updateTemplate.contains("supported_protocols = ?"));
        assertTrue(updateTemplate.contains("updated_at = ?"));
        assertTrue(updateTemplate.contains("WHERE participant_id = ?"));
        // Check for 5 placeholders
        assertEquals(5, updateTemplate.chars().filter(ch -> ch == '?').count());
    }

    @Test
    void getFindByIdTemplate_shouldReturnValidSelectByIdStatement() {
        String findByIdTemplate = statements.getFindByIdTemplate();
        
        assertNotNull(findByIdTemplate);
        assertTrue(findByIdTemplate.startsWith("SELECT * FROM edc_participant"));
        assertTrue(findByIdTemplate.contains("WHERE participant_id = ?"));
        assertEquals(1, findByIdTemplate.chars().filter(ch -> ch == '?').count());
    }

    @Test
    void getFindAllTemplate_shouldReturnValidSelectAllStatement() {
        String findAllTemplate = statements.getFindAllTemplate();
        
        assertNotNull(findAllTemplate);
        assertTrue(findAllTemplate.startsWith("SELECT * FROM edc_participant"));
        assertTrue(findAllTemplate.contains("ORDER BY name"));
        // No placeholders for findAll
        assertEquals(0, findAllTemplate.chars().filter(ch -> ch == '?').count());
    }

    @Test
    void getDeleteTemplate_shouldReturnValidDeleteStatement() {
        String deleteTemplate = statements.getDeleteTemplate();
        
        assertNotNull(deleteTemplate);
        assertTrue(deleteTemplate.startsWith("DELETE FROM edc_participant"));
        assertTrue(deleteTemplate.contains("WHERE participant_id = ?"));
        assertEquals(1, deleteTemplate.chars().filter(ch -> ch == '?').count());
    }

    @Test
    void getCountTemplate_shouldReturnValidCountStatement() {
        String countTemplate = statements.getCountTemplate();
        
        assertNotNull(countTemplate);
        assertTrue(countTemplate.startsWith("SELECT COUNT(*) FROM edc_participant"));
        assertTrue(countTemplate.contains("WHERE participant_id = ?"));
        assertEquals(1, countTemplate.chars().filter(ch -> ch == '?').count());
    }

    @Test
    void shouldInheritDefaultColumnNames() {
        // Verify that default column names from ParticipantStatements are used
        assertEquals("edc_participant", statements.getParticipantTable());
        assertEquals("participant_id", statements.getIdColumn());
        assertEquals("name", statements.getNameColumn());
        assertEquals("endpoint", statements.getEndpointColumn());
        assertEquals("supported_protocols", statements.getSupportedProtocolsColumn());
        assertEquals("created_at", statements.getCreatedAtColumn());
        assertEquals("updated_at", statements.getUpdatedAtColumn());
    }

    @Test
    void insertTemplate_shouldContainAllColumns() {
        String insertTemplate = statements.getInsertTemplate();
        
        // Verify all column names are present in the correct order
        int idIndex = insertTemplate.indexOf("participant_id");
        int nameIndex = insertTemplate.indexOf("name");
        int endpointIndex = insertTemplate.indexOf("endpoint");
        int protocolsIndex = insertTemplate.indexOf("supported_protocols");
        int createdAtIndex = insertTemplate.indexOf("created_at");
        int updatedAtIndex = insertTemplate.indexOf("updated_at");
        
        assertTrue(idIndex < nameIndex, "participant_id should come before name");
        assertTrue(nameIndex < endpointIndex, "name should come before endpoint");
        assertTrue(endpointIndex < protocolsIndex, "endpoint should come before supported_protocols");
        assertTrue(protocolsIndex < createdAtIndex, "supported_protocols should come before created_at");
        assertTrue(createdAtIndex < updatedAtIndex, "created_at should come before updated_at");
    }

    @Test
    void updateTemplate_shouldNotIncludeIdOrCreatedAtInSetClause() {
        String updateTemplate = statements.getUpdateTemplate();
        
        // The SET clause should not update id or created_at
        String setClause = updateTemplate.substring(
            updateTemplate.indexOf("SET"),
            updateTemplate.indexOf("WHERE")
        );
        
        assertFalse(setClause.contains("participant_id"), "SET clause should not include participant_id");
        assertFalse(setClause.contains("created_at"), "SET clause should not include created_at");
    }
}
