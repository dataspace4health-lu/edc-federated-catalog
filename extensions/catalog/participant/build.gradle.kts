plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.core.spi)
    implementation(libs.edc.web.spi)
    implementation(libs.jakarta.rsApi)
    api(libs.swagger.annotations.jakarta)
    
    // Add dependency to participant store for database-backed loading
    implementation(project(":extensions:store:sql:participant-sql"))

    // Test dependencies
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.jersey.common)
}
