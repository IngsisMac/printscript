plugins {
    id("kotlin-conventions")
    id("testing-conventions")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    api(project(":token"))
    api(project(":ast"))
    api(project(":common"))
    testImplementation(project(":lexer"))
}
