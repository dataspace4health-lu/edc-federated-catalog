package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the {@code libs} extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final EdcLibraryAccessors laccForEdcLibraryAccessors = new EdcLibraryAccessors(owner);
    private final JacksonLibraryAccessors laccForJacksonLibraryAccessors = new JacksonLibraryAccessors(owner);
    private final TestcontainersLibraryAccessors laccForTestcontainersLibraryAccessors = new TestcontainersLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

    /**
     * Dependency provider for <b>awaitility</b> with <b>org.awaitility:awaitility</b> coordinates and
     * with version reference <b>awaitility</b>
     * <p>
     * This dependency was declared in catalog libs.versions.toml
     */
    public Provider<MinimalExternalModuleDependency> getAwaitility() {
        return create("awaitility");
    }

    /**
     * Dependency provider for <b>postgres</b> with <b>org.postgresql:postgresql</b> coordinates and
     * with version reference <b>postgres</b>
     * <p>
     * This dependency was declared in catalog libs.versions.toml
     */
    public Provider<MinimalExternalModuleDependency> getPostgres() {
        return create("postgres");
    }

    /**
     * Dependency provider for <b>restAssured</b> with <b>io.rest-assured:rest-assured</b> coordinates and
     * with version reference <b>restAssured</b>
     * <p>
     * This dependency was declared in catalog libs.versions.toml
     */
    public Provider<MinimalExternalModuleDependency> getRestAssured() {
        return create("restAssured");
    }

    /**
     * Group of libraries at <b>edc</b>
     */
    public EdcLibraryAccessors getEdc() {
        return laccForEdcLibraryAccessors;
    }

    /**
     * Group of libraries at <b>jackson</b>
     */
    public JacksonLibraryAccessors getJackson() {
        return laccForJacksonLibraryAccessors;
    }

    /**
     * Group of libraries at <b>testcontainers</b>
     */
    public TestcontainersLibraryAccessors getTestcontainers() {
        return laccForTestcontainersLibraryAccessors;
    }

    /**
     * Group of versions at <b>versions</b>
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Group of bundles at <b>bundles</b>
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Group of plugins at <b>plugins</b>
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class EdcLibraryAccessors extends SubDependencyFactory {
        private final EdcApiLibraryAccessors laccForEdcApiLibraryAccessors = new EdcApiLibraryAccessors(owner);
        private final EdcConfigLibraryAccessors laccForEdcConfigLibraryAccessors = new EdcConfigLibraryAccessors(owner);
        private final EdcControlplaneLibraryAccessors laccForEdcControlplaneLibraryAccessors = new EdcControlplaneLibraryAccessors(owner);
        private final EdcCoreLibraryAccessors laccForEdcCoreLibraryAccessors = new EdcCoreLibraryAccessors(owner);
        private final EdcDcpLibraryAccessors laccForEdcDcpLibraryAccessors = new EdcDcpLibraryAccessors(owner);
        private final EdcDidLibraryAccessors laccForEdcDidLibraryAccessors = new EdcDidLibraryAccessors(owner);
        private final EdcDpfLibraryAccessors laccForEdcDpfLibraryAccessors = new EdcDpfLibraryAccessors(owner);
        private final EdcDspLibraryAccessors laccForEdcDspLibraryAccessors = new EdcDspLibraryAccessors(owner);
        private final EdcExtLibraryAccessors laccForEdcExtLibraryAccessors = new EdcExtLibraryAccessors(owner);
        private final EdcIamLibraryAccessors laccForEdcIamLibraryAccessors = new EdcIamLibraryAccessors(owner);
        private final EdcLibLibraryAccessors laccForEdcLibLibraryAccessors = new EdcLibLibraryAccessors(owner);
        private final EdcOauth2LibraryAccessors laccForEdcOauth2LibraryAccessors = new EdcOauth2LibraryAccessors(owner);
        private final EdcSpiLibraryAccessors laccForEdcSpiLibraryAccessors = new EdcSpiLibraryAccessors(owner);
        private final EdcSqlLibraryAccessors laccForEdcSqlLibraryAccessors = new EdcSqlLibraryAccessors(owner);

        public EdcLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>boot</b> with <b>org.eclipse.edc:boot</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getBoot() {
            return create("edc.boot");
        }

        /**
         * Dependency provider for <b>junit</b> with <b>org.eclipse.edc:junit</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJunit() {
            return create("edc.junit");
        }

        /**
         * Group of libraries at <b>edc.api</b>
         */
        public EdcApiLibraryAccessors getApi() {
            return laccForEdcApiLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.config</b>
         */
        public EdcConfigLibraryAccessors getConfig() {
            return laccForEdcConfigLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.controlplane</b>
         */
        public EdcControlplaneLibraryAccessors getControlplane() {
            return laccForEdcControlplaneLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.core</b>
         */
        public EdcCoreLibraryAccessors getCore() {
            return laccForEdcCoreLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.dcp</b>
         */
        public EdcDcpLibraryAccessors getDcp() {
            return laccForEdcDcpLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.did</b>
         */
        public EdcDidLibraryAccessors getDid() {
            return laccForEdcDidLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.dpf</b>
         */
        public EdcDpfLibraryAccessors getDpf() {
            return laccForEdcDpfLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.dsp</b>
         */
        public EdcDspLibraryAccessors getDsp() {
            return laccForEdcDspLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.ext</b>
         */
        public EdcExtLibraryAccessors getExt() {
            return laccForEdcExtLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.iam</b>
         */
        public EdcIamLibraryAccessors getIam() {
            return laccForEdcIamLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.lib</b>
         */
        public EdcLibLibraryAccessors getLib() {
            return laccForEdcLibLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.oauth2</b>
         */
        public EdcOauth2LibraryAccessors getOauth2() {
            return laccForEdcOauth2LibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.spi</b>
         */
        public EdcSpiLibraryAccessors getSpi() {
            return laccForEdcSpiLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.sql</b>
         */
        public EdcSqlLibraryAccessors getSql() {
            return laccForEdcSqlLibraryAccessors;
        }

    }

    public static class EdcApiLibraryAccessors extends SubDependencyFactory {
        private final EdcApiControlLibraryAccessors laccForEdcApiControlLibraryAccessors = new EdcApiControlLibraryAccessors(owner);

        public EdcApiLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>management</b> with <b>org.eclipse.edc:management-api</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getManagement() {
            return create("edc.api.management");
        }

        /**
         * Dependency provider for <b>observability</b> with <b>org.eclipse.edc:api-observability</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getObservability() {
            return create("edc.api.observability");
        }

        /**
         * Dependency provider for <b>version</b> with <b>org.eclipse.edc:version-api</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getVersion() {
            return create("edc.api.version");
        }

        /**
         * Group of libraries at <b>edc.api.control</b>
         */
        public EdcApiControlLibraryAccessors getControl() {
            return laccForEdcApiControlLibraryAccessors;
        }

    }

    public static class EdcApiControlLibraryAccessors extends SubDependencyFactory {

        public EdcApiControlLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>config</b> with <b>org.eclipse.edc:control-api-configuration</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getConfig() {
            return create("edc.api.control.config");
        }

    }

    public static class EdcConfigLibraryAccessors extends SubDependencyFactory {

        public EdcConfigLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>filesystem</b> with <b>org.eclipse.edc:configuration-filesystem</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getFilesystem() {
            return create("edc.config.filesystem");
        }

    }

    public static class EdcControlplaneLibraryAccessors extends SubDependencyFactory {

        public EdcControlplaneLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>transform</b> with <b>org.eclipse.edc:control-plane-transform</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTransform() {
            return create("edc.controlplane.transform");
        }

    }

    public static class EdcCoreLibraryAccessors extends SubDependencyFactory {
        private final EdcCoreDataPlaneLibraryAccessors laccForEdcCoreDataPlaneLibraryAccessors = new EdcCoreDataPlaneLibraryAccessors(owner);

        public EdcCoreLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>api</b> with <b>org.eclipse.edc:api-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getApi() {
            return create("edc.core.api");
        }

        /**
         * Dependency provider for <b>connector</b> with <b>org.eclipse.edc:connector-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getConnector() {
            return create("edc.core.connector");
        }

        /**
         * Dependency provider for <b>controlplane</b> with <b>org.eclipse.edc:control-plane-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getControlplane() {
            return create("edc.core.controlplane");
        }

        /**
         * Dependency provider for <b>edrstore</b> with <b>org.eclipse.edc:edr-store-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getEdrstore() {
            return create("edc.core.edrstore");
        }

        /**
         * Dependency provider for <b>jersey</b> with <b>org.eclipse.edc:jersey-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJersey() {
            return create("edc.core.jersey");
        }

        /**
         * Dependency provider for <b>jetty</b> with <b>org.eclipse.edc:jetty-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJetty() {
            return create("edc.core.jetty");
        }

        /**
         * Dependency provider for <b>runtime</b> with <b>org.eclipse.edc:runtime-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRuntime() {
            return create("edc.core.runtime");
        }

        /**
         * Dependency provider for <b>token</b> with <b>org.eclipse.edc:token-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getToken() {
            return create("edc.core.token");
        }

        /**
         * Group of libraries at <b>edc.core.dataPlane</b>
         */
        public EdcCoreDataPlaneLibraryAccessors getDataPlane() {
            return laccForEdcCoreDataPlaneLibraryAccessors;
        }

    }

    public static class EdcCoreDataPlaneLibraryAccessors extends SubDependencyFactory {

        public EdcCoreDataPlaneLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>selector</b> with <b>org.eclipse.edc:data-plane-selector-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getSelector() {
            return create("edc.core.dataPlane.selector");
        }

    }

    public static class EdcDcpLibraryAccessors extends SubDependencyFactory {
        private final EdcDcpStsLibraryAccessors laccForEdcDcpStsLibraryAccessors = new EdcDcpStsLibraryAccessors(owner);

        public EdcDcpLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>core</b> with <b>org.eclipse.edc:identity-trust-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCore() {
            return create("edc.dcp.core");
        }

        /**
         * Dependency provider for <b>issuersconfig</b> with <b>org.eclipse.edc:identity-trust-issuers-configuration</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getIssuersconfig() {
            return create("edc.dcp.issuersconfig");
        }

        /**
         * Dependency provider for <b>service</b> with <b>org.eclipse.edc:identity-trust-service</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getService() {
            return create("edc.dcp.service");
        }

        /**
         * Dependency provider for <b>transform</b> with <b>org.eclipse.edc:identity-trust-transform</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTransform() {
            return create("edc.dcp.transform");
        }

        /**
         * Group of libraries at <b>edc.dcp.sts</b>
         */
        public EdcDcpStsLibraryAccessors getSts() {
            return laccForEdcDcpStsLibraryAccessors;
        }

    }

    public static class EdcDcpStsLibraryAccessors extends SubDependencyFactory {

        public EdcDcpStsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>client</b> with <b>org.eclipse.edc:identity-trust-sts-remote-client</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getClient() {
            return create("edc.dcp.sts.client");
        }

    }

    public static class EdcDidLibraryAccessors extends SubDependencyFactory {

        public EdcDidLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>core</b> with <b>org.eclipse.edc:identity-did-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCore() {
            return create("edc.did.core");
        }

        /**
         * Dependency provider for <b>web</b> with <b>org.eclipse.edc:identity-did-web</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getWeb() {
            return create("edc.did.web");
        }

    }

    public static class EdcDpfLibraryAccessors extends SubDependencyFactory {
        private final EdcDpfSelectorLibraryAccessors laccForEdcDpfSelectorLibraryAccessors = new EdcDpfSelectorLibraryAccessors(owner);

        public EdcDpfLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>edc.dpf.selector</b>
         */
        public EdcDpfSelectorLibraryAccessors getSelector() {
            return laccForEdcDpfSelectorLibraryAccessors;
        }

    }

    public static class EdcDpfSelectorLibraryAccessors extends SubDependencyFactory {

        public EdcDpfSelectorLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>core</b> with <b>org.eclipse.edc:data-plane-selector-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCore() {
            return create("edc.dpf.selector.core");
        }

        /**
         * Dependency provider for <b>spi</b> with <b>org.eclipse.edc:data-plane-selector-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getSpi() {
            return create("edc.dpf.selector.spi");
        }

    }

    public static class EdcDspLibraryAccessors extends SubDependencyFactory {
        private final EdcDspApiLibraryAccessors laccForEdcDspApiLibraryAccessors = new EdcDspApiLibraryAccessors(owner);
        private final EdcDspTransformLibraryAccessors laccForEdcDspTransformLibraryAccessors = new EdcDspTransformLibraryAccessors(owner);

        public EdcDspLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>all</b> with <b>org.eclipse.edc:dsp</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAll() {
            return create("edc.dsp.all");
        }

        /**
         * Group of libraries at <b>edc.dsp.api</b>
         */
        public EdcDspApiLibraryAccessors getApi() {
            return laccForEdcDspApiLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.dsp.transform</b>
         */
        public EdcDspTransformLibraryAccessors getTransform() {
            return laccForEdcDspTransformLibraryAccessors;
        }

    }

    public static class EdcDspApiLibraryAccessors extends SubDependencyFactory {

        public EdcDspApiLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>configuration</b> with <b>org.eclipse.edc:dsp-http-api-configuration-2025</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getConfiguration() {
            return create("edc.dsp.api.configuration");
        }

    }

    public static class EdcDspTransformLibraryAccessors extends SubDependencyFactory {
        private final EdcDspTransformCatalogLibraryAccessors laccForEdcDspTransformCatalogLibraryAccessors = new EdcDspTransformCatalogLibraryAccessors(owner);

        public EdcDspTransformLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>catalog2025</b> with <b>org.eclipse.edc:dsp-catalog-transform-2025</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCatalog2025() {
            return create("edc.dsp.transform.catalog2025");
        }

        /**
         * Group of libraries at <b>edc.dsp.transform.catalog</b>
         */
        public EdcDspTransformCatalogLibraryAccessors getCatalog() {
            return laccForEdcDspTransformCatalogLibraryAccessors;
        }

    }

    public static class EdcDspTransformCatalogLibraryAccessors extends SubDependencyFactory {

        public EdcDspTransformCatalogLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>lib</b> with <b>org.eclipse.edc:dsp-catalog-transform-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getLib() {
            return create("edc.dsp.transform.catalog.lib");
        }

    }

    public static class EdcExtLibraryAccessors extends SubDependencyFactory {

        public EdcExtLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>http</b> with <b>org.eclipse.edc:http</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getHttp() {
            return create("edc.ext.http");
        }

    }

    public static class EdcIamLibraryAccessors extends SubDependencyFactory {

        public EdcIamLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>mock</b> with <b>org.eclipse.edc:iam-mock</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getMock() {
            return create("edc.iam.mock");
        }

    }

    public static class EdcLibLibraryAccessors extends SubDependencyFactory {
        private final EdcLibJsonLibraryAccessors laccForEdcLibJsonLibraryAccessors = new EdcLibJsonLibraryAccessors(owner);
        private final EdcLibProvidersLibraryAccessors laccForEdcLibProvidersLibraryAccessors = new EdcLibProvidersLibraryAccessors(owner);

        public EdcLibLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>api</b> with <b>org.eclipse.edc:api-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getApi() {
            return create("edc.lib.api");
        }

        /**
         * Dependency provider for <b>boot</b> with <b>org.eclipse.edc:boot-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getBoot() {
            return create("edc.lib.boot");
        }

        /**
         * Dependency provider for <b>jerseyproviders</b> with <b>org.eclipse.edc:jersey-providers-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJerseyproviders() {
            return create("edc.lib.jerseyproviders");
        }

        /**
         * Dependency provider for <b>query</b> with <b>org.eclipse.edc:query-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getQuery() {
            return create("edc.lib.query");
        }

        /**
         * Dependency provider for <b>sql</b> with <b>org.eclipse.edc:sql-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getSql() {
            return create("edc.lib.sql");
        }

        /**
         * Dependency provider for <b>store</b> with <b>org.eclipse.edc:store-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getStore() {
            return create("edc.lib.store");
        }

        /**
         * Dependency provider for <b>transform</b> with <b>org.eclipse.edc:transform-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTransform() {
            return create("edc.lib.transform");
        }

        /**
         * Dependency provider for <b>util</b> with <b>org.eclipse.edc:util-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getUtil() {
            return create("edc.lib.util");
        }

        /**
         * Group of libraries at <b>edc.lib.json</b>
         */
        public EdcLibJsonLibraryAccessors getJson() {
            return laccForEdcLibJsonLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.lib.providers</b>
         */
        public EdcLibProvidersLibraryAccessors getProviders() {
            return laccForEdcLibProvidersLibraryAccessors;
        }

    }

    public static class EdcLibJsonLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public EdcLibJsonLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>json</b> with <b>org.eclipse.edc:json-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> asProvider() {
            return create("edc.lib.json");
        }

        /**
         * Dependency provider for <b>ld</b> with <b>org.eclipse.edc:json-ld-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getLd() {
            return create("edc.lib.json.ld");
        }

    }

    public static class EdcLibProvidersLibraryAccessors extends SubDependencyFactory {

        public EdcLibProvidersLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>jersey</b> with <b>org.eclipse.edc:jersey-providers-lib</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJersey() {
            return create("edc.lib.providers.jersey");
        }

    }

    public static class EdcOauth2LibraryAccessors extends SubDependencyFactory {

        public EdcOauth2LibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>client</b> with <b>org.eclipse.edc:oauth2-client</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getClient() {
            return create("edc.oauth2.client");
        }

    }

    public static class EdcSpiLibraryAccessors extends SubDependencyFactory {
        private final EdcSpiDataplaneLibraryAccessors laccForEdcSpiDataplaneLibraryAccessors = new EdcSpiDataplaneLibraryAccessors(owner);
        private final EdcSpiDspLibraryAccessors laccForEdcSpiDspLibraryAccessors = new EdcSpiDspLibraryAccessors(owner);
        private final EdcSpiTransactionLibraryAccessors laccForEdcSpiTransactionLibraryAccessors = new EdcSpiTransactionLibraryAccessors(owner);

        public EdcSpiLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>catalog</b> with <b>org.eclipse.edc:catalog-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCatalog() {
            return create("edc.spi.catalog");
        }

        /**
         * Dependency provider for <b>contract</b> with <b>org.eclipse.edc:contract-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getContract() {
            return create("edc.spi.contract");
        }

        /**
         * Dependency provider for <b>core</b> with <b>org.eclipse.edc:core-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCore() {
            return create("edc.spi.core");
        }

        /**
         * Dependency provider for <b>dsp08</b> with <b>org.eclipse.edc:dsp-spi-08</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getDsp08() {
            return create("edc.spi.dsp08");
        }

        /**
         * Dependency provider for <b>jsonld</b> with <b>org.eclipse.edc:json-ld-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJsonld() {
            return create("edc.spi.jsonld");
        }

        /**
         * Dependency provider for <b>transform</b> with <b>org.eclipse.edc:transform-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTransform() {
            return create("edc.spi.transform");
        }

        /**
         * Dependency provider for <b>web</b> with <b>org.eclipse.edc:web-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getWeb() {
            return create("edc.spi.web");
        }

        /**
         * Group of libraries at <b>edc.spi.dataplane</b>
         */
        public EdcSpiDataplaneLibraryAccessors getDataplane() {
            return laccForEdcSpiDataplaneLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.spi.dsp</b>
         */
        public EdcSpiDspLibraryAccessors getDsp() {
            return laccForEdcSpiDspLibraryAccessors;
        }

        /**
         * Group of libraries at <b>edc.spi.transaction</b>
         */
        public EdcSpiTransactionLibraryAccessors getTransaction() {
            return laccForEdcSpiTransactionLibraryAccessors;
        }

    }

    public static class EdcSpiDataplaneLibraryAccessors extends SubDependencyFactory {

        public EdcSpiDataplaneLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>selector</b> with <b>org.eclipse.edc:data-plane-selector-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getSelector() {
            return create("edc.spi.dataplane.selector");
        }

    }

    public static class EdcSpiDspLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public EdcSpiDspLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>dsp</b> with <b>org.eclipse.edc:dsp-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> asProvider() {
            return create("edc.spi.dsp");
        }

        /**
         * Dependency provider for <b>http</b> with <b>org.eclipse.edc:dsp-http-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getHttp() {
            return create("edc.spi.dsp.http");
        }

    }

    public static class EdcSpiTransactionLibraryAccessors extends SubDependencyFactory {

        public EdcSpiTransactionLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>datasource</b> with <b>org.eclipse.edc:transaction-datasource-spi</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getDatasource() {
            return create("edc.spi.transaction.datasource");
        }

    }

    public static class EdcSqlLibraryAccessors extends SubDependencyFactory {
        private final EdcSqlTestLibraryAccessors laccForEdcSqlTestLibraryAccessors = new EdcSqlTestLibraryAccessors(owner);

        public EdcSqlLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>bootstrapper</b> with <b>org.eclipse.edc:sql-bootstrapper</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getBootstrapper() {
            return create("edc.sql.bootstrapper");
        }

        /**
         * Dependency provider for <b>core</b> with <b>org.eclipse.edc:sql-core</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCore() {
            return create("edc.sql.core");
        }

        /**
         * Dependency provider for <b>pool</b> with <b>org.eclipse.edc:sql-pool-apache-commons</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getPool() {
            return create("edc.sql.pool");
        }

        /**
         * Dependency provider for <b>transactionlocal</b> with <b>org.eclipse.edc:transaction-local</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTransactionlocal() {
            return create("edc.sql.transactionlocal");
        }

        /**
         * Group of libraries at <b>edc.sql.test</b>
         */
        public EdcSqlTestLibraryAccessors getTest() {
            return laccForEdcSqlTestLibraryAccessors;
        }

    }

    public static class EdcSqlTestLibraryAccessors extends SubDependencyFactory {

        public EdcSqlTestLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>fixtures</b> with <b>org.eclipse.edc:sql-test-fixtures</b> coordinates and
         * with version reference <b>edc</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getFixtures() {
            return create("edc.sql.test.fixtures");
        }

    }

    public static class JacksonLibraryAccessors extends SubDependencyFactory {

        public JacksonLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>jsr310</b> with <b>com.fasterxml.jackson.datatype:jackson-datatype-jsr310</b> coordinates and
         * with version reference <b>jackson</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJsr310() {
            return create("jackson.jsr310");
        }

    }

    public static class TestcontainersLibraryAccessors extends SubDependencyFactory {

        public TestcontainersLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>junit</b> with <b>org.testcontainers:junit-jupiter</b> coordinates and
         * with version reference <b>testcontainers</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJunit() {
            return create("testcontainers.junit");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>awaitility</b> with value <b>4.2.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getAwaitility() { return getVersion("awaitility"); }

        /**
         * Version alias <b>edc</b> with value <b>0.15.0-SNAPSHOT</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getEdc() { return getVersion("edc"); }

        /**
         * Version alias <b>jackson</b> with value <b>2.19.2</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getJackson() { return getVersion("jackson"); }

        /**
         * Version alias <b>postgres</b> with value <b>42.7.7</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getPostgres() { return getVersion("postgres"); }

        /**
         * Version alias <b>restAssured</b> with value <b>5.5.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getRestAssured() { return getVersion("restAssured"); }

        /**
         * Version alias <b>testcontainers</b> with value <b>1.21.3</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getTestcontainers() { return getVersion("testcontainers"); }

    }

    public static class BundleAccessors extends BundleFactory {
        private final EdcBundleAccessors baccForEdcBundleAccessors = new EdcBundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

        /**
         * Dependency bundle provider for <b>dcp</b> which contains the following dependencies:
         * <ul>
         *    <li>org.eclipse.edc:identity-trust-core</li>
         *    <li>org.eclipse.edc:identity-trust-issuers-configuration</li>
         *    <li>org.eclipse.edc:identity-trust-service</li>
         *    <li>org.eclipse.edc:identity-trust-sts-remote-client</li>
         *    <li>org.eclipse.edc:identity-trust-transform</li>
         *    <li>org.eclipse.edc:identity-did-core</li>
         *    <li>org.eclipse.edc:identity-did-web</li>
         *    <li>org.eclipse.edc:oauth2-client</li>
         * </ul>
         * <p>
         * This bundle was declared in catalog libs.versions.toml
         */
        public Provider<ExternalModuleDependencyBundle> getDcp() {
            return createBundle("dcp");
        }

        /**
         * Group of bundles at <b>bundles.edc</b>
         */
        public EdcBundleAccessors getEdc() {
            return baccForEdcBundleAccessors;
        }

    }

    public static class EdcBundleAccessors extends BundleFactory {

        public EdcBundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

        /**
         * Dependency bundle provider for <b>edc.dpf</b> which contains the following dependencies:
         * <ul>
         *    <li>org.eclipse.edc:data-plane-selector-spi</li>
         *    <li>org.eclipse.edc:data-plane-selector-core</li>
         * </ul>
         * <p>
         * This bundle was declared in catalog libs.versions.toml
         */
        public Provider<ExternalModuleDependencyBundle> getDpf() {
            return createBundle("edc.dpf");
        }

    }

    public static class PluginAccessors extends PluginFactory {
        private final EdcPluginAccessors paccForEdcPluginAccessors = new EdcPluginAccessors(providers, config);

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Plugin provider for <b>shadow</b> with plugin id <b>com.gradleup.shadow</b> and
         * with version <b>8.3.8</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getShadow() { return createPlugin("shadow"); }

        /**
         * Group of plugins at <b>plugins.edc</b>
         */
        public EdcPluginAccessors getEdc() {
            return paccForEdcPluginAccessors;
        }

    }

    public static class EdcPluginAccessors extends PluginFactory {

        public EdcPluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Plugin provider for <b>edc.build</b> with plugin id <b>org.eclipse.edc.edc-build</b> and
         * with version <b>0.1.0</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getBuild() { return createPlugin("edc.build"); }

    }

}
