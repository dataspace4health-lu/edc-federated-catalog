package org.eclipse.edc.extensions.federatedcatalog.noderesolver;

import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.extensions.federatedcatalog.store.sql.ParticipantStore;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FederatedCatalogNodeResolverExtensionTest {

    private FederatedCatalogNodeResolverExtension extension;
    private ServiceExtensionContext context;
    private Monitor monitor;
    private ParticipantStore participantStore;

    @BeforeEach
    void setUp() {
        extension = new FederatedCatalogNodeResolverExtension();
        context = mock(ServiceExtensionContext.class);
        monitor = mock(Monitor.class);
        participantStore = mock(ParticipantStore.class);
        
        // Use reflection to inject dependencies since @Inject is not available in tests
        try {
            var monitorField = FederatedCatalogNodeResolverExtension.class.getDeclaredField("monitor");
            monitorField.setAccessible(true);
            monitorField.set(extension, monitor);
            
            var storeField = FederatedCatalogNodeResolverExtension.class.getDeclaredField("participantStore");
            storeField.setAccessible(true);
            storeField.set(extension, participantStore);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void name_shouldReturnExtensionName() {
        var extension = new FederatedCatalogNodeResolverExtension();
        // assert against runtime class simple name (keeps test aligned with object-oriented approach)
        assertThat(extension.name()).isEqualTo(FederatedCatalogNodeResolverExtension.class.getSimpleName());
    }

    @Test
    void initialize_shouldLogInitializationMessage() {
        extension.initialize(context);
        
        verify(monitor).info(contains("Initializing with database-backed participant loading"));
    }

    @Test
    void federatedCacheNodeDirectory_shouldCreateAndReturnDirectory() {
        TargetNodeDirectory directory = extension.federatedCacheNodeDirectory();
        
        assertThat(directory).isNotNull();
        assertThat(directory).isInstanceOf(DatabaseCatalogNodeDirectory.class);
        verify(monitor).info(contains("Creating DatabaseCatalogNodeDirectory"));
    }

    @Test
    void federatedCacheNodeDirectory_shouldReturnSameInstance_whenCalledMultipleTimes() {
        TargetNodeDirectory directory1 = extension.federatedCacheNodeDirectory();
        TargetNodeDirectory directory2 = extension.federatedCacheNodeDirectory();
        
        assertThat(directory1).isSameAs(directory2);
        verify(monitor, times(1)).info(contains("Creating DatabaseCatalogNodeDirectory"));
    }
}
