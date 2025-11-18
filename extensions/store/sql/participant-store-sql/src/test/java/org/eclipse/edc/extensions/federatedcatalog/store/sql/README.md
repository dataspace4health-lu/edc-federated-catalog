# Unit Tests Documentation

## Test Files Structure

```
extensions/store/sql/participant-store-sql/src/test/java/
└── org/eclipse/edc/extensions/federatedcatalog/store/sql/
    ├── ParticipantTest.java                         (13 tests - Entity)
    └── api/
        ├── ParticipantValidatorTest.java            (21 tests - Validation)
        └── ParticipantRegistryApiControllerTest.java (27 tests - CRUD API)
```

**Total: 61 unit tests**

## Current Test Coverage

### ✅ Participant Entity Tests (13 tests)
Location: `extensions/store/sql/participant-store-sql/src/test/java/org/eclipse/edc/extensions/federatedcatalog/store/sql/ParticipantTest.java`

Tests the Participant data model class including builder pattern, getters/setters, equals/hashCode contracts, and edge cases.

### ✅ Participant Validator Tests (21 tests)
Location: `extensions/store/sql/participant-store-sql/src/test/java/org/eclipse/edc/extensions/federatedcatalog/store/sql/api/ParticipantValidatorTest.java`

Tests input validation for:
- ID validation (null, empty, whitespace, length limits)
- Name validation (null, empty, length limits)
- DSP URL validation (null, empty, invalid format, length limits)
- Supported protocols validation (null, empty)
- Multiple validation errors handling

### ✅ CRUD API Controller Tests (27 tests)
Location: `extensions/store/sql/participant-store-sql/src/test/java/org/eclipse/edc/extensions/federatedcatalog/store/sql/api/ParticipantRegistryApiControllerTest.java`

Tests all REST API endpoints using Mockito to mock dependencies:

**CREATE Tests (7 tests):**
- Valid participant creation returns 201 Created
- Duplicate ID returns 409 Conflict
- Missing ID returns 400 Bad Request
- Missing name returns 400 Bad Request
- Invalid DSP URL returns 400 Bad Request
- Missing protocols returns 400 Bad Request
- Store failure returns 500 Internal Server Error

**READ ONE Tests (4 tests):**
- Existing participant returns 200 OK
- Non-existing participant returns 404 Not Found
- Null ID returns 400 Bad Request
- Empty ID returns 400 Bad Request

**READ ALL Tests (3 tests):**
- Returns all participants with 200 OK
- Empty database returns empty list with 200 OK
- Store exception returns 500 Internal Server Error

**UPDATE Tests (5 tests):**
- Valid update returns 200 OK
- Non-existing participant returns 404 Not Found
- Path ID mismatch with body ID returns 400 Bad Request
- Invalid data returns 400 Bad Request
- Store failure returns 500 Internal Server Error

**DELETE Tests (5 tests):**
- Existing participant deletion returns 204 No Content
- Non-existing participant returns 404 Not Found
- Null ID returns 400 Bad Request
- Empty ID returns 400 Bad Request
- Store failure returns 500 Internal Server Error

## Total Test Coverage

**61 unit tests** covering:
- ✅ Entity/Model layer (Participant class)
- ✅ Validation layer (ParticipantValidator class)
- ✅ Controller/API layer (ParticipantRegistryApiController class)

### ❌ Not Yet Implemented - Integration Tests

The following integration tests could be added for complete coverage:

**ParticipantStore Integration Tests** (with real database):
- Test actual SQL queries with a test database (e.g., H2 in-memory or Testcontainers PostgreSQL)
- Test transaction rollback scenarios
- Test concurrent access scenarios
- Test database connection failures

**End-to-End API Tests:**
- Test full HTTP request/response cycle with embedded server
- Test authentication/authorization (if applicable)
- Test content negotiation
- Test error responses in real HTTP context

## Running Tests

### Option 1: Via Docker Build (Recommended)
Tests now run automatically during Docker build:

```bash
cd /home/developer/repo/edc-federated-catalog
docker build -t localhost:5432/edc-federated-catalog:latest .
```

The build will **fail** if any test fails, ensuring only tested code is deployed.

### Option 2: Via Gradle (requires JDK with compiler)
Run tests for the participant-store module:

```bash
cd /home/developer/repo/edc-federated-catalog
./gradlew :extensions:store:sql:participant-store-sql:test
```

## Test Reports

After running tests, view the HTML report at:
```
extensions/store/sql/participant-store-sql/build/reports/tests/test/index.html
```

## Test Frameworks Used

- **JUnit 5 (Jupiter)** - Test framework with `@Test`, `@BeforeEach`, `@ExtendWith`
- **Mockito** - Mocking framework for isolating dependencies (mocks ParticipantStore, Monitor)
- **AssertJ** - Fluent assertion library for readable test assertions
- **Jersey** - JAX-RS implementation required for Response object creation in tests
- **Gradle** - Build and test execution with JUnit Platform support

## Key Configuration

The subproject `build.gradle.kts` requires:
```kotlin
tasks.withType<Test> {
    useJUnitPlatform()  // CRITICAL: Without this, JUnit 5 tests won't run!
}
```

And test dependencies:
```kotlin
testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
testImplementation("org.assertj:assertj-core:3.24.2")
testImplementation("org.mockito:mockito-core:5.8.0")
testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
testImplementation("org.glassfish.jersey.core:jersey-common:3.1.3")
testImplementation("org.glassfish.jersey.inject:jersey-hk2:3.1.3")
testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
```

## Next Steps

1. Create controller tests for CRUD operations
2. Create integration tests with actual database
3. Add test coverage reporting
4. Consider adding mock/stub tests for dependencies
