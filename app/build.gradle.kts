import io.github.z4kn4fein.semver.toVersion
import kotlin.random.Random

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.devtools)
    alias(libs.plugins.about.libraries)
}

val outputApkFileName = "${rootProject.name}-$version.apk"

dependencies {
    // AndroidX Core
    implementation(libs.androidx.ktx)
    implementation(libs.runtime.ktx)
    implementation(libs.runtime.compose)
    implementation(libs.splash.screen)
    implementation(libs.activity.compose)
    implementation(libs.work.runtime.ktx)
    implementation(libs.preferences.datastore)
    implementation(libs.appcompat)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.preview)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.livedata)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)

    // Accompanist
    implementation(libs.accompanist.drawablepainter)

    // Placeholder
    implementation(libs.placeholder.material3)

    // Coil (async image loading, network image)
    implementation(libs.coil.compose)
    implementation(libs.coil.appiconloader)

    // KotlinX
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collection.immutable)
    implementation(libs.kotlinx.datetime)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)

    // ReVanced
    implementation(libs.revanced.patcher)
    implementation(libs.revanced.library)

    // Downloader plugins
    implementation(project(":api"))

    // Native processes
    implementation(libs.kotlin.process)

    // HiddenAPI
    compileOnly(libs.hidden.api.stub)

    // LibSU
    implementation(libs.libsu.core)
    implementation(libs.libsu.service)
    implementation(libs.libsu.nio)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.navigation)
    implementation(libs.koin.workmanager)

    // Licenses
    implementation(libs.about.libraries)

    // Ktor
    implementation(libs.ktor.core)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.okhttp)
    implementation(libs.ktor.content.negotiation)
    implementation(libs.ktor.serialization)

    // Markdown
    implementation(libs.markdown.renderer)

    // Fading Edges
    implementation(libs.fading.edges)

    // Scrollbars
    implementation(libs.scrollbars)

    // EnumUtil
    implementation(libs.enumutil)
    ksp(libs.enumutil.ksp)

    // Reorderable lists
    implementation(libs.reorderable)

    // Compose Icons
    implementation(libs.compose.icons.fontawesome)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // Semantic versioning string parser
        classpath(libs.semver.parser)
    }
}

android {
    namespace = "app.revanced.manager"
    compileSdk = 35
    buildToolsVersion = "35.0.1"

    defaultConfig {
        applicationId = "app.revanced.manager"
        minSdk = 26
        targetSdk = 35

        val versionStr = if (version == "unspecified") "1.0.0" else version.toString()
        versionName = versionStr
        versionCode = with(versionStr.toVersion()) {
            major * 10_000_000 +
                    minor * 10_000 +
                    patch * 100 +
                    (preRelease?.substringAfterLast('.')?.toInt() ?: 0)
        }
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "Mystery Manager (Debug)")
            isPseudoLocalesEnabled = true

            buildConfigField("long", "BUILD_ID", "${Random.nextLong()}L")
        }

        release {
            if (!project.hasProperty("noProguard")) {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }

            applicationIdSuffix = ".liso"
            resValue("string", "app_name", "Mystery Manager")

            signingConfig = if (
                project.hasProperty("signing.storeFile") &&
                project.hasProperty("signing.storePassword") &&
                project.hasProperty("signing.keyAlias") &&
                project.hasProperty("signing.keyPassword")
            ) {
                signingConfigs.create("ci") {
                    storeFile = file(project.property("signing.storeFile") as String)
                    storePassword = project.property("signing.storePassword") as String
                    keyAlias = project.property("signing.keyAlias") as String
                    keyPassword = project.property("signing.keyPassword") as String
                }
            } else {
                signingConfigs.getByName("debug")
            }

            buildConfigField("long", "BUILD_ID", "0L")
        }
    }

    applicationVariants.all {
        outputs.all {
            this as com.android.build.gradle.internal.api.ApkVariantOutputImpl

            outputFileName = outputApkFileName
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "/prebuilt/**",
                "META-INF/DEPENDENCIES",
                "META-INF/**.version",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json",
                "org/bouncycastle/pqc/**.properties",
                "org/bouncycastle/x509/**.properties",
            )
        )
        jniLibs {
            useLegacyPackaging = true
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
    }

    android {
        androidResources {
            generateLocaleConfig = true
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

kotlin {
    jvmToolchain(17)
}
