plugins {
    `java-library`
    id("checkstyle")
    id("application")
    id("test-report-aggregation")
    id("jacoco")
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

    // Pin jackson-core (only) to 2.21.1 to fix GHSA-72hv-8253-57qq (Number Length Constraint
    // Bypass in Async Parser, published 2026-02-26). EDC BOM 0.15.1 resolves 2.20.1 which is
    // in the affected range 2.19.0-2.21.0; patched version is 2.21.1.
    // NOTE: only jackson-core received the 2.21.1 security patch — jackson-annotations and
    // jackson-databind have no 2.21.1 release and must remain at their BOM-resolved versions.
    // Pin Eclipse Jetty (all modules) to 12.1.7 to fix:
    //  - CVE-2026-1605 (HIGH) — GzipHandler JDK Inflater resource leak when handling compressed
    //    requests with uncompressed responses. Affects 12.0.0-12.0.31 and 12.1.0-12.1.5;
    //    fixed in 12.0.32 and 12.1.6.
    //  - CVE-2026-2332 (HIGH) — HTTP/1.1 parser vulnerable to request smuggling via chunk
    //    extensions ("funky chunks"). Affects jetty-http 12.1.6; fixed in 12.1.7 and 12.0.33.
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.fasterxml.jackson.core" && requested.name == "jackson-core") {
                useVersion("2.21.1")
                because("GHSA-72hv-8253-57qq: pin jackson-core to 2.21.1 (patched)")
            } else if (requested.group == "org.eclipse.jetty") {
                useVersion("12.1.7")
                because("CVE-2026-2332: pin all Jetty modules to 12.1.7 to fix HTTP/1.1 request smuggling via chunk extensions (HIGH)")
            }
        }
    }
}

dependencies {
    implementation(libs.edc.runtime.core)
    implementation(libs.edc.connector.core)
    implementation(libs.edc.boot)
    implementation(libs.edc.control.plane.core)
    implementation(libs.edc.token.core)
    implementation(libs.edc.http) {
        exclude(group = "org.eclipse.edc", module = "jersey-core")
    }
    implementation(libs.edc.dsp)
    implementation(libs.edc.decentralized.claims.core)
    implementation(libs.edc.api.observability)
    implementation(libs.edc.participant.context.single.core)

    // Filesystem configuration support for loading properties file
    implementation(libs.edc.configuration.filesystem)
    
    // // JSON-LD support for DSP protocol
    // implementation(libs.edc.json.ld.lib)
    
    // // DID and OAuth2 support for identity management
    // implementation(libs.edc.did.core)
    // implementation(libs.edc.did.web)
    // implementation(libs.edc.oauth2.client)
    // implementation(libs.edc.spi.token)
    // implementation(libs.edc.spi.jwt)

    // //IH module dependencies
    // implementation(libs.edc.core.spi)
    // implementation(libs.edc.token.core)

    // Identity and DID modules (required by DSP protocol adapters)
    implementation(libs.edc.decentralized.claims.core)
    implementation(libs.edc.json.ld.lib)
    implementation(libs.edc.did.core)
    implementation(libs.edc.oauth2.client)
    
    // Federated Catalog Core modules with protocol adapters for DSP crawling
    implementation(libs.edc.fc.core)
    implementation(libs.edc.fc.core2025)  // DSP 2025/1 protocol adapter
    implementation(libs.edc.fc.core08)     // DSP 08 protocol adapter
    implementation(libs.edc.fc.api)
    
    // SQL infrastructure
    implementation(libs.edc.sql.core)
    implementation(libs.edc.sql.pool)
    implementation(libs.edc.sql.transactionlocal)
    implementation(libs.edc.util.lib)
    implementation(libs.edc.sql.lib)
    implementation(libs.edc.sql.bootstrapper)
    implementation(libs.postgres)

    implementation(libs.edc.vault.hashicorp)

    implementation(project(":extensions:catalog:directory"))
    implementation(project(":extensions:catalog:participant"))
    implementation(project(":extensions:common:http:client"))
    implementation(project(":extensions:common:http:jersey-core"))
    implementation(project(":extensions:store:sql:participant-sql"))
    implementation(project(":extensions:scope"))

    testReportAggregation(project(":extensions:catalog:directory"))
    testReportAggregation(project(":extensions:catalog:participant"))
    testReportAggregation(project(":extensions:common:http:client"))
    testReportAggregation(project(":extensions:common:http:jersey-core"))
    testReportAggregation(project(":extensions:store:sql:participant-sql"))
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

sourceSets {
    main {
        java {
            // First, clear default srcDirs by removing them one by one
            setSrcDirs(emptySet<File>())

            // Find all directories named 'src/main/java' under 'extensions'
            val srcFolders = file("extensions").walkTopDown()
                .filter { it.isDirectory && it.path.endsWith("src${File.separator}main${File.separator}java") }
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
        html.outputLocation.set(layout.buildDirectory.file("reports/checkstyle/lint.html"))
    }
}

subprojects {
    plugins.apply("java")
    plugins.apply("jacoco")
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        // optional per-task report locations; not necessary for aggregation
        
        testLogging {
            events("FAILED", "PASSED", "SKIPPED")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
            showStandardStreams = true
        }

        reports {
            junitXml.required.set(true)
            junitXml.outputLocation.set(layout.buildDirectory.dir("reports/junit/xml"))
            html.required.set(true)
            html.outputLocation.set(layout.buildDirectory.dir("reports/junit/html"))
        }
        finalizedBy(tasks.jacocoTestReport)
    }
    jacoco {
        toolVersion = "0.8.10"
    }
    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/xml/jacoco.xml"))
            html.required.set(true)
            html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
            csv.required.set(false)
        }
    }
}

// 1) Register a single aggregation task
val allTests by tasks.register<TestReport>("allTests") {
    destinationDirectory.set(layout.buildDirectory.dir("reports/junit/html"))
    // Collect results from all subprojects' "test" tasks
    testResults.from(subprojects.map { it.tasks.named("test") })
}

// 2) Ensure aggregation runs even if tests fail
// finalizedBy makes Gradle run 'allTests' regardless of the outcome of 'test'
tasks.named("test") {
    finalizedBy(allTests)
}
