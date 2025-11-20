--
--  Copyright (c) 2025
--
--  This program and the accompanying materials are made available under the
--  terms of the Apache License, Version 2.0 which is available at
--  https://www.apache.org/licenses/LICENSE-2.0
--
--  SPDX-License-Identifier: Apache-2.0
--
--  Contributors:
--       Initial SQL Schema for Participant Store
--

-- table: edc_participant
-- only intended for and tested with Postgres!
CREATE TABLE IF NOT EXISTS edc_participant
(
    participant_id       VARCHAR(255) PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL,
    endpoint             VARCHAR(500) NOT NULL,
    supported_protocols  TEXT,
    created_at           BIGINT       NOT NULL,
    updated_at           BIGINT       NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_participant_id ON edc_participant(participant_id);
