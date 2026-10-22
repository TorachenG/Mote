plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.torachen.mote.baselineprofile"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }
}

dependencies {
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    // 必须 >= 1.4.0：1.3.x 对 `pm dump-profiles` 的 stdout 做了严格等值校验，
    // 而 Android 16/17 会多打印两行 flush 日志，导致
    // "Expected `pm dump-profiles` stdout to be either black or ..." 直接失败。
    // 上游修复见 androidx commit aad8223c（"Fix overly strict profile flush message check"），
    // 改为 endsWith 匹配。
    implementation("androidx.benchmark:benchmark-macro-junit4:1.5.0")
}