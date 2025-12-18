package com.nttdata.dataspace.edc.fc.catalog.directory;

import com.nttdata.dataspace.edc.fc.store.sql.Participant;
import com.nttdata.dataspace.edc.fc.store.sql.ParticipantStore;

import java.util.List;

import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.spi.monitor.Monitor;

/**
 * Implementation of TargetNodeDirectory that loads participants from a database using ParticipantStore.
 * This replaces the file-based approach with a database-backed solution for better scalability and management.
 */
public class DbCatalogNodeDirectory implements TargetNodeDirectory {
    
    private final ParticipantStore participantStore;
    private final Monitor monitor;

    public DbCatalogNodeDirectory(ParticipantStore participantStore, Monitor monitor) {
        this.participantStore = participantStore;
        this.monitor = monitor;
    }

    @Override
    public List<TargetNode> getAll() {
        monitor.debug(DbCatalogNodeDirectory.class.getSimpleName() + ": Loading all participants from database");
        
        var participants = participantStore.findAll()
                .map(this::toTargetNode)
                .toList();
        
        monitor.info(DbCatalogNodeDirectory.class.getSimpleName() + ": Loaded " + participants.size() + " participants from database");
        return participants;
    }

    @Override
    public void insert(TargetNode targetNode) {
        monitor.debug(DbCatalogNodeDirectory.class.getSimpleName() + ": Inserting target node: " + targetNode.id());
        
        var participant = toParticipant(targetNode);
        var result = participantStore.save(participant);
        
        if (result.failed()) {
            monitor.warning(DbCatalogNodeDirectory.class.getSimpleName() + ": Failed to insert participant: " + result.getFailureDetail());
            throw new IllegalStateException("Failed to insert participant: " + result.getFailureDetail());
        } else {
            monitor.info(DbCatalogNodeDirectory.class.getSimpleName() + ": Successfully inserted participant: " + targetNode.id());
        }
    }

    @Override
    public TargetNode remove(String id) {
        monitor.debug(DbCatalogNodeDirectory.class.getSimpleName() + ": Removing target node: " + id);
        
        var result = participantStore.delete(id);
        
        if (result.succeeded()) {
            monitor.info(DbCatalogNodeDirectory.class.getSimpleName() + ": Successfully removed participant: " + id);
            return toTargetNode(result.getContent());
        } else {
            monitor.warning(DbCatalogNodeDirectory.class.getSimpleName() + ": Failed to remove participant: " + result.getFailureDetail());
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
