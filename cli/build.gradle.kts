plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    application
}

dependencies {
    implementation(libs.kotlin.stdlib)
    api(project(":runner"))
    api(project(":common"))
}

application {
    mainClass = "com.printscript.cli.MainKt"
}
