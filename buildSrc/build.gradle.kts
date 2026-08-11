plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.spotless.plugin)
    implementation(libs.detekt.plugin)
}

