plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.fc.core)
    implementation(libs.edc.fc.ext.api)
    implementation(libs.edc.fc.spi.crawler)
    
    // Add dependency to participant store for database-backed loading
    implementation(project(":extensions:store:sql:participant-store-sql"))

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Test dependencies
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
}