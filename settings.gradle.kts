rootProject.name = "printscript"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

include(
    "common",
    "token",
    "ast",
    "lexer",
    "parser",
    "interpreter",
    "formatter",
    "linter",
    "runner",
    "cli"
)
