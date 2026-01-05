package com.nttdata.dataspace.edc.fc.catalog.directory;

import com.nttdata.dataspace.edc.fc.store.sql.ParticipantStore;

import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class CatalogNodeResolverExtension implements ServiceExtension {
    @Inject
    private Monitor monitor;
    
    @Inject
    private ParticipantStore participantStore;

    private TargetNodeDirectory nodeDirectory;

    @Override
    public String name() {
        // Object-oriented way at runtime (allowed here, not in annotation)
        return CatalogNodeResolverExtension.class.getSimpleName();
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor.info(name() + ": Initializing with database-backed participant loading");
    }

    @Provider
    public TargetNodeDirectory federatedCacheNodeDirectory() {
        if (nodeDirectory == null) {
            monitor.info("Creating DbCatalogNodeDirectory");
            nodeDirectory = new DbCatalogNodeDirectory(participantStore, monitor);
        }
        return nodeDirectory;
    }
}
