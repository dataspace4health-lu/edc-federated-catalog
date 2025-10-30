package org.eclipse.edc.extensions.federatedcatalog.http;

import okhttp3.OkHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

@Extension(value = "Configurable HTTP Client Extension")
public class HttpClientExtension implements ServiceExtension {

    private static final String DISABLE_TLS_CONFIG = "edc.http.disable.tls";

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        boolean disableTls = context.getConfig().getBoolean(DISABLE_TLS_CONFIG, false);

        if (disableTls) {
            monitor.warning("TLS and hostname verification are DISABLED for HTTP clients! Do not use in production environments.");
        } else {
            monitor.info("TLS and hostname verification are ENABLED for HTTP clients.");
        }
    }

    @Provider
    public OkHttpClient createHttpClient(ServiceExtensionContext context) {
        boolean disableTls = context.getConfig().getBoolean(DISABLE_TLS_CONFIG, false);

        if (disableTls) {
            return createInsecureClient();
        } else {
            // Default secure client
            return new OkHttpClient();
        }
    }

    private OkHttpClient createInsecureClient() {
        try {
            // Trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create insecure HTTP client", e);
        }
    }
}