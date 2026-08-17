pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SistemaPersonal"

include(":app-personal")
include(":app-familiar")
include(":core-model")
include(":core-domain")
include(":core-data")
include(":core-network")
include(":core-ui")
