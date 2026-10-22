package com.torachen.mote.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 全局 UI 风格模式。
 * - Miuix：使用 MIUIX 组件库（默认）
 * - Material：使用 Material 3 组件库
 *
 * 由 prefs 键 "ui_style" 持久化，值域 { "miuix", "material" }。
 * 通过 LocalUiMode 向下分发到各页面。
 */
enum class UiMode(val value: String) {
    Miuix("miuix"),
    Material("material");

    companion object {
        fun fromValue(value: String): UiMode = when (value) {
            Material.value -> Material
            else -> Miuix
        }

        val DEFAULT_VALUE = Miuix.value
    }
}

val LocalUiMode = staticCompositionLocalOf { UiMode.Miuix }