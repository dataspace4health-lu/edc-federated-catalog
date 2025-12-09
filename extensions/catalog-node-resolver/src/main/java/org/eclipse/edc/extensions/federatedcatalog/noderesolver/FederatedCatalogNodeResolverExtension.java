package org.eclipse.edc.extensions.federatedcatalog.noderesolver;

import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.ParticipantStore;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class FederatedCatalogNodeResolverExtension implements ServiceExtension {
    @Inject
    private Monitor monitor;
    
    @Inject
    private ParticipantStore participantStore;

    private TargetNodeDirectory nodeDirectory;

    @Override
    public String name() {
        // Object-oriented way at runtime (allowed here, not in annotation)
        return FederatedCatalogNodeResolverExtension.class.getSimpleName();
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor.info(name() + ": Initializing with database-backed participant loading");
    }

    @Provider
    public TargetNodeDirectory federatedCacheNodeDirectory() {
        if (nodeDirectory == null) {
            monitor.info("Creating DatabaseCatalogNodeDirectory");
            nodeDirectory = new DatabaseCatalogNodeDirectory(participantStore, monitor);
        }
        return nodeDirectory;
    }
}
