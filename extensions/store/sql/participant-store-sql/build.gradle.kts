plugins {
    `java-library`
}

dependencies {
    implementation(project(":extensions:catalog-node-resolver"))
    implementation(libs.edc.core.spi)
    implementation(libs.postgres)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}