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
}