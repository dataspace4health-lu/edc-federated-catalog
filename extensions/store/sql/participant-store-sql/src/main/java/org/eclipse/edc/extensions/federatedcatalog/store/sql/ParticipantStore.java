package org.eclipse.edc.extensions.federatedcatalog.store.sql;

import org.eclipse.edc.extensions.federatedcatalog.store.sql.Participant;

import java.util.List;
import java.util.Optional;

/**
 * Store for managing participant entities.
 */
public interface ParticipantStore {
    
    Optional<Participant> findById(String id);
    
    List<Participant> findAll();
    
    void save(Participant participant);
    
    void delete(String id);
}