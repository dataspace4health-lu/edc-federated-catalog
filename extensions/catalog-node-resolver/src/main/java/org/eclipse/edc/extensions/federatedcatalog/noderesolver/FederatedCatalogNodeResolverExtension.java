package org.eclipse.edc.extensions.federatedcatalog.noderesolver;

import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

import java.io.File;

@Extension(value = FederatedCatalogNodeResolverExtension.NAME)
public class FederatedCatalogNodeResolverExtension implements ServiceExtension {
    public static final String NAME = "Federated Catalog Node Resolver Extension";
    public static final String PARTICIPANT_LIST_FILE_PATH = "participants.json";

    private File participantListFile;
    private TargetNodeDirectory nodeDirectory;

    @Inject
    private Monitor monitor;

    @Inject
    private TypeManager typeManager;


    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        participantListFile = new File(PARTICIPANT_LIST_FILE_PATH).getAbsoluteFile();
        if (!participantListFile.exists()) {
            monitor.warning("Path '%s' does not exist. It must be a resolvable path with read access. Will not add any VCs.".formatted(PARTICIPANT_LIST_FILE_PATH));
        }

        monitor.info(NAME + " initialized...");
    }

    @Provider
    public TargetNodeDirectory federatedCacheNodeDirectory() {
        if (nodeDirectory == null) {
            nodeDirectory = new CatalogNodeDirectory(typeManager.getMapper(), participantListFile, monitor);
        }
        return nodeDirectory;
    }
}
