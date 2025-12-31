package com.nttdata.dataspace.edc.fc.store.sql.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParticipantStatements interface default methods.
 */
class ParticipantStatementsTest {

    // Create a concrete implementation to test default methods
    private final ParticipantStatements statements = new ParticipantStatements() {
        @Override
        public String getInsertTemplate() {
            return "INSERT";
        }

        @Override
        public String getUpdateTemplate() {
            return "UPDATE";
        }

        @Override
        public String getFindByIdTemplate() {
            return "FIND_BY_ID";
        }

        @Override
        public String getFindAllTemplate() {
            return "FIND_ALL";
        }

        @Override
        public String getDeleteTemplate() {
            return "DELETE";
        }

        @Override
        public String getCountTemplate() {
            return "COUNT";
        }
    };

    @Test
    void getParticipantTable_shouldReturnDefaultTableName() {
        assertEquals("edc_participant", statements.getParticipantTable());
    }

    @Test
    void getIdColumn_shouldReturnDefaultIdColumnName() {
        assertEquals("participant_id", statements.getIdColumn());
    }

    @Test
    void getNameColumn_shouldReturnDefaultNameColumnName() {
        assertEquals("name", statements.getNameColumn());
    }

    @Test
    void getEndpointColumn_shouldReturnDefaultEndpointColumnName() {
        assertEquals("endpoint", statements.getEndpointColumn());
    }

    @Test
    void getSupportedProtocolsColumn_shouldReturnDefaultProtocolsColumnName() {
        assertEquals("supported_protocols", statements.getSupportedProtocolsColumn());
    }

    @Test
    void getCreatedAtColumn_shouldReturnDefaultCreatedAtColumnName() {
        assertEquals("created_at", statements.getCreatedAtColumn());
    }

    @Test
    void getUpdatedAtColumn_shouldReturnDefaultUpdatedAtColumnName() {
        assertEquals("updated_at", statements.getUpdatedAtColumn());
    }
}
