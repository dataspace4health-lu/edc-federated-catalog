package org.eclipse.edc.extensions.federatedcatalog.noderesolver;

import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.Participant;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.List;

/**
 * Implementation of TargetNodeDirectory that loads participants from a database using ParticipantStore.
 * This replaces the file-based approach with a database-backed solution for better scalability and management.
 */
public class DatabaseCatalogNodeDirectory implements TargetNodeDirectory {
    
    private final ParticipantStore participantStore;
    private final Monitor monitor;

    public DatabaseCatalogNodeDirectory(ParticipantStore participantStore, Monitor monitor) {
        this.participantStore = participantStore;
        this.monitor = monitor;
    }

    @Override
    public List<TargetNode> getAll() {
        monitor.debug(DatabaseCatalogNodeDirectory.class.getSimpleName() + ": Loading all participants from database");
        
        var participants = participantStore.findAll()
                .map(this::toTargetNode)
                .toList();
        
        monitor.info(DatabaseCatalogNodeDirectory.class.getSimpleName() + ": Loaded " + participants.size() + " participants from database");
        return participants;
    }

    @Override
    public void insert(TargetNode targetNode) {
        monitor.debug(DatabaseCatalogNodeDirectory.class.getSimpleName() + ": Inserting target node: " + targetNode.id());
        
        var participant = toParticipant(targetNode);
        var result = participantStore.save(participant);
        
        if (result.failed()) {
            monitor.warning(DatabaseCatalogNodeDirectory.class.getSimpleName() + ": Failed to insert participant: " + result.getFailureDetail());
            throw new IllegalStateException("Failed to insert participant: " + result.getFailureDetail());
        } else {
            monitor.info(DatabaseCatalogNodeDirectory.class.getSimpleName() + ": Successfully inserted participant: " + targetNode.id());
        }
    }

    @Override
    public TargetNode remove(String id) {
        monitor.debug(DatabaseCatalogNodeDirectory.class.getSimpleName() + ": Removing target node: " + id);
        
        var result = participantStore.delete(id);
        
        if (result.succeeded()) {
            monitor.info(DatabaseCatalogNodeDirectory.class.getSimpleName() + ": Successfully removed participant: " + id);
            return toTargetNode(result.getContent());
        } else {
            monitor.warning(DatabaseCatalogNodeDirectory.class.getSimpleName() + ": Failed to remove participant: " + result.getFailureDetail());
            throw new IllegalStateException("Failed to remove participant: " + result.getFailureDetail());
        }
    }
    
    /**
     * Converts a Participant entity from the database to a TargetNode for the crawler.
     */
    private TargetNode toTargetNode(Participant participant) {
        return new TargetNode(
                participant.getName(),
                participant.getId(),
                participant.getEndpoint(),
                participant.getSupportedProtocols()
        );
    }
    
    /**
     * Converts a TargetNode to a Participant entity for database storage.
     */
    private Participant toParticipant(TargetNode targetNode) {
        long now = System.currentTimeMillis();
        return Participant.builder()
                .id(targetNode.id())
                .name(targetNode.name())
                .endpoint(targetNode.targetUrl())
                .supportedProtocols(targetNode.supportedProtocols())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
