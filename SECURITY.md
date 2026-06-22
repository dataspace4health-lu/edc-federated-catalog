# Security Policy: edc-federated-catalog

## Supported Versions

| Version | Supported |
|---|---|
| 1.1.x (current) | ✅ Active |
| 1.0.x | ⚠️ Security fixes only |
| < 1.0 | ❌ Not supported |

---

## Reporting a Vulnerability

If you discover a security vulnerability in this repository, **do not open a public GitHub issue**. Instead, report it privately to the NTT Data DS4H team:

- **Email**: Contact the repository author (Aastha Rai) or the NTT Data DS4H security team via internal channels.
- **Response time**: We aim to acknowledge reports within 2 business days and provide a remediation plan within 10 business days.
- **Disclosure**: We follow responsible disclosure. We will coordinate a public disclosure date with you after the fix is released.

---

## Security Architecture

### Container Security

- **Non-root execution**: The container runs as `appuser` (UID 10100) in a dedicated system group. The process has no ability to escalate to root inside the container.
- **Minimal base image**: `eclipse-temurin:17-jre` provides only the JRE, not a full JDK or OS with development tools.
- **`pebble` binary removed**: The Canonical Ubuntu `pebble` process manager binary was removed from the base image in v1.1.0 to eliminate 8 HIGH-severity Go stdlib CVEs (CVE-2026-33811, CVE-2026-33814, CVE-2026-39820, CVE-2026-39823, CVE-2026-39825, CVE-2026-39836, CVE-2026-42499, CVE-2026-42504).
- **APT packages upgraded**: The Docker build explicitly upgrades known-vulnerable APT packages (gnupg family, keyboxd) during image assembly.

### Secrets Management

- **No secrets in environment variables** (production): All sensitive values (PostgreSQL passwords, Vault tokens, private keys, certificates) are stored in HashiCorp Vault and resolved at runtime by the `edc:vault-hashicorp` module.
- **Kubernetes secret for Vault token**: The Vault authentication token is stored in a Kubernetes secret (`edc-federator-vault`) rather than in the Helm values file.
- **No secrets in config files**: The `.properties` configuration file uses Vault references (e.g., `vault:secret/catalog#db-password`) rather than plain-text secrets.

### Network Security

- **Participant Registry API is not authenticated**: The `/participants/` endpoints do not enforce authentication at the application layer. They **must** be protected at the infrastructure layer (Kubernetes `NetworkPolicy`, Ingress mTLS, or VPN access restriction). Exposing the participant registry port to the public internet is a security misconfiguration.
- **CORS**: The `CorsFilter` restricts cross-origin requests to the origins of registered participant connectors. The CORS allowlist is refreshed automatically when the participant registry is mutated.
- **Outbound TLS**: All outbound requests from the federator (to participant connectors and the IdentityHub) use TLS. The `edc.http.disable.tls=true` property disables TLS verification and must **never** be set in production.

### Identity and Credential Security

- **DCP-based identity**: The federator uses the Dataspace Credential Protocol (DCP) for inter-connector communication. Participant connectors must present a valid Verifiable Presentation (VP) containing a `MembershipCredential` to respond to catalog queries.
- **VC scope enforcement**: `ScopeExtension` ensures every outgoing catalog request includes the required `MembershipCredential:read` scope. This scope is enforced by participant connectors before they return catalog data.
- **Trusted issuers**: Only VCs issued by trusted issuers (registered in `TrustedIssuerRegistry`) are accepted.

---

## Known Resolved CVEs

### v1.1.0

| CVE / Advisory | Severity | Component | Fix |
|---|---|---|---|
| CVE-2026-2332 | HIGH | Jetty (`jetty-http` 12.1.6) | HTTP/1.1 request smuggling via chunk extensions — pinned Jetty to 12.1.7 |
| CVE-2026-1605 | HIGH | Jetty (`jetty-core` 12.1.0–12.1.5) | GzipHandler JDK Inflater resource leak — pinned Jetty to 12.1.7 |
| GHSA-72hv-8253-57qq | HIGH | jackson-core 2.19.0–2.21.0 | Number length constraint bypass in async parser — pinned jackson-core to 2.21.1 |
| CVE-2026-33811 | HIGH | Go stdlib (in pebble binary) | Removed `pebble` from Docker image |
| CVE-2026-33814 | HIGH | Go stdlib (in pebble binary) | Removed `pebble` from Docker image |
| CVE-2026-39820 | HIGH | Go stdlib (in pebble binary) | Removed `pebble` from Docker image |
| CVE-2026-39823 | HIGH | Go stdlib (in pebble binary) | Removed `pebble` from Docker image |
| CVE-2026-39825 | HIGH | Go stdlib (in pebble binary) | Removed `pebble` from Docker image |
| CVE-2026-39836 | HIGH | Go stdlib (in pebble binary) | Removed `pebble` from Docker image |
| CVE-2026-42499 | HIGH | Go stdlib (in pebble binary) | Removed `pebble` from Docker image |
| CVE-2026-42504 | HIGH | Go stdlib (in pebble binary) | Removed `pebble` from Docker image |

---

## Security Best Practices for Operators

### Deployment

1. **Do not expose the catalog port publicly.** The Participant Registry API (`web.http.catalog.port`, default `8082`) is an administrative interface. Restrict it to internal network access only.
2. **Use an external, production-grade HashiCorp Vault.** The embedded Vault subchart (`vault.enabled: true`) is for development only. Production deployments must use a separately managed, HA Vault cluster.
3. **Rotate Vault tokens regularly.** The Vault token stored in the Kubernetes secret should be a short-lived token tied to a Vault policy with the minimum required permissions.
4. **Enable TLS on all HTTP endpoints.** Terminate TLS at the Kubernetes Ingress level. Do not disable TLS verification (`edc.http.disable.tls=false` is the required production setting).
5. **Keep the Docker base image current.** Rebuild and redeploy when new `eclipse-temurin:17-jre` patches are released.

### Dependency Updates

- Monitor the [EDC release notes](https://github.com/eclipse-edc/Connector/releases) for security advisories in EDC modules.
- Monitor [GitHub Advisories](https://github.com/advisories) for jackson, Jetty, Jersey, and PostgreSQL JDBC driver CVEs.
- Pin fixed versions in the root `build.gradle.kts` `resolutionStrategy` block with a comment referencing the CVE.

### Database

1. **Use a dedicated PostgreSQL user** with permissions scoped to the `edc_participant` table only (SELECT, INSERT, UPDATE, DELETE). Do not use a superuser or the `postgres` system user.
2. **Enable SSL on the PostgreSQL connection** by appending `?ssl=true&sslmode=require` to `edc.datasource.default.url`.
3. **Backup the `edc_participant` table regularly.** Loss of this table results in the federator starting with an empty participant list, which stops catalog crawling.
