plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    id("androidx.baselineprofile")
}

val generatedSettingsSearchDir: File = layout.buildDirectory
    .dir("generated/settingssearch/kotlin")
    .get()
    .asFile

val generateSettingsSearchCatalog by tasks.registering {
    group = "build"
    description = "扫描 Settings_*.kt，自动生成设置项搜索目录"

    val sourceDir = file("src/main/java/com/torachen/mote/ui/settings")
    val outputDir = generatedSettingsSearchDir

    inputs.dir(sourceDir).withPropertyName("settingsSources")
    outputs.dir(outputDir).withPropertyName("generatedCatalog")

    doLast {
        outputDir.mkdirs()
        val outFile = File(outputDir, "SettingsSearchCatalog.kt")

        val entries = mutableListOf<String>()

        if (sourceDir.exists()) {
            val itemCallRegex = Regex("SearchableItem\\s*\\(([^)]*)\\)")
            val fieldRegex = Regex("(\\w+)\\s*=\\s*\"([^\"]*)\"")
            val tabIndexRegex = Regex("tabIndex\\s*=\\s*(\\d+)")

            sourceDir.listFiles { f ->
                f.isFile && f.name.startsWith("Settings_") && f.name.endsWith(".kt")
            }
                ?.sortedBy { it.name }
                ?.forEach { file ->
                    val content = file.readText()
                    itemCallRegex.findAll(content).forEach { match ->
                        val body = match.groupValues[1]
                        var title: String? = null
                        var route: String? = null
                        var pageTitle: String? = null
                        var itemKey: String? = null
                        var tabIndex: String? = null

                        fieldRegex.findAll(body).forEach { f ->
                            val k = f.groupValues[1]
                            val v = f.groupValues[2]
                            when (k) {
                                "title" -> title = v
                                "pageRoute" -> route = v
                                "pageTitle" -> pageTitle = v
                                "itemKey" -> itemKey = v
                            }
                        }
                        tabIndexRegex.find(body)?.let { tabIndex = it.groupValues[1] }

                        if (title != null && route != null &&
                            pageTitle != null && itemKey != null
                        ) {
                            val tabArg =
                                if (tabIndex != null) ", tabIndex = $tabIndex" else ""
                            entries.add(
                                "    SearchableEntry(\"$title\", \"$route\", \"$pageTitle\", \"$itemKey\"$tabArg),"
                            )
                        }
                    }
                }
        }

        val content = buildString {
            appendLine("// 此文件由 build.gradle.kts 的 generateSettingsSearchCatalog 任务自动生成。")
            appendLine("// 请勿手动修改；重新编译即可根据源码里的 SearchableItem 自动刷新。")
            appendLine()
            appendLine("package com.torachen.mote.ui")
            appendLine()
            appendLine("internal val SETTINGS_SEARCH_CATALOG: List<SearchableEntry> = listOf(")
            entries.forEach { appendLine(it) }
            appendLine(")")
        }

        outFile.writeText(content)
        println(
            "[generateSettingsSearchCatalog] 扫描到 ${entries.size} 个设置项 -> " +
                    outFile.absolutePath
        )
    }
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
        versionCode = 22
        versionName = "0.3.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a")
            isUniversalApk = false
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            // 开启 R8：代码精简 + 优化（含 baseline profile 的 R8 重写）
            optimization {
                enable = true
            }
            // 暂用 debug 签名，便于直接装机验证；正式发布前替换为自有 keystore
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        // 仅用于生成 Baseline Profile 的可采集变体：不可调试、独立包名、debug 签名；
        // 必须保持未混淆，采集结果由 AGP 在 release 打包时按 mapping 重写
        create("profile") {
            isDebuggable = false
            applicationIdSuffix = ".bp"
            versionNameSuffix = "-bp"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    sourceSets["main"].java.srcDir(generatedSettingsSearchDir)
    sourceSets["main"].kotlin.srcDir(generatedSettingsSearchDir)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.media3.common.util.UnstableApi")
    }
}

tasks.named("preBuild") {
    dependsOn(generateSettingsSearchCatalog)
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

    // ===== Material 3 =====
    implementation("org.jetbrains.compose.material3:material3:1.9.0")
    implementation("com.materialkolor:material-kolor:5.0.1")

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

    // ===== AndroidX WebKit =====
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
    // OpenCV：Java 绑定取自官方 AAR 的 classes.jar（app/libs），原生库为自行裁剪编译的 core/imgproc/imgcodecs
    implementation(files("libs/opencv-4.10.0.jar"))
    implementation("com.google.mlkit:text-recognition-chinese:16.0.1")

    // ===== 原生视频播放器 =====
    val media3Version = "1.11.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-exoplayer-hls:$media3Version")
    implementation("androidx.media3:media3-exoplayer-dash:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")
    implementation("androidx.media:media:1.7.0")

    // ★ 关键：主工程引入子模块
    baselineProfile(project(":baselineprofile"))

    // ★ 云端同步SDK
    implementation("com.github.aptabase:aptabase-kotlin:0.0.8")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}