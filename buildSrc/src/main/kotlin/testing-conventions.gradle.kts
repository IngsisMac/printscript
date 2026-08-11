import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("kotlin-conventions")
    `jvm-test-suite`
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    testImplementation(libs.findLibrary("junit-api").get())
    testRuntimeOnly(libs.findLibrary("junit-engine").get())
    testImplementation(libs.findLibrary("junit-params").get())
    testImplementation(libs.findLibrary("hamcrest").get())
    testImplementation(libs.findLibrary("mockito").get())
    testImplementation(libs.findLibrary("mockito-kotlin").get())
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(project())
            }
        }

        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(libs.findLibrary("junit-api").get())
                runtimeOnly(libs.findLibrary("junit-engine").get())
                implementation(libs.findLibrary("junit-params").get())
                implementation(libs.findLibrary("hamcrest").get())
                implementation(libs.findLibrary("mockito").get())
                implementation(libs.findLibrary("mockito-kotlin").get())
            }
            targets.all {
                testTask.configure {
                    shouldRunAfter(testing.suites.named("test"))
                }
            }
        }

        register<JvmTestSuite>("memoryTest") {
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(libs.findLibrary("junit-api").get())
                runtimeOnly(libs.findLibrary("junit-engine").get())
                implementation(libs.findLibrary("junit-params").get())
                implementation(libs.findLibrary("hamcrest").get())
            }
            targets.all {
                testTask.configure {
                    minHeapSize = "5m"
                    maxHeapSize = "7m"
                    shouldRunAfter(testing.suites.named("test"))
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
    dependsOn(testing.suites.named("memoryTest"))
}


