# CORS Participant Validator Extension

## Problem

EDC's default CORS implementation had the following issues:
1. **Non-compliant multi-origin responses**: Returned comma-separated origin lists (e.g., `Access-Control-Allow-Origin: origin1,origin2`) which violates the [CORS specification](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS#access-control-allow-origin)
2. **No participant validation**: Did not validate origins against the federated catalog's participant list
3. **Browser compatibility**: Browsers expect a single origin or `*`, not comma-separated lists

## Solution

This extension provides CORS-compliant origin handling:
- **Single origin response**: Returns only the specific matching origin (e.g., `Access-Control-Allow-Origin: https://example.com`)
- **Participant-based validation**: Automatically extracts and validates origins from `participants.json`
- **Jersey response filter**: Single filter handles both preflight (OPTIONS) and actual (POST) requests

## Configuration

### Settings

| Setting | Description | Default |
|---------|-------------|----------|
| `edc.web.rest.cors.headers` | Allowed CORS headers | `origin, content-type, accept, authorization, x-api-key` |
| `edc.web.rest.cors.methods` | Allowed HTTP methods | `POST, OPTIONS` |
| `edc.web.rest.cors.origins` | Static origins (comma-separated) | None |
| `edc.federated.catalog.cors.participant.file.path` | Participant JSON file path | `/etc/config/participants.json` |

### Participants File

Origins are extracted from participant URLs in `/etc/config/participants.json`:

```json
{
  "participant1": "https://edc-user1.example.com/api/v1/dsp",
  "participant2": "https://edc-holder1.example.com/api/v1/dsp"
}
```

Extracted origins (without trailing slash to match browser format):
- `https://edc-user1.example.com`
- `https://edc-holder1.example.com`

## Build & Deploy

```bash
# Build
cd /home/developer/repo/edc-federated-catalog
docker build -t localhost:5432/edc-federated-catalog:latest .
docker push localhost:5432/edc-federated-catalog:latest

# Deploy
cd /home/developer/repo/integration-edc
make uninstall && make install
```

### Verify
```bash
kubectl get pods -n edc-federator
kubectl logs -n edc-federator edc-catalog-<pod-id> | grep "CORS"
# Expected: "CORS: Registered CORS filter on context 'catalog'"
# Expected: "CORS: Loaded 2 participant origin(s): https://edc-user1..., https://edc-holder1..."
```

## Testing

### Test 1: OPTIONS Preflight
```bash
kubectl exec -n edc-federator <pod> -- curl -i -X OPTIONS \
  -H "Origin: https://edc-user1.dataspace4health.local" \
  http://127.0.0.1:9195/edc/fc/catalog/api/v1alpha/catalog/query

# Expected:
# HTTP/1.1 200 OK
# Access-Control-Allow-Origin: https://edc-user1.dataspace4health.local
# Access-Control-Allow-Methods: POST, OPTIONS
# Access-Control-Allow-Headers: origin, content-type, accept, authorization, x-api-key
# Access-Control-Allow-Credentials: true
```

### Test 2: POST with Origin
```bash
kubectl exec -n edc-federator <pod> -- curl -i -X POST \
  -H "Origin: https://edc-user1.dataspace4health.local" \
  -H "Content-Type: application/json" \
  -d '{"offset":0,"limit":50}' \
  http://127.0.0.1:9195/edc/fc/catalog/api/v1alpha/catalog/query

# Expected:
# HTTP/1.1 200 OK
# Access-Control-Allow-Origin: https://edc-user1.dataspace4health.local
# Access-Control-Allow-Credentials: true
# [catalog data...]
```

### Test 3: Unauthorized Origin
```bash
kubectl exec -n edc-federator <pod> -- curl -i -X OPTIONS \
  -H "Origin: https://evil.com" \
  http://127.0.0.1:9195/edc/fc/catalog/api/v1alpha/catalog/query

# Expected:
# HTTP/1.1 200 OK
# Allow: POST,OPTIONS
# (no Access-Control-Allow-Origin header - browser will block)
```
## Documentation

For detailed information:

- **CORS_SOLUTION_EXPLAINED.md** - Comprehensive non-technical explanation with analogies
- **TECHNICAL_DOCUMENTATION.md** - Deep dive into implementation and troubleshooting
- **STATUS.md** - Current status and architecture summary
- **RESUME_WORK_HERE.md** - Quick start guide

## Troubleshooting

### CORS Headers Not Appearing
- Verify extension loaded: `kubectl logs <pod> | grep "CORS"`
- Check participants.json mounted: `kubectl exec <pod> -- cat /etc/config/participants.json`
- Ensure Origin header sent in request (browsers send without trailing slash)
- Verify origins loaded without trailing slash in logs

### Unauthorized Origins
- Origin not in participants.json
- Check participant URLs match (including protocol and port)
- Verify origin extraction: `https://example.com:8080/path` → `https://example.com:8080`
- Browsers send Origin WITHOUT trailing slash: `https://example.com` not `https://example.com/`

### Wrong Number of Origins
- If seeing comma-separated origins, default EDC filter may be active
- Verify custom extension loaded
- Check service loader registration in META-INF/services

## Architecture Notes

**Why Single Filter Approach?**
- Filter intercepts ALL responses (OPTIONS and POST)
- No need for separate OPTIONS controller
- Simpler architecture with less code
- Single component handles all CORS logic

**Why No @Provider Annotation?**
- Filter registered programmatically via `webService.registerResource()`
- Jersey automatically detects `ContainerResponseFilter` implementations
- EDC ServiceExtension controls instantiation and lifecycle

**Why Origins Without Trailing Slash?**
- Browsers send Origin headers WITHOUT trailing slash
- Example: Browser sends `https://example.com` not `https://example.com/`
- Our stored origins must match browser format for validation to work

## Status

✅ **Production Ready** - Deployed and tested in Kubernetes  
✅ **CORS Compliant** - Returns single matching origin  
✅ **Simple Architecture** - Single filter handles all CORS logic  
✅ **Browser Compatible** - Origins match browser format (no trailing slash)  
✅ **Tested** - Preflight, POST, and unauthorized origin scenarios verified

---

For questions or more details, see `CORS_SOLUTION_EXPLAINED.md`