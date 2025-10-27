project.setProperty("mainClassName", "org.eclipse.edc.boot.system.runtime.BaseRuntime")

plugins {
    `java-library`
    id("checkstyle")
    id("application")
    alias(libs.plugins.shadow)
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    compileOnly(libs.edc.fc.spi.crawler)

    implementation(libs.edc.runtime.core)
    implementation(libs.edc.connector.core)
    implementation(libs.edc.boot)
    implementation(libs.edc.control.plane.core)
    implementation(libs.edc.configuration.filesystem)
    implementation(libs.edc.token.core)
    implementation(libs.edc.http)
    implementation(libs.edc.dsp)
    implementation(libs.edc.iam.mock)
    implementation(libs.edc.api.observability)

    implementation(project(":extensions:catalog-node-resolver"))
    implementation(project(":extensions:store:sql:participant-store-sql")) // ADD THIS LINE
    implementation("org.postgresql:postgresql:42.7.3")
    runtimeOnly(libs.edc.api.observability)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

checkstyle {
    toolVersion = "12.0.0"
    configFile = file("config/checkstyle/checkstyle.xml")
    isShowViolations = true
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
}

tasks.withType<Checkstyle> {
    reports {
        xml.required.set(false)
        html.required.set(true)
        html.outputLocation.set(file("$buildDir/reports/checkstyle/lint.html"))
    }
}

tasks.test {
    reports.html.outputLocation.set(file("$buildDir/test-results/html/test.html"))
    reports.junitXml.outputLocation.set(file("$buildDir/test-results/xml/test.xml"))
}