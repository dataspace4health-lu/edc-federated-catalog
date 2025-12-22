plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.core.spi)
    implementation(libs.edc.transaction.spi)
    implementation(libs.edc.transactions.datasource.spi)
    
    // EDC SQL infrastructure
    implementation(libs.edc.util.lib)
    implementation(libs.edc.sql.lib)
    implementation(libs.edc.sql.bootstrapper)
    
    implementation(libs.postgres)
    
    // Test dependencies - use consistent JUnit 5 versions
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
}