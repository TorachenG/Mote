# Android System WebView

Mote 的网页渲染由设备上的 **Android System WebView**（Chromium）提供，它随系统更新，不由 Mote 分发。

- **来源**：由 Google 与设备厂商提供，不同设备上的版本可能不同。
- **数据处理**：Cookie、缓存、localStorage 等由 WebView 按网站要求读写，保存在应用私有目录。
- **开源声明**：Chromium 基于 BSD-3-Clause 等许可协议发布，完整声明见 Chromium 项目主页。

项目主页：https://www.chromium.org/

如果你希望减少本机留存，可以使用无痕模式，或在系统设置中清除应用数据。
