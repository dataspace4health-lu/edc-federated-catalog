plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.fc.core)
    implementation(libs.edc.fc.ext.api)
    implementation(libs.edc.fc.spi.crawler)

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}
