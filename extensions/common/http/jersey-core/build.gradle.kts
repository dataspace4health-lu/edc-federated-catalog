plugins {
    `java-library`
}

dependencies {
    api(libs.edc.jersey.providers)
    api(libs.bundles.jersey.core)
    implementation(libs.jakarta.rsApi)

    // Add dependency to participant store for database-backed loading
    implementation(project(":extensions:store:sql:participant-sql"))

    testImplementation(libs.edc.junit)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
}
