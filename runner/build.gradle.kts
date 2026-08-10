plugins {
    id("kotlin-conventions")
    id("testing-conventions")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    api(project(":interpreter"))
    api(project(":formatter"))
    api(project(":linter"))
    api(project(":common"))
}
