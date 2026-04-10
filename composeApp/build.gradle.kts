import org.gradle.kotlin.dsl.invoke
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // Only configure iOS targets on macOS to avoid slowing configuration on other OSes
    val osName = System.getProperty("os.name").lowercase()
    if (osName.contains("mac")) {
        iosArm64()
        iosSimulatorArm64()
        // Configure frameworks if needed
        listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }
    }

    // JVM target for Compose Desktop
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Keep only truly multiplatform libs here
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)

                // Ktor shared (core & negotiation)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)
            }
        }

        val androidMain by getting {
            dependencies {
                // Android-specific libs remain here
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.activity.compose)

                // Core Android
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.activity.compose)

                // Compose UI (Android BOM)
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.compose.ui)
                implementation(libs.androidx.compose.material3)
                implementation(libs.androidx.compose.ui.tooling.preview)

                // Navigation
                implementation(libs.androidx.navigation.compose)

                // DataStore (Android)
                implementation(libs.androidx.datastore.preferences)

                // Palette
                implementation(libs.androidx.palette.ktx)

                // SQLite
                implementation(libs.androidx.sqlite.ktx)

                // Ktor Android engine (OkHttp is faster to initialize)
                implementation(libs.ktor.client.okhttp)
            }
        }

        val jvmMain by getting {
            dependencies {
                // Compose Desktop runtime
                implementation(compose.desktop.currentOs)

                // Swing coroutine helpers (if used)
                implementation(libs.kotlinx.coroutinesSwing)

                // Use OkHttp on JVM/desktop for faster cold start than CIO
                implementation(libs.ktor.client.okhttp)

                // mDNS / discovery
                implementation(libs.jmdns)

                // DataStore JVM impl
                implementation(libs.androidx.datastore.preferences.jvm)

                implementation(libs.slf4j.simple)

                // NOTE: slf4j is moved out of here to debugImplementation below
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "it.sonix.connect"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "it.sonix.connect"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

// make slf4j debug-only so it doesn't initialize logging in release/startup
dependencies {
    debugImplementation(libs.compose.uiTooling)
    debugImplementation(libs.slf4j.simple)
}

compose.desktop {


    application {

        mainClass = "it.sonix.connect.MainKt"

        // JVM args tuned for faster startup (adjust to your needs)
        jvmArgs(
            "-Xms128m",
            "-Xmx512m",
            "-XX:+UseZGC",
            // skiko renderer selection: OPENGL or METAL (on mac), try OPENGL first
            "-Dskiko.renderApi=OPENGL"
        )

        buildTypes {
            release {
                proguard {
                    isEnabled.set(false)
                }
            }
        }

        nativeDistributions {
            targetFormats(
                TargetFormat.Exe,
                TargetFormat.Msi,
                TargetFormat.Dmg,
                TargetFormat.Deb
            )

            packageName = "Sonix Connect"
            packageVersion = "1.1.0"
            description = "Desktop Application"
            vendor = "SonixLabs"

            windows {
                iconFile.set(project.file("src/jvmMain/resources/icons/sonix_logo.ico"))
                menuGroup = "Sonix Connect"
                shortcut = true
                perUserInstall = true
                dirChooser = true
            }
        }
    }
}


/*
tasks.register<Exec>("signMsi") {
    commandLine(
        "signtool",
        "sign",
        "/f", "certificate.pfx",
        "/p", "password",
        "/tr", "http://timestamp.digicert.com",
        "/td", "sha256",
        "/fd", "sha256",
        "build/compose/binaries/main-release/msi/Sonix Connect.msi"
    )
}*/
