# INFRASTRUCTURE.md

# Infrastructure: edc-federated-catalog

## Overview

The `edc-federated-catalog` is deployed on Kubernetes via the `helm-edc-federated-catalog` Helm chart. It runs alongside a dedicated IdentityHub instance and an optional dashboard, all wired together under one federator identity.

---

## Helm Chart

**Chart location:** `helm-edc-federated-catalog`

The parent chart packages:
- `edc-federated-catalog` (catalog runtime) from `components/catalog`
- `federator-identityhub` (identity and STS) from `components/identityhub`
- `dashboard` (read-only UI) from `components/dashboard`
- optional `vault` dependency

### Install

```bash
cd helm-edc-federated-catalog
make build
make install NAMESPACE=edc-federator override_values="-f my-federator-values.yaml"
```

Or directly with Helm:

```bash
# Without embedded Vault
helm install edc-federator ./helm-edc-federated-catalog \
  --namespace edc-federator \
  --create-namespace \
  -f helm-edc-federated-catalog/values.yaml

# With embedded dev Vault
helm install edc-federator ./helm-edc-federated-catalog \
  --namespace edc-federator \
  --create-namespace \
  -f helm-edc-federated-catalog/values.yaml \
  -f helm-edc-federated-catalog/vault-values.yaml
```

### Upgrade

```bash
helm upgrade edc-federator ./helm-edc-federated-catalog \
  --namespace edc-federator \
  -f my-federator-values.yaml
```

### Uninstall

```bash
helm uninstall edc-federator --namespace edc-federator
```

---

## Kubernetes Resources

The chart creates the following resources per release:

| Resource | Description |
|---|---|
| `Deployment` (catalog) | Runs the federated catalog runtime |
| `Deployment` (identityhub) | Runs the federator IdentityHub |
| `Deployment` (dashboard) | Runs the read-only dashboard UI |
| `Service` | Exposes catalog ports 8082 and 8181 within the cluster |
| `Ingress` | Routes external traffic for catalog API, DID, STS, and dashboard |
| `ConfigMap` | Mounts EDC configuration properties |
| `Secret` (optional) | Vault token secret |

### Container

| Setting | Value |
|---|---|
| Base image | `eclipse-temurin:17-jre` |
| Run as user | UID 10100 (non-root) |
| Exposed ports | 8082 (catalog API), 8181 (health) |

---

## Dependencies

### PostgreSQL

Each subchart (catalog and identityhub) deploys its own PostgreSQL instance by default.

| Mode | Configuration |
|---|---|
| Bundled (default) | `postgresql.enabled: true` |
| External | `postgresql.enabled: false` — configure JDBC URL via properties |

The catalog database requires the `edc_participant` table. Schema is auto-created at startup (`edc:sql-bootstrapper`).

### HashiCorp Vault

Both catalog and identityhub runtimes share the same Vault token source by default (`edc-federator-vault` secret).

| Mode | Configuration |
|---|---|
| Bundled dev Vault | `vault.enabled: true` — for local/integration use only |
| External Vault | `vault.enabled: false` — configure vault URL and token secret in both subcharts |

Create the Vault token secret before installing:

```bash
kubectl create secret generic edc-federator-vault \
  --from-literal=token=<vault-token> \
  -n edc-federator
```

---

## Ingress

The chart exposes one public hostname for all federator endpoints.

```yaml
ingress:
  enabled: true
  hosts:
    - host: edc-federator.dataspace4health.local
      paths:
        - path: /edc/fc/catalog/api
          port: 8082
          pathType: Prefix
        - path: /.well-known
          port: <identityhub-did-port>
          pathType: Prefix
        - path: /edc/ih/sts/api
          port: <identityhub-sts-port>
          pathType: Prefix
        - path: /edc/dashboard
          port: <dashboard-port>
          pathType: Prefix
  tls:
    - secretName: edc-federator-tls
      hosts:
        - edc-federator.dataspace4health.local
```

---

## Health and Readiness

Health check endpoint:

```
GET http://<pod-ip>:8181/api/check/health
```

Expected response: `[{"isHealthy": true, ...}]`

---

## Vault Secrets Required Before Startup

| Vault Path | Key | Description |
|---|---|---|
| `secret/catalog` | `db-password` | PostgreSQL password |
| `secret/catalog` | `private-key` | Federator private key for DID/signing |
| `secret/catalog` | `public-key` | Federator public key |

```bash
vault kv put secret/catalog \
  db-password="<password>" \
  private-key="$(cat private.pem)" \
  public-key="$(cat public.pem)"
```

---

## Rollback

```bash
# List release history
helm history edc-federator --namespace edc-federator

# Rollback to previous revision
helm rollback edc-federator --namespace edc-federator

# Rollback to specific revision
helm rollback edc-federator 2 --namespace edc-federator
```

The `edc_participant` schema is additive — the only migration is the initial `CREATE TABLE IF NOT EXISTS`. Revert manual schema changes before rolling back the application.

---

## Key Chart Values Reference

| Value | Description |
|---|---|
| `edc-federated-catalog.config.participantId` | Federator DID |
| `edc-federated-catalog.config.participants` | List of participant DSP endpoints to crawl |
| `edc-federated-catalog.config.cacheExecutionDelay` | Seconds before first crawl |
| `edc-federated-catalog.config.cacheExecutionPeriod` | Seconds between crawl cycles |
| `edc-federated-catalog.config.sts.oauthTokenUrl` | Internal STS endpoint |
| `edc-federated-catalog.config.sts.oauthClientId` | Must match federator DID |
| `edc-federated-catalog.config.iam.trustedissuer` | Trusted VC issuers for catalog interactions |
| `edc-federated-catalog.config.vault.url` | Vault server URL |
| `edc-federated-catalog.config.vault.tokenSecretName` | Kubernetes secret with Vault token |
| `federator-identityhub.config.participant.id` | Federator DID (must match catalog) |
| `federator-identityhub.config.vault.url` | Vault URL for IdentityHub |
| `vault.enabled` | Deploy bundled dev Vault |
| `dashboard.config.federatedCatalogEnabled` | Enable catalog tab in dashboard |
| `dashboard.config.federatedCatalogUrl` | Federator catalog API URL |
| `dashboard.config.negotiationEnabled` | Set `false` for read-only federator dashboard |
