plugins {
    `java-library`
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
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
}