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
    implementation(libs.edc.iam.mock)
    implementation(libs.edc.api.observability)
    implementation(libs.edc.participant.context.single.core)
    
    // SQL infrastructure
    implementation(libs.edc.sql.core)
    implementation(libs.edc.sql.pool)
    implementation(libs.edc.sql.transactionlocal)
    implementation(libs.edc.util.lib)
    implementation(libs.edc.sql.lib)
    implementation(libs.edc.sql.bootstrapper)
    implementation(libs.postgres)

    implementation(project(":extensions:catalog:directory"))
    implementation(project(":extensions:catalog:participant"))
    implementation(project(":extensions:common:http:client"))
    implementation(project(":extensions:common:http:jersey-core"))
    implementation(project(":extensions:store:sql:participant-sql"))

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
    // Collect results from all subprojects’ "test" tasks
    testResults.from(subprojects.map { it.tasks.named("test") })
}

// 2) Ensure aggregation runs even if tests fail
// finalizedBy makes Gradle run 'allTests' regardless of the outcome of 'test'
tasks.named("test") {
    finalizedBy(allTests)
}



