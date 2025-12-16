package com.nttdata.dataspace.federatedcatalog.jersey;

import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import com.nttdata.dataspace.federatedcatalog.jersey.validation.ResourceInterceptorBinder;
import com.nttdata.dataspace.federatedcatalog.jersey.validation.ResourceInterceptorProvider;
import org.eclipse.edc.web.spi.WebServer;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.validation.InterceptorFunctionRegistry;

import java.util.*;

@Provides(WebService.class)
public class JerseyExtension implements ServiceExtension {
    private JerseyRestService jerseyRestService;

    private static final String DEFAULT_PARTICIPANT_FILE_PATH = "/etc/config/participants.json";
    
    @Setting
    public static final String CORS_CONFIG_ORIGINS_SETTING = "edc.web.rest.cors.origins";
    @Setting
    public static final String CORS_CONFIG_ENABLED_SETTING = "edc.web.rest.cors.enabled";
    @Setting
    public static final String CORS_CONFIG_HEADERS_SETTING = "edc.web.rest.cors.headers";
    @Setting
    public static final String CORS_CONFIG_METHODS_SETTING = "edc.web.rest.cors.methods";
    @Setting
    private static final String SETTING_PARTICIPANT_FILE_PATH = "edc.federated.catalog.cors.participant.file.path";

    @Inject
    private WebServer webServer;

    @Inject
    private TypeManager typeManager;

    @Inject
    private Monitor monitor;

    private ResourceInterceptorProvider provider;

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor.info("Initializing Jersey extension.");
        
        var allowedOrigins = context.getSetting(CORS_CONFIG_ORIGINS_SETTING, "*");

        // Getting participants and inserting those into the allowedOrigins
        var participantFilePath = context.getSetting(SETTING_PARTICIPANT_FILE_PATH, DEFAULT_PARTICIPANT_FILE_PATH);
        Set<String> participantOrigins = Collections.emptySet();
        if (participantFilePath != null && !participantFilePath.isEmpty()) {
            var loader = new OriginsLoader(typeManager, monitor, participantFilePath);
            participantOrigins = loader.loadOrigins();
        }

        Set<String> mergedOrigins = new LinkedHashSet<>();
        if (!participantOrigins.isEmpty()) {
            Arrays.stream(allowedOrigins.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty() && !s.equals("*"))
              .map(String::toLowerCase)
              .forEach(mergedOrigins::add);

            participantOrigins.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .forEach(mergedOrigins::add);
        }

        var configuration = JerseyConfiguration.Builder.newInstance()
            .allowedHeaders(context.getSetting(CORS_CONFIG_HEADERS_SETTING, "origin, content-type, accept, authorization"))
            .allowedOrigins(String.join(",", mergedOrigins))
            .allowedMethods(context.getSetting(CORS_CONFIG_METHODS_SETTING, "GET, POST, DELETE, PUT, OPTIONS"))
            .corsEnabled(context.getSetting(CORS_CONFIG_ENABLED_SETTING, false))
            .build();

        jerseyRestService = new JerseyRestService(webServer, typeManager, configuration, monitor);

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
}