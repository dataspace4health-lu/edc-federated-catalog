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

package org.eclipse.edc.extensions.federatedcatalog.store.sql.schema.postgres;

import org.eclipse.edc.extensions.federatedcatalog.store.sql.schema.ParticipantStatements;

import static java.lang.String.format;

/**
 * Postgres-specific SQL statements for Participant store.
 */
public class PostgresDialectStatements implements ParticipantStatements {
    
    @Override
    public String getInsertTemplate() {
        return format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
                getParticipantTable(),
                getIdColumn(),
                getNameColumn(),
                getEndpointColumn(),
                getSupportedProtocolsColumn(),
                getCreatedAtColumn(),
                getUpdatedAtColumn());
    }
    
    @Override
    public String getUpdateTemplate() {
        return format("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ?",
                getParticipantTable(),
                getNameColumn(),
                getEndpointColumn(),
                getSupportedProtocolsColumn(),
                getUpdatedAtColumn(),
                getIdColumn());
    }
    
    @Override
    public String getFindByIdTemplate() {
        return format("SELECT * FROM %s WHERE %s = ?",
                getParticipantTable(),
                getIdColumn());
    }
    
    @Override
    public String getFindAllTemplate() {
        return format("SELECT * FROM %s ORDER BY %s",
                getParticipantTable(),
                getNameColumn());
    }
    
    @Override
    public String getDeleteTemplate() {
        return format("DELETE FROM %s WHERE %s = ?",
                getParticipantTable(),
                getIdColumn());
    }
    
    @Override
    public String getCountTemplate() {
        return format("SELECT COUNT(*) FROM %s WHERE %s = ?",
                getParticipantTable(),
                getIdColumn());
    }
}
