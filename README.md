#  Mote

Mote 是一款完全使用 Jetpack Compose 编写的 Android 浏览器，UI 严格遵循 [MIUIX](https://compose-miuix-ui.github.io/miuix/) 设计语言，力求在所有 Android 设备上还原 Xiaomi Hyper OS 的视觉效果，同时带来流畅、纯净、优雅的浏览体验。

>项目当前在功能开发阶段，不代表最终品质

### 项目结构

每个设置子页面共享同一套骨架：`Box` + `layerBackdrop` + `LazyColumn` + 顶部模糊栏，保证视觉一致性。

### 技术栈

| 组件 | 说明 |
| :--- | :--- |
| 语言 | Kotlin |
| UI | Jetpack Compose / Compose Multiplatform |
| 设计系统 | [MIUIX for Compose](https://compose-miuix-ui.github.io/miuix/) |
| Web 引擎 | Android WebView |
| 数据持久化 | SharedPreferences |
| 最低支持 | Android 7.0 (API 24) |

### 构建

```bash
git clone https://github.com/TorachenG/Mote.git
cd Mote
./gradlew :app:assembleDebug
