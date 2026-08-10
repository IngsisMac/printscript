plugins {
    id("kotlin-conventions")
    id("testing-conventions")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    api(project(":lexer"))
    api(project(":ast"))
    api(project(":common"))
}
