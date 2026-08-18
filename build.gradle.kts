plugins {
    id("kotlin-conventions") apply false
    id("testing-conventions") apply false
}

allprojects {
    group = "com.printscript"
    version = "1.0.0-SNAPSHOT"
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

