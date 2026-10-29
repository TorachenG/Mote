package com.torachen.mote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.DropdownMenu as M3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.RadioButton as M3RadioButton
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun UaSettingsPage(onBack: () -> Unit) {
    if (LocalUiMode.current == UiMode.Miuix) UaSettingsPageMiuix(onBack) else UaSettingsPageMaterial(onBack)
}

@Composable
private fun UaSettingsPageMiuix(onBack: () -> Unit) {
    val context = LocalContext.current
    var preset by remember { mutableStateOf(UaStore.presetIndex(context)) }
    var customList by remember { mutableStateOf(UaStore.customList(context)) }
    var customSelected by remember { mutableStateOf(UaStore.customSelected(context)) }
    var desktopIndex by remember { mutableStateOf(UaStore.desktopIndex(context)) }
    var input by remember { mutableStateOf("") }
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun exitSelect() {
        selecting = false
        selected = emptySet()
    }

    val barTitle = if (selecting) "已选择 ${selected.size} 项" else "浏览器标识"

    Column(modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface)) {
        TopAppBar(
            title = barTitle,
            largeTitle = barTitle,
            color = Color.Transparent,
            defaultWindowInsetsPadding = true,
            navigationIcon = {
                IconButton(onClick = { if (selecting) exitSelect() else onBack() }) {
                    Icon(
                        imageVector = if (selecting) MiuixIcons.Close else MiuixIcons.ChevronBackward,
                        contentDescription = if (selecting) "关闭" else "返回",
                        tint = MiuixTheme.colorScheme.onSurface,
                    )
                }
            },
            actions = {
                if (selecting) {
                    val all = customList.toSet()
                    IconButton(onClick = { selected = if (selected.size == all.size) emptySet() else all }) {
                        Icon(
                            imageVector = MiuixIcons.SelectAll,
                            contentDescription = "全选",
                            tint = if (selected.size == all.size) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().overScrollVertical(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
            overscrollEffect = null,
        ) {
            item(key = "preset_header") { SmallTitle("预设") }
            item(key = "preset_card") {
                Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    UaStore.presets.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clickable {
                                    preset = index
                                    customSelected = null
                                    UaStore.setPresetIndex(context, index)
                                    UaStore.setCustomSelected(context, null)
                                }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                item.name,
                                style = MiuixTheme.textStyles.headline1,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            RadioButton(
                                selected = customSelected == null && preset == index,
                                onClick = null,
                            )
                        }
                    }
                }
            }

            item(key = "custom_header") { SmallTitle("自定义") }
            item(key = "custom_card") {
                Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        TextField(
                            value = input,
                            onValueChange = { input = it },
                            label = "User-Agent",
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Button(
                                onClick = {
                                    val ua = input.trim()
                                    if (ua.isNotBlank() && ua !in customList) {
                                        customList = customList + ua
                                        UaStore.setCustomList(context, customList)
                                        input = ""
                                    }
                                },
                            ) { Text("添加") }
                        }
                    }
                    if (customList.isNotEmpty()) {
                        customList.forEach { ua ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (selecting) {
                                                selected = if (ua in selected) selected - ua else selected + ua
                                            } else {
                                                customSelected = ua
                                                UaStore.setCustomSelected(context, ua)
                                            }
                                        },
                                        onLongClick = {
                                            selecting = true
                                            selected = selected + ua
                                        },
                                    )
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    ua,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 14.sp,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                if (selecting) {
                                    Spacer(Modifier.width(12.dp))
                                    Checkbox(
                                        state = if (ua in selected) ToggleableState.On else ToggleableState.Off,
                                        onClick = null,
                                    )
                                } else {
                                    RadioButton(selected = customSelected == ua, onClick = null)
                                }
                            }
                        }
                    }
                }
            }

            item(key = "other_header") { SmallTitle("其它") }
            item(key = "other_card") {
                Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    WindowDropdownPreference(
                        title = "电脑模式下的浏览器标识",
                        entries = listOf(
                            DropdownEntry(
                                items = UaStore.desktopOptions.mapIndexed { index, label ->
                                    DropdownItem(
                                        text = label,
                                        selected = desktopIndex == index,
                                        onClick = {
                                            desktopIndex = index
                                            UaStore.setDesktopIndex(context, index)
                                        },
                                    )
                                },
                            ),
                        ),
                        collapseOnSelection = true,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 24.dp)
                .height(56.dp)
                .background(MiuixTheme.colorScheme.surfaceContainer, RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selecting) {
                IconButton(onClick = {
                    customList = customList.filterNot { it in selected }
                    UaStore.setCustomList(context, customList)
                    if (customSelected in selected) {
                        customSelected = null
                        UaStore.setCustomSelected(context, null)
                    }
                    exitSelect()
                }) {
                    Icon(MiuixIcons.Delete, contentDescription = "删除", tint = Color(0xFFFF3B30))
                }
            } else {
                IconButton(onClick = { selecting = false }) {
                    Icon(MiuixIcons.ChevronBackward, contentDescription = "返回", tint = MiuixTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun UaSettingsPageMaterial(onBack: () -> Unit) {
    val context = LocalContext.current
    var preset by remember { mutableStateOf(UaStore.presetIndex(context)) }
    var customList by remember { mutableStateOf(UaStore.customList(context)) }
    var customSelected by remember { mutableStateOf(UaStore.customSelected(context)) }
    var desktopIndex by remember { mutableStateOf(UaStore.desktopIndex(context)) }
    var input by remember { mutableStateOf("") }
    var desktopMenu by remember { mutableStateOf(false) }
    val bg = M3MaterialTheme.colorScheme.surface

    M3Scaffold(
        containerColor = bg,
        topBar = {
            M3TopAppBar(
                title = { M3Text("浏览器标识") },
                navigationIcon = {
                    M3IconButton(onClick = onBack) {
                        M3Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "返回")
                    }
                },
                colors = M3TopAppBarDefaults.topAppBarColors(containerColor = bg),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).overScrollVertical(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            overscrollEffect = null,
        ) {
            item(key = "preset_header") {
                Text("预设", modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp), fontSize = 13.sp, color = M3MaterialTheme.colorScheme.primary)
            }
            item(key = "preset_card") {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    androidx.compose.material3.Surface(shape = RoundedCornerShape(16.dp), color = bg) {
                        Column {
                            UaStore.presets.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp)
                                        .clickable {
                                            preset = index
                                            customSelected = null
                                            UaStore.setPresetIndex(context, index)
                                            UaStore.setCustomSelected(context, null)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    M3Text(item.name, modifier = Modifier.weight(1f))
                                    M3RadioButton(selected = customSelected == null && preset == index, onClick = null)
                                }
                            }
                        }
                    }
                }
            }
            item(key = "custom_header") {
                Text("自定义", modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp), fontSize = 13.sp, color = M3MaterialTheme.colorScheme.primary)
            }
            item(key = "custom_card") {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    androidx.compose.material3.Surface(shape = RoundedCornerShape(16.dp), color = bg) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            androidx.compose.material3.OutlinedTextField(
                                value = input,
                                onValueChange = { input = it },
                                label = { M3Text("User-Agent") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                androidx.compose.material3.TextButton(onClick = {
                                    val ua = input.trim()
                                    if (ua.isNotBlank() && ua !in customList) {
                                        customList = customList + ua
                                        UaStore.setCustomList(context, customList)
                                        input = ""
                                    }
                                }) { M3Text("添加") }
                            }
                        }
                    }
                    if (customList.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            customList.forEach { ua ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp)
                                        .clickable {
                                            customSelected = ua
                                            UaStore.setCustomSelected(context, ua)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    M3Text(ua, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    M3RadioButton(selected = customSelected == ua, onClick = null)
                                }
                            }
                        }
                    }
                }
            }
            item(key = "other_header") {
                Text("其它", modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp), fontSize = 13.sp, color = M3MaterialTheme.colorScheme.primary)
            }
            item(key = "other_card") {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    androidx.compose.material3.Surface(shape = RoundedCornerShape(16.dp), color = bg) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clickable { desktopMenu = true }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            M3Text("电脑模式下的浏览器标识", modifier = Modifier.weight(1f))
                            M3Text(UaStore.desktopOptions[desktopIndex], color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    M3DropdownMenu(expanded = desktopMenu, onDismissRequest = { desktopMenu = false }) {
                        UaStore.desktopOptions.forEachIndexed { index, label ->
                            M3DropdownMenuItem(
                                text = { M3Text(label) },
                                onClick = {
                                    desktopIndex = index
                                    UaStore.setDesktopIndex(context, index)
                                    desktopMenu = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
