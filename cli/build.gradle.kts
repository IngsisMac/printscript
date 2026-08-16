plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    application
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.picocli)
    api(project(":runner"))
    api(project(":common"))
}

application {
    mainClass = "com.printscript.cli.MainKt"
}

tasks.named<JavaExec>("run") {
    workingDir = rootDir
}

tasks.withType<JacocoReport>().configureEach {
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude("**/MainKt*")
                }
            },
        ),
    )
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude("**/MainKt*")
                }
            },
        ),
    )
}
