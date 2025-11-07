plugins {
    `java-library`
    id("checkstyle")
    id("application")
    alias(libs.plugins.shadow)
}

version = ""

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
    implementation(libs.edc.token.core)
    implementation(libs.edc.http)
    implementation(libs.edc.dsp)
    implementation(libs.edc.iam.mock)
    implementation(libs.edc.api.observability)
    
    // SQL infrastructure
    implementation(libs.edc.sql.core)
    implementation(libs.edc.sql.pool)
    implementation(libs.edc.sql.transactionlocal)
    implementation(libs.postgres)

    implementation(project(":extensions:catalog-node-resolver"))
    implementation(project(":extensions:store:sql:participant-store-sql"))
    
    runtimeOnly(libs.edc.api.observability)

}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

sourceSets {
    main {
        java {
            // First, clear default srcDirs by removing them one by one
            setSrcDirs(emptySet<File>())

            // Find all directories named 'src' under 'extensions/**/src'
            val srcFolders = file("extensions").walkTopDown()
                .filter { it.isDirectory && it.name == "src" }
                .toSet()

            // Add those directories as source dirs
            setSrcDirs(srcFolders)
        }
    }
}

checkstyle {
    toolVersion = "12.0.0"
    configFile = file("config/checkstyle/checkstyle.xml")
    isShowViolations = true
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveClassifier.set("")
}

tasks.withType<Checkstyle> {
    reports {
        xml.required.set(false)
        html.required.set(true)
        html.outputLocation.set(file("$buildDir/reports/checkstyle/lint.html"))
    }
}

tasks.test {
    // Change the directory for HTML reports
    reports.html.outputLocation.set(file("$buildDir/test-results/html/test.html"))

    // Change the directory for JUnit XML test results
    reports.junitXml.outputLocation.set(file("$buildDir/test-results/xml/test.xml"))
}