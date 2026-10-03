plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    id("androidx.baselineprofile")
}

android {
    namespace = "com.torachen.mote"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.torachen.mote"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "0.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ===== AndroidX 基础库 =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ===== JetBrains Compose Multiplatform 核心 =====
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // ===== JetBrains 导航库 =====
    implementation(libs.jetbrains.navigation.compose)
    implementation("androidx.navigationevent:navigationevent-compose:1.1.2")

    // ===== MIUIX 组件库 =====
    implementation(libs.miuix.ui)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.preference)
    implementation(libs.miuix.squircle)
    implementation(libs.miuix.blur)
    implementation("top.yukonga.miuix.kmp:miuix-nav:0.9.4")

    // ===== 测试 =====
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    baselineProfile(project(":baselineprofile"))
}