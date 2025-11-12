plugins {
    `java-library`
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":extensions:catalog-node-resolver"))
    implementation(libs.edc.core.spi)
    implementation(libs.edc.transaction.spi)
    implementation(libs.edc.transactions.datasource.spi)
    implementation(libs.edc.web.spi)
    implementation(libs.postgres)
    implementation(libs.jakarta.rsApi)
    
    // Swagger/OpenAPI dependencies - use implementation instead of compileOnly for Docker build
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.20")
    
    // Test dependencies - use consistent JUnit 5 versions
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.glassfish.jersey.core:jersey-common:3.1.3") // For JAX-RS Response implementation
    testImplementation("org.glassfish.jersey.inject:jersey-hk2:3.1.3") // For dependency injection
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
}