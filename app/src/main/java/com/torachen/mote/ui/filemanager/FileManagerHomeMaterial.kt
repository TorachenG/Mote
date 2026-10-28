package com.torachen.mote.ui.filemanager

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torachen.mote.ui.FileManagerController
import com.torachen.mote.ui.FileManagerPrefs
import com.torachen.mote.ui.M3SegmentArrowItem
import com.torachen.mote.ui.MoteSegmentedColumn
import com.torachen.mote.ui.fmCategories
import com.torachen.mote.ui.fmFormatGb
import com.torachen.mote.ui.fmStorageUsage
import com.torachen.mote.ui.rememberRemoteIcon
import com.torachen.mote.ui.requestAllFiles
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.More

/** 文件管理首页的 Material 版本（列表卡片沿用设置主页的 MoteSegmentedColumn 体系） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FileManagerHomeMaterial(
    controller: FileManagerController,
    onOpenFileSettings: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val surface = MaterialTheme.colorScheme.surface
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val storage = remember { fmStorageUsage() }
    val apps = remember { fmDetectedApps(context) }
    var showMoreMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(surface)) {
        TopAppBar(
            title = { Text("文件管理") },
            navigationIcon = {
                IconButton(onClick = { if (!controller.onBackPressed()) controller.onBack() }) {
                    Icon(MiuixIcons.Back, contentDescription = "返回")
                }
            },
            actions = {
                Box {
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(MiuixIcons.More, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("文件管理设置") },
                            onClick = {
                                showMoreMenu = false
                                onOpenFileSettings()
                            },
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = surface,
                scrolledContainerColor = surface,
            ),
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = navBottom + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (controller.needsAllFiles) {
                item(key = "all_files") {
                    MoteSegmentedColumn(
                        content = listOf {
                            M3SegmentArrowItem(
                                title = "受系统限制，Mote 需要「所有文件访问权限」才能显示全部文件",
                                onClick = { requestAllFiles(context) },
                            )
                        },
                    )
                }
            }

            item(key = "storage") {
                val used = storage.first
                val total = storage.second
                val fraction = if (total > 0) (used.toFloat() / total).coerceIn(0f, 1f) else 0f
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier.size(56.dp),
                                strokeWidth = 5.dp,
                            )
                            Text(
                                text = "${(fraction * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = fmStorageHint(fraction),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "已占用 ${fmFormatGb(used)} GB / ${fmFormatGb(total)} GB" +
                                    "（可用 ${fmFormatGb((total - used).coerceAtLeast(0))} GB）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item(key = "roots") {
                val rootItems = buildList<@Composable () -> Unit> {
                    add {
                        M3SegmentArrowItem(
                            title = "手机存储目录",
                            onClick = {
                                controller.onOpenDir(
                                    controller.root.absolutePath,
                                    null,
                                    "手机存储目录",
                                    false,
                                )
                            },
                            leading = {
                                FileManagerMaterialIconBox(
                                    color = MaterialTheme.colorScheme.primary,
                                    icon = Icons.Rounded.PhoneAndroid,
                                )
                            },
                        )
                    }
                    if (FileManagerPrefs.recycleEnabled) {
                        add {
                            M3SegmentArrowItem(
                                title = "回收站",
                                onClick = {
                                    controller.onOpenDir(
                                        controller.recycleRoot.absolutePath,
                                        null,
                                        "回收站",
                                        false,
                                    )
                                },
                                leading = {
                                    FileManagerMaterialIconBox(
                                        color = Color(0xFFE8452C),
                                        icon = Icons.Rounded.Delete,
                                    )
                                },
                            )
                        }
                    }
                }
                MoteSegmentedColumn(content = rootItems)
            }

            item(key = "categories_header") {
                FileManagerMaterialSectionTitle("文件分类")
            }

            item(key = "categories") {
                MoteSegmentedColumn(
                    content = fmCategories.map { entry ->
                        {
                            M3SegmentArrowItem(
                                title = entry.category.label,
                                onClick = {
                                    controller.onOpenDir(
                                        controller.root.absolutePath,
                                        entry.category.name,
                                        entry.category.label,
                                        false,
                                    )
                                },
                                leading = {
                                    FileManagerMaterialIconBox(
                                        color = if (entry.usePrimary) MaterialTheme.colorScheme.primary
                                        else entry.color,
                                        icon = entry.icon,
                                    )
                                },
                            )
                        }
                    },
                )
            }

            if (apps.isNotEmpty()) {
                item(key = "apps_header") {
                    FileManagerMaterialSectionTitle("应用文件")
                }
                item(key = "apps") {
                    MoteSegmentedColumn(
                        content = apps.map { app ->
                            {
                                val logo = rememberRemoteIcon(app.iconUrl)
                                M3SegmentArrowItem(
                                    title = app.name,
                                    onClick = {
                                        val exists = java.io.File(app.dirPath).exists()
                                        when {
                                            app.restricted -> controller.onOpenDir(
                                                app.dirPath, null, app.name, true,
                                            )
                                            exists -> controller.onOpenDir(
                                                app.dirPath, null, app.name, true,
                                            )
                                            else -> android.widget.Toast.makeText(
                                                controller.context,
                                                "无法访问 ${app.name} 的目录",
                                                android.widget.Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    },
                                    leading = {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(9.dp)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (logo != null) {
                                                Image(
                                                    bitmap = logo,
                                                    contentDescription = app.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize(),
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(app.color),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = app.name.take(1),
                                                        fontSize = 15.sp,
                                                        color = Color.White,
                                                    )
                                                }
                                            }
                                        }
                                    },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FileManagerMaterialSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun FileManagerMaterialIconBox(
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}
