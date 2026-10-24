package com.torachen.mote.ui.floating

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/** 与 Mote 当前主题一致：跟随系统深浅色 */
@Composable
internal fun isInDarkTheme(): Boolean = isSystemInDarkTheme()
