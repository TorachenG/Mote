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
        versionCode = 18
        versionName = "0.2.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    //构建 Release APK 时仅保留 arm64 和 x86_64，减小包体大小，但不兼容 arm32 与 x86
    //splits {
    //    abi {
    //        isEnable = true
    //        reset()
    //        include("arm64-v8a", "x86_64")
    //        isUniversalApk = false
    //    }
    //}
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.media3.common.util.UnstableApi")
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
    implementation(libs.miuix.preference)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.blur)
    implementation(libs.miuix.squircle)
    implementation("top.yukonga.miuix.kmp:miuix-nav:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-shader:0.9.4")

    // ===== AndroidX WebKit（推测加载 API） =====
    implementation("androidx.webkit:webkit:1.17.1")

    // ===== 测试 =====
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.profileinstaller:profileinstaller:1.4.1")

    // ===== 扫一扫 =====
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // ===== 原生视频播放器（ExoPlayer / Media3）=====
    val media3Version = "1.11.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-exoplayer-hls:$media3Version")
    implementation("androidx.media3:media3-exoplayer-dash:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version") // 使用基础的 PlayerView 视图
    implementation("androidx.media:media:1.7.0")

    baselineProfile(project(":baselineprofile"))
}
