/* 
 * Copyright 2026 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  // Can't use alias() or we get some weird error about double Android on classpath?
  id(libs.plugins.android.application.get().pluginId)
  alias(libs.plugins.ksp)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.android.cacheFix)
}

android {
  namespace = "com.pyamsoft.tetherfi"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.pyamsoft.tetherfi"
    versionCode = 71
    versionName = "20260818-1"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    isCoreLibraryDesugaringEnabled = true
  }

  signingConfigs {
    named("debug") {
      storeFile = file("debug.keystore")
      keyAlias = "androiddebugkey"
      keyPassword = "android"
      storePassword = "android"
    }
    create("release") {
      // FIX FOR CI: local.properties may not exist
      val rootPath = isolated.rootProject.projectDirectory.asFile.absolutePath
      val propFile = file("$rootPath/local.properties")
      if (propFile.exists()) {
        val properties = propFile.reader().use { r -> Properties().apply { load(r) } }
        storeFile = file(properties.getProperty("BUNDLE_STORE_FILE") ?: "debug.keystore")
        keyAlias = properties.getProperty("BUNDLE_KEY_ALIAS") ?: "androiddebugkey"
        keyPassword = properties.getProperty("BUNDLE_KEY_PASSWD") ?: "android"
        storePassword = properties.getProperty("BUNDLE_STORE_PASSWD") ?: "android"
      } else {
        // CI fallback - use debug keystore
        storeFile = file("debug.keystore")
        keyAlias = "androiddebugkey"
        keyPassword = "android"
        storePassword = "android"
      }
    }
  }

  flavorDimensions += listOf("store")
  productFlavors {
    create("google") {
      dimension = "store"
      dependenciesInfo {
        includeInApk = true
        includeInBundle = true
      }
    }
    create("fdroid") {
      dimension = "store"
      dependenciesInfo {
        includeInApk = false
        includeInBundle = false
      }
    }
  }

  buildTypes {
    release {
      signingConfig = signingConfigs.getByName("release")
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      ndk {
        debugSymbolLevel = "FULL"
      }
    }
    debug {
      signingConfig = signingConfigs.getByName("debug")
      applicationIdSuffix = ".dev"
      versionNameSuffix = "-dev"
      ndk {
        debugSymbolLevel = "FULL"
      }
    }
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }

  packaging {
    resources.pickFirsts += setOf(
      "META-INF/core_release.kotlin_module",
      "META-INF/ui_release.kotlin_module",
      "META-INF/INDEX.LIST",
      "META-INF/io.netty.versions.properties",
    )
  }
}

kotlin {
  compilerOptions {
    languageVersion = KotlinVersion.KOTLIN_2_4
    jvmTarget = JvmTarget.JVM_21
  }
}

dependencies {
  coreLibraryDesugaring(libs.android.desugar)
  ksp(libs.dagger.compiler)
  debugImplementation(libs.leakcanary)
  implementation(libs.leakcanary.plumber)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.preference)
  implementation(libs.androidx.dataStore)
  implementation(libs.pydroid.notify)
  implementation(libs.pydroid.ui)
  add("fdroidImplementation", libs.pydroid.billing.noop)
  add("fdroidImplementation", libs.pydroid.bootstrap.noop)
  add("googleImplementation", libs.pydroid.billing.play)
  add("googleImplementation", libs.pydroid.bootstrap.play)
  implementation(project(":behavior"))
  implementation(project(":connections"))
  implementation(project(":core"))
  implementation(project(":info"))
  implementation(project(":main"))
  implementation(project(":server"))
  implementation(project(":service"))
  implementation(project(":settings"))
  implementation(project(":status"))
  implementation(project(":tile"))
  implementation(project(":ui"))
  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
}
