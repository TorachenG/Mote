package com.torachen.mote.ui.component.miuix.effect

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun isInDarkTheme(): Boolean {
    return MiuixTheme.colorScheme.surface.luminance() < 0.5f
}