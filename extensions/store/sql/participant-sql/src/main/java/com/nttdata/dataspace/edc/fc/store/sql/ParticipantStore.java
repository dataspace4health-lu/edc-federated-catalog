package com.nttdata.dataspace.edc.fc.store.sql;

import org.eclipse.edc.spi.result.StoreResult;

import java.util.stream.Stream;

/**
 * Store for managing participant entities.
 */
public interface ParticipantStore {
    
    /**
     * Find a participant by its ID.
     *
     * @param id the participant ID
     * @return the participant if found, null otherwise
     */
    Participant findById(String id);
    
    /**
     * Find all participants.
     *
     * @return stream of all participants
     */
    Stream<Participant> findAll();
    
    /**
     * Save a participant. If a participant with the same ID already exists, returns an error.
     *
     * @param participant the participant to save
     * @return success or error result
     */
    StoreResult<Void> save(Participant participant);
    
    /**
     * Update an existing participant.
     *
     * @param participant the participant to update
     * @return success or error result
     */
    StoreResult<Void> update(Participant participant);
    
    /**
     * Delete a participant by its ID.
     *
     * @param id the participant ID
     * @return the deleted participant or error result
     */
    StoreResult<Participant> delete(String id);
}
