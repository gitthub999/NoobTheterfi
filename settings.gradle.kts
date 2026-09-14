import org.gradle.kotlin.dsl.withType

pluginManagement {
  repositories {
    maven { url = uri(rootProject.projectDir.resolve("local-repo")) }
    mavenLocal()
    google()
    mavenCentral()
    gradlePluginPortal()
    maven {
      setUrl("https://jitpack.io")
      content {
        includeGroup("com.github.pyamsoft.cachify")
        includeGroup("com.github.pyamsoft.pydroid")
        includeGroup("com.github.pyamsoft")
      }
    }
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    maven { url = uri("local-repo") }
    mavenLocal()
    google()
    mavenCentral()
    gradlePluginPortal()
    maven {
      setUrl("https://jitpack.io")
      content {
        includeGroup("com.github.pyamsoft.cachify")
        includeGroup("com.github.pyamsoft.pydroid")
        includeGroup("com.github.pyamsoft")
      }
    }
  }
}

gradle.lifecycle.beforeProject {
  tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
    options.isDeprecation = true
    options.isFork = true
  }
  tasks.withType<Test>().configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
    reports.html.required.set(false)
    reports.junitXml.required.set(false)
    maxHeapSize = "4g"
  }
}

rootProject.name = "TetherFuseNet"
include(":app")
include(":behavior")
include(":connections")
include(":core")
include(":info")
include(":main")
include(":networktest")
include(":server")
include(":service")
include(":settings")
include(":status")
include(":tile")
include(":ui")
