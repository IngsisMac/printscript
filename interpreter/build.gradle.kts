plugins {
    id("kotlin-conventions")
    id("testing-conventions")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    api(project(":ast"))
    api(project(":common"))
    testImplementation(project(":parser"))
    testImplementation(project(":lexer"))
}
