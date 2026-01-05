plugins {
    `java-library`
}

dependencies {
    api(libs.edc.fc.spi.crawler)
    implementation(libs.edc.fc.core)
    implementation(libs.edc.fc.ext.api)
    
    // Add dependency to participant store for database-backed loading
    implementation(project(":extensions:store:sql:participant-sql"))

    // Test dependencies
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
}