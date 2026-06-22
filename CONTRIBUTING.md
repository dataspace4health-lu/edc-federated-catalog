# Contributing to edc-federated-catalog

## Overview

This document describes how to contribute to the `edc-federated-catalog` repository. It covers the project structure, coding standards, how to write and run tests, and the pull request process.

---

## Repository Structure

```
edc-federated-catalog/
├── build.gradle.kts              # Root build: shadow JAR, dependency pinning, source set aggregation
├── settings.gradle.kts           # Gradle settings and subproject declarations
├── Dockerfile                    # Multi-stage Docker build
├── config/checkstyle/            # Checkstyle configuration
├── gradle/libs.versions.toml     # Version catalog for all dependencies
├── extensions/
│   ├── catalog/
│   │   ├── directory/            # CatalogNodeResolverExtension + DbCatalogNodeDirectory
│   │   └── participant/          # ParticipantApiExtension + REST API controller
│   ├── common/
│   │   └── http/
│   │       ├── client/           # Configurable HTTP client
│   │       └── jersey-core/      # Jersey REST server with CORS
│   ├── scope/                    # ScopeExtension + ScopeMappingFunction
│   └── store/
│       └── sql/
│           └── participant-sql/  # ParticipantStore SQL implementation + schema
└── memory-bank/                  # Axet memory bank (do not treat as codebase)
```

---

## Development Environment

### Requirements

- JDK 17 (`java --version` should report `17.x`)
- Gradle wrapper is included — do not install Gradle globally
- Docker (for building images and running Testcontainers-based integration tests)

### Initial Setup

```bash
# Clone and enter the repository
cd edc-federated-catalog

# Verify the build
./gradlew shadowJar --no-daemon

# Run all tests
./gradlew allTests --no-daemon
```

---

## Coding Standards

### Style

- Code style is enforced by **Checkstyle** (`config/checkstyle/checkstyle.xml`). Violations fail the build.
- Run checkstyle manually: `./gradlew checkstyleMain`
- The HTML report is generated at `build/reports/checkstyle/lint.html`

### General Rules

1. **Use `Monitor` for logging** — never import `java.util.logging`, SLF4J, or Log4j directly in extension code. Use `Monitor.debug()`, `Monitor.info()`, `Monitor.warning()`, `Monitor.severe()`.

2. **Inject EDC services via `@Inject`** — services managed by the EDC runtime (Monitor, WebService, PolicyEngine, ParticipantStore, etc.) must be declared as `@Inject` fields. Wire custom collaborators in `initialize()` or `@Provider` methods.

3. **Use `ApiResponse<T>` envelope** — all Participant Registry API responses must use `ApiResponse.success()` or `ApiResponse.error()`. Do not return raw entity objects.

4. **Check `ServiceResult<T>` before use** — `ParticipantStore` methods return `ServiceResult<T>`. Always call `.succeeded()` / `.failed()` before accessing `.getContent()`.

5. **Avoid static state** — extension classes are singletons in the EDC runtime. Do not use mutable static fields.

6. **Register services via `META-INF/services`** — every `ServiceExtension` implementation must be listed in `src/main/resources/META-INF/services/org.eclipse.edc.spi.system.ServiceExtension`.

### Adding a New Extension

1. Create a new directory under `extensions/` following the existing module structure.
2. Add a `build.gradle.kts` declaring the module's dependencies.
3. Add the module to `settings.gradle.kts`.
4. Implement your `ServiceExtension` and register it in `META-INF/services`.
5. Add the module as an `implementation` dependency in the root `build.gradle.kts`.
6. Add the module to `testReportAggregation` in root `build.gradle.kts` if it has tests.

### Adding a New Participant API Endpoint

1. Add the method signature to `ParticipantRegistryApi` (the JAX-RS interface).
2. Implement the method in `ParticipantRegistryApiController`.
3. Validate inputs using `ParticipantValidator` or a new dedicated validator.
4. Return `ApiResponse.success()` on success and `ApiResponse.error()` on failure.
5. Call `refreshCorsOrigins()` if the operation mutates the participant registry.
6. Add unit tests in `ParticipantRegistryApiControllerTest`.

---

## Testing

### Running Tests

```bash
# Run all tests (all subprojects) with aggregated report
./gradlew allTests --no-daemon

# Run tests for a specific subproject
./gradlew :extensions:catalog:participant:test --no-daemon

# Run tests with coverage
./gradlew test jacocoTestReport --no-daemon

# Coverage report: build/reports/jacoco/html/index.html
```

### Test Structure

Each extension module has tests under `src/test/java/...` mirroring the main source structure.

| Test Class | What it tests |
|---|---|
| `CatalogNodeResolverExtensionTest` | Extension initialization, `@Provider` method |
| `DbCatalogNodeDirectoryTest` | `getAll()`, `insert()`, `remove()` with mock `ParticipantStore` |
| `ParticipantApiExtensionTest` | Extension initialization, controller registration |
| `ParticipantRegistryApiControllerTest` | All CRUD endpoints, validation, error paths |
| `ParticipantValidatorTest` | Field validation rules |
| `ApiResponseTest` | `ApiResponse` factory methods |
| `ParticipantDtoTest` | DTO builder and field accessors |
| `ScopeExtensionTest` | Post-validator registration |
| `ScopeMappingFunctionTest` | Scope merging logic |

### Writing Tests

- Use **JUnit Jupiter** (`@Test`, `@BeforeEach`, etc.)
- Use **Mockito** for mocking EDC services (`Monitor`, `ParticipantStore`, etc.)
- Use **AssertJ** for assertions (`assertThat(result).isEqualTo(expected)`)
- For integration tests requiring a database, use **Testcontainers** with `@Testcontainers` and a `PostgreSQLContainer`

### Test Example Pattern

```java
@ExtendWith(MockitoExtension.class)
class DbCatalogNodeDirectoryTest {

    @Mock
    private ParticipantStore participantStore;

    @Mock
    private Monitor monitor;

    private DbCatalogNodeDirectory directory;

    @BeforeEach
    void setUp() {
        directory = new DbCatalogNodeDirectory(participantStore, monitor);
    }

    @Test
    void getAll_shouldReturnMappedTargetNodes() {
        // Arrange
        var participant = Participant.builder()
                .id("p-01").name("Test").endpoint("https://example.com/dsp")
                .supportedProtocols(List.of("dataspace-protocol-http"))
                .createdAt(0L).updatedAt(0L).build();
        when(participantStore.findAll()).thenReturn(Stream.of(participant));

        // Act
        var result = directory.getAll();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("p-01");
    }
}
```

---

## Dependency Management

All dependency versions are declared in `gradle/libs.versions.toml`. When adding a new dependency:

1. Add the version to `[versions]` if it's a new library family.
2. Add the library to `[libraries]`.
3. Reference it in the relevant `build.gradle.kts` via `libs.your-library`.

### Security Pinning

If a CVE affects a transitive dependency, pin the fixed version in the root `build.gradle.kts` `resolutionStrategy` block with a comment explaining the CVE and the reason for the pin. Follow the pattern already established for jackson-core and Jetty.

---

## Pull Request Process

1. **Branch naming**: `feature/<short-description>`, `fix/<short-description>`, `chore/<short-description>`
2. **Commit messages**: Use imperative mood, e.g. `Add pagination to participant list endpoint`
3. **Tests**: All new code must have unit tests. Do not submit PRs that reduce test coverage.
4. **Checkstyle**: PRs must pass `./gradlew checkstyleMain` with no violations.
5. **Memory bank**: If your change introduces a new architectural pattern, modifies the data model, or changes key configuration properties, update the relevant memory bank files (`systemPatterns.md`, `techContext.md`, `activeContext.md`, `progress.md`).
6. **Documentation**: Update `API.md` for new or changed API endpoints, `DEPLOYMENT.md` for new configuration properties, and `ARCHITECTURE.md` for structural changes.

---

## Extension Registration Reference

Every `ServiceExtension` must be registered. The file to update is:

```
extensions/{module}/src/main/resources/META-INF/services/org.eclipse.edc.spi.system.ServiceExtension
```

The file content is the fully-qualified class name, e.g.:

```
com.nttdata.dataspace.edc.fc.catalog.directory.CatalogNodeResolverExtension
```

If a module has multiple extensions, list each on a separate line.

---

## Contact

Author: Aastha Rai  
Organization: NTT Data  
Project: Dataspace4Health (DS4H)
