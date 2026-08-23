plugins {
    id("kotlin-conventions") apply false
    id("testing-conventions") apply false
}

val baseVersion = providers.gradleProperty("baseVersion").getOrElse("1.0")
val buildNumber = System.getenv("GITHUB_RUN_NUMBER")
val releaseVersion =
    if (!buildNumber.isNullOrBlank()) {
        "$baseVersion.$buildNumber"
    } else {
        "$baseVersion.0-SNAPSHOT"
    }

allprojects {
    group = "com.printscript"
    version = releaseVersion
}

tasks.register<Copy>("installGitHooks") {
    description = "Installs Git hooks from .githooks into .git/hooks"
    group = "build setup"
    from(file("$rootDir/.githooks"))
    into(file("$rootDir/.git/hooks"))
    filePermissions {
        user {
            read = true
            write = true
            execute = true
        }
        group {
            read = true
            execute = true
        }
        other {
            read = true
            execute = true
        }
    }
}
