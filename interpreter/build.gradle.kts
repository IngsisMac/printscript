plugins {
    id("kotlin-conventions")
    id("testing-conventions")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    api(project(":parser"))
    api(project(":common"))
}
