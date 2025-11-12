# Unit Tests Documentation

## Current Test Coverage

### ✅ Participant Entity Tests (13 tests)
Location: `extensions/store/sql/participant-store-sql/src/test/java/org/eclipse/edc/extensions/federatedcatalog/store/sql/ParticipantTest.java`

#### Test Categories:

**1. Builder Pattern Tests (3 tests)**
- `testBuilder_setsAllFields()` - Verifies all fields are correctly set via builder
- `testBuilder_buildsEmptyParticipant()` - Ensures builder works with no fields
- `testBuilder_canChainMethods()` - Validates fluent method chaining

**2. Getters and Setters (1 test)**
- `testSettersAndGetters()` - Confirms all properties can be set and retrieved

**3. Equals Contract Tests (4 tests)**
- `testEquals_sameId_returnsTrue()` - Same ID means objects are equal
- `testEquals_differentId_returnsFalse()` - Different IDs means objects are not equal
- `testEquals_sameInstance_returnsTrue()` - Object equals itself (reflexive)
- `testEquals_withNull_returnsFalse()` - Object is not equal to null
- `testEquals_withDifferentClass_returnsFalse()` - Object is not equal to different type

**4. HashCode Contract Tests (3 tests)**
- `testHashCode_sameId_returnsSameHashCode()` - Equal objects have same hash code
- `testHashCode_differentId_returnsDifferentHashCode()` - Different objects have different hash codes
- `testHashCode_nullId_handlesGracefully()` - Null IDs don't cause exceptions

**5. Chaining Test (1 test)**
- `testBuilder_canChainMethods()` - Builder methods can be chained fluently

### ❌ Not Yet Implemented - CRUD API Controller Tests

The following tests should be created for complete coverage:

**ParticipantRegistryApiController Tests** (Location: Same directory, new file `ParticipantRegistryApiControllerTest.java`)

1. **Create Tests (POST /participants)**
   - `testCreate_validParticipant_returnsCreated()`
   - `testCreate_duplicateId_returnsConflict()`
   - `testCreate_invalidData_returnsBadRequest()`

2. **Read All Tests (GET /participants)**
   - `testGetAll_returnsAllParticipants()`
   - `testGetAll_emptyDatabase_returnsEmptyList()`

3. **Read One Tests (GET /participants/{id})**
   - `testGetById_existingParticipant_returnsParticipant()`
   - `testGetById_nonExistingParticipant_returnsNotFound()`

4. **Update Tests (PUT /participants/{id})**
   - `testUpdate_existingParticipant_returnsUpdated()`
   - `testUpdate_nonExistingParticipant_returnsNotFound()`
   - `testUpdate_invalidData_returnsBadRequest()`

5. **Delete Tests (DELETE /participants/{id})**
   - `testDelete_existingParticipant_returnsNoContent()`
   - `testDelete_nonExistingParticipant_returnsNotFound()`

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

### Option 3: Via Test Script
For quick testing without full build:

```bash
cd /home/developer/repo/edc-federated-catalog
./run-tests.sh
```

## Test Reports

After running tests, view the HTML report at:
```
extensions/store/sql/participant-store-sql/build/reports/tests/test/index.html
```

## Test Frameworks Used

- **JUnit 5 (Jupiter)** - Test framework
- **AssertJ** - Fluent assertion library
- **Gradle** - Build and test execution

## Next Steps

1. Create controller tests for CRUD operations
2. Create integration tests with actual database
3. Add test coverage reporting
4. Consider adding mock/stub tests for dependencies
