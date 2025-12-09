plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.core.spi)
    implementation(libs.edc.transaction.spi)
    implementation(libs.edc.transactions.datasource.spi)
    implementation(libs.edc.web.spi)
    
    // EDC SQL infrastructure
    implementation(libs.edc.util.lib)
    implementation(libs.edc.sql.lib)
    implementation(libs.edc.sql.bootstrapper)
    
    implementation(libs.postgres)
    implementation(libs.jakarta.rsApi)
    
    // Swagger/OpenAPI dependencies - use implementation instead of compileOnly for Docker build
    implementation(libs.swagger.annotations.jakarta)
    
    // Test dependencies - use consistent JUnit 5 versions
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.jersey.common) // For JAX-RS Response implementation
    testImplementation(libs.jersey.hk2) // For dependency injection
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}