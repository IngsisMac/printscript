plugins {
    id("kotlin-conventions")
    id("testing-conventions")
}

dependencies {
    api(libs.kotlin.stdlib)
    api(project(":lexer"))
    api(project(":parser"))
    api(project(":interpreter"))
    api(project(":formatter"))
    api(project(":linter"))
    api(project(":common"))

    testImplementation(project(":common"))
    testImplementation(project(":token"))
    testImplementation(project(":ast"))
    testImplementation(project(":lexer"))
    testImplementation(project(":parser"))
    testImplementation(project(":interpreter"))
}
