package com.nttdata.dataspace.edc.fc.web.jersey;

import com.nttdata.dataspace.edc.fc.store.sql.ParticipantStore;
import com.nttdata.dataspace.edc.fc.web.jersey.validation.ResourceInterceptorBinder;
import com.nttdata.dataspace.edc.fc.web.jersey.validation.ResourceInterceptorProvider;

import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

import org.eclipse.edc.web.spi.WebServer;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.validation.InterceptorFunctionRegistry;

@Provides(WebService.class)
public class JerseyExtension implements ServiceExtension {
    private JerseyRestService jerseyRestService;
    private JerseyConfiguration jerseyConfiguration;

    @Setting
    public static final String CORS_CONFIG_ORIGINS_SETTING = "edc.web.rest.cors.origins";
    @Setting
    public static final String CORS_CONFIG_ENABLED_SETTING = "edc.web.rest.cors.enabled";
    @Setting
    public static final String CORS_CONFIG_HEADERS_SETTING = "edc.web.rest.cors.headers";
    @Setting
    public static final String CORS_CONFIG_METHODS_SETTING = "edc.web.rest.cors.methods";

    @Inject
    private WebServer webServer;

    @Inject
    private TypeManager typeManager;

    @Inject
    private Monitor monitor;

    @Inject
    private ParticipantStore participantStore;

    private ResourceInterceptorProvider provider;

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor.info("Initializing Jersey extension.");

        jerseyConfiguration = JerseyConfiguration.Builder.newInstance()
            .allowedHeaders(context.getSetting(CORS_CONFIG_HEADERS_SETTING, "origin, content-type, accept, authorization"))
            .allowedOrigins(context.getSetting(CORS_CONFIG_ORIGINS_SETTING, "*"))
            .allowedMethods(context.getSetting(CORS_CONFIG_METHODS_SETTING, "GET, POST, DELETE, PUT, OPTIONS"))
            .corsEnabled(context.getSetting(CORS_CONFIG_ENABLED_SETTING, false))
            .build();

        jerseyRestService = new JerseyRestService(webServer, typeManager, jerseyConfiguration, monitor, participantStore);

        provider = new ResourceInterceptorProvider();
        jerseyRestService.registerInstance(() -> new ResourceInterceptorBinder(provider));
        
        context.registerService(WebService.class, jerseyRestService);
    }

    @Override
    public void start() {
        jerseyRestService.start();
    }

    @Provider
    public InterceptorFunctionRegistry createInterceptorFunctionRegistry() {
        return provider;
    }

    /**
     * Exposes the {@link JerseyConfiguration} as an EDC service so other extensions
     * (e.g., the participant API) can call {@link JerseyConfiguration#setParticipantOrigins}
     * to keep the CORS allowlist in sync with the participant registry.
     */
    @Provider
    public JerseyConfiguration jerseyConfiguration() {
        return jerseyConfiguration;
    }
}