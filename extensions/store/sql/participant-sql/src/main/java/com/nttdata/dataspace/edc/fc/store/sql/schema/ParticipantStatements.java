/*
 *  Copyright (c) 2025
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Initial API and implementation
 *
 */

package com.nttdata.dataspace.edc.fc.store.sql.schema;

import org.eclipse.edc.sql.statement.SqlStatements;

/**
 * Defines all SQL statements needed for the Participant store.
 */
public interface ParticipantStatements extends SqlStatements {
    
    default String getParticipantTable() {
        return "edc_participant";
    }
    
    default String getIdColumn() {
        return "participant_id";
    }
    
    default String getNameColumn() {
        return "name";
    }
    
    default String getEndpointColumn() {
        return "endpoint";
    }
    
    default String getSupportedProtocolsColumn() {
        return "supported_protocols";
    }
    
    default String getCreatedAtColumn() {
        return "created_at";
    }
    
    default String getUpdatedAtColumn() {
        return "updated_at";
    }
    
    String getInsertTemplate();
    
    String getUpdateTemplate();
    
    String getFindByIdTemplate();
    
    String getFindAllTemplate();
    
    String getDeleteTemplate();
    
    String getCountTemplate();
}
