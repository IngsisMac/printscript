dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
            version("kotlin", "2.0.21")
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").version("2.0.21")
            library("kotlin-gradle", "org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
        }
    }
}

