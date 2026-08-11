plugins {
    kotlin("jvm")
    `java-library`
    id("com.diffplug.spotless")
    id("io.gitlab.arturbosch.detekt")
    jacoco
}

kotlin {
    jvmToolchain(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

version = "0.1.0"

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint("1.3.1")
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("1.3.1")
    }
}

configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
    buildUponDefaultConfig = true
    allRules = false
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.withType<Test>().configureEach {
    finalizedBy("jacocoTestReport")
}

tasks.withType<JacocoReport>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
