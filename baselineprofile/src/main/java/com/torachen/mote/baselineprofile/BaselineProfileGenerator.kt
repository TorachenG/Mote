package com.torachen.mote.baselineprofile

import android.os.SystemClock
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 纯 instrumentation 版 Baseline Profile 采集器（不依赖 Gradle 的 connectedAndroidTest）。
 *
 * 手动执行：
 *   adb shell am instrument -w -r \
 *     -e targetAppId com.torachen.mote \
 *     -e class com.torachen.mote.baselineprofile.BaselineProfileGenerator \
 *     com.torachen.mote.baselineprofile/androidx.test.runner.AndroidJUnitRunner
 *
 * 产出（默认写在测试包的外部媒体目录）：
 *   /sdcard/Android/media/com.torachen.mote.baselineprofile/
 *     BaselineProfileGenerator_generate-baseline-prof.txt      <- 全路径 baseline
 *     BaselineProfileGenerator_startupProfile-startup-prof.txt <- 纯冷启动 startup
 *
 * 设计原则：
 *  1. 每一段 UI 操作都必须独立 try-catch，任何一步失败都不能让整个 collect() 抛异常，
 *     否则 BaselineProfileRule 会直接不落盘（表现就是"跑完了但没有文件"）。
 *  2. 所有查找都带超时轮询，绝不 "findObject 立刻点"。
 *  3. 只做"打开/滚动/返回"这类只读操作，不修改任何用户设置（不动开关、不输入文字、不点删除）。
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    /** am instrument 的 -e targetAppId 参数；Gradle 任务会自动注入。 */
    private val targetAppId: String
        get() = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: DEFAULT_TARGET_APP_ID

    /**
     * 全路径 baseline profile：冷启动 -> 等首屏 -> 工具栏 -> 设置 -> 滑动 -> 返回 -> 网页滑动。
     * 产出 xxx-baseline-prof.txt。
     */
    @Test
    fun generate() {
        var round = 0
        rule.collect(
            packageName = targetAppId,
            includeInStartupProfile = false,
        ) {
            val firstRound = round++ == 0
            log("=== generate() round=${round} pkg=$packageName ===")

            // 1. 冷启动 + 等首屏真正画完
            coldStart(settleMs = if (firstRound) SETTLE_FIRST_ROUND_MS else SETTLE_OTHER_ROUND_MS)

            // 2. 工具栏 -> 设置 -> 滑动 -> 返回
            var settingsOpened = false
            val toolSheetOpened = step("tap-toolbar") {
                tapAny(
                    device, TAP_TIMEOUT_MS,
                    By.desc("工具栏"),
                    By.descContains("工具栏"),
                    By.textContains("工具栏"),
                )
            } ?: false

            if (toolSheetOpened) {
                // 等 sheet 弹出动画结束（Compose AnimatedVisibility）
                device.waitForIdle(SHEET_IDLE_MS)
                sleep(600)

                settingsOpened = step("tap-settings") {
                    tapAny(
                        device, TAP_TIMEOUT_MS,
                        By.desc("设置"),
                        By.text("设置"),
                        By.descContains("设置"),
                    )
                } ?: false
            }

            if (settingsOpened) {
                // 等设置页入场动画（slideInVertically）
                sleep(SETTINGS_ENTER_MS)
                device.waitForIdle(SHEET_IDLE_MS)

                step("scroll-settings") {
                    repeat(3) {
                        device.swipe(
                            device.displayWidth / 2, (device.displayHeight * 0.75f).toInt(),
                            device.displayWidth / 2, (device.displayHeight * 0.30f).toInt(),
                            SWIPE_STEPS,
                        )
                        sleep(SWIPE_SETTLE_MS)
                    }
                    // 回滑一次，覆盖反向滚动路径
                    device.swipe(
                        device.displayWidth / 2, (device.displayHeight * 0.30f).toInt(),
                        device.displayWidth / 2, (device.displayHeight * 0.75f).toInt(),
                        SWIPE_STEPS,
                    )
                    sleep(SWIPE_SETTLE_MS)
                }

                // 设置页由 BackHandler 处理，返回浏览器（不会退出 App）
                step("back-from-settings") {
                    device.pressBack()
                    sleep(BACK_SETTLE_MS)
                }
            }

            // 3. 网页区域滑动：触发 WebView / 手势 / 滚动相关的类加载
            step("scroll-page") {
                repeat(2) {
                    device.swipe(
                        device.displayWidth / 2, (device.displayHeight * 0.70f).toInt(),
                        device.displayWidth / 2, (device.displayHeight * 0.35f).toInt(),
                        SWIPE_STEPS,
                    )
                    sleep(SWIPE_SETTLE_MS)
                }
            }

            // 4. 书签列表 / 历史记录 / 下载与文件管理：把这些界面的首帧与滚动路径也收进 profile
            step("bookmarks") {
                if (openToolSheet(device)) {
                    tapAny(device, TAP_TIMEOUT_MS, By.textContains("书签列表"), By.descContains("书签列表"))
                    device.waitForIdle(SHEET_IDLE_MS)
                    swipeUpDown(device, 2)
                    device.pressBack()
                    sleep(BACK_SETTLE_MS)
                }
            }
            step("history") {
                if (openToolSheet(device)) {
                    tapAny(device, TAP_TIMEOUT_MS, By.textContains("历史记录"), By.descContains("历史记录"))
                    device.waitForIdle(SHEET_IDLE_MS)
                    swipeUpDown(device, 2)
                    device.pressBack()
                    sleep(BACK_SETTLE_MS)
                }
            }
            step("downloads-and-files") {
                if (openToolSheet(device)) {
                    tapAny(device, TAP_TIMEOUT_MS, By.textContains("下载管理"), By.descContains("下载管理"))
                    device.waitForIdle(SHEET_IDLE_MS)
                    sleep(800)
                    swipeUpDown(device, 2)
                    // 切到文件页签
                    tapAny(device, TAP_TIMEOUT_MS, By.textContains("文件"), By.descContains("文件"))
                    device.waitForIdle(SHEET_IDLE_MS)
                    sleep(800)
                    swipeUpDown(device, 3)
                    device.pressBack()
                    sleep(BACK_SETTLE_MS)
                }
            }

            device.waitForIdle(SHEET_IDLE_MS)
            log("=== generate() round=${round} done (toolSheet=$toolSheetOpened settings=$settingsOpened) ===")
        }
    }

    /**
     * 只采冷启动路径，作为 startup profile（决定 primary dex 布局）。
     * 产出 xxx-startup-prof.txt。
     */
    @Test
    fun startupProfile() {
        rule.collect(
            packageName = targetAppId,
            includeInStartupProfile = true,
        ) {
            log("=== startupProfile() pkg=$packageName ===")
            coldStart(settleMs = SETTLE_FIRST_ROUND_MS)
            device.waitForIdle(SHEET_IDLE_MS)
        }
    }

    // ------------------------------------------------------------------ 核心步骤

    /**
     * 一次真正的冷启动：回桌面 -> 拉起 App -> 等首帧 -> 静置 settleMs。
     * 静置是必须的：Compose 首帧之后的异步初始化（SharedPreferences、WebView 首包、
     * 主题/字体加载）才是冷启动耗时的大头，profile 必须覆盖到它们。
     */
    private fun MacrobenchmarkScope.coldStart(settleMs: Long) {
        step("pressHome+startActivityAndWait") {
            pressHome()
            device.waitForIdle(2_000)
            startActivityAndWait()
        }

        // 主窗口出现（launcher 还没让位时继续等）
        step("wait-first-window") {
            device.wait(Until.hasObject(By.pkg(packageName).depth(0)), WINDOW_TIMEOUT_MS)
        }
        device.waitForIdle(SHEET_IDLE_MS)
        sleep(settleMs)
    }

    // ------------------------------------------------------------------ 工具方法

    /**
     * 轮询多个选择器，任一命中就点，返回是否点成功。
     * 用轮询而不是 findObject 立即点，避免 HyperOS 上首帧未稳定时的 StaleObjectException。
     */
    /** 打开工具弹窗，返回是否成功 */
    private fun openToolSheet(device: UiDevice): Boolean {
        val opened = tapAny(
            device, TAP_TIMEOUT_MS,
            By.descContains("工具"),
            By.textContains("工具"),
        )
        if (opened) {
            device.waitForIdle(SHEET_IDLE_MS)
            sleep(600)
        }
        return opened
    }

    /** 上下往返滑动，覆盖反向滚动路径 */
    private fun swipeUpDown(device: UiDevice, times: Int) {
        repeat(times) {
            device.swipe(
                device.displayWidth / 2, (device.displayHeight * 0.75f).toInt(),
                device.displayWidth / 2, (device.displayHeight * 0.35f).toInt(),
                SWIPE_STEPS,
            )
            sleep(SWIPE_SETTLE_MS)
        }
        device.swipe(
            device.displayWidth / 2, (device.displayHeight * 0.35f).toInt(),
            device.displayWidth / 2, (device.displayHeight * 0.75f).toInt(),
            SWIPE_STEPS,
        )
        sleep(SWIPE_SETTLE_MS)
    }

    private fun tapAny(device: UiDevice, timeoutMs: Long, vararg selectors: BySelector): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            for (selector in selectors) {
                val node = try {
                    device.findObject(selector)
                } catch (e: Exception) {
                    log("findObject failed: ${e.javaClass.simpleName}")
                    null
                } ?: continue

                val clicked = try {
                    node.click()
                    true
                } catch (e: Exception) {
                    log("click failed: ${e.javaClass.simpleName}")
                    false
                }
                if (clicked) {
                    device.waitForIdle(500)
                    return true
                }
            }
            sleep(250)
        }
        return false
    }

    private companion object {
        const val DEFAULT_TARGET_APP_ID = "com.torachen.mote"

        const val WINDOW_TIMEOUT_MS = 10_000L
        const val TAP_TIMEOUT_MS = 4_000L
        const val SHEET_IDLE_MS = 1_000L
        const val SETTINGS_ENTER_MS = 1_200L
        const val BACK_SETTLE_MS = 1_200L
        const val SWIPE_SETTLE_MS = 400L
        const val SWIPE_STEPS = 24

        /** 首轮冷启动多等，保证首屏 + WebView 预热全部落进 profile；后续轮次缩短总耗时。 */
        const val SETTLE_FIRST_ROUND_MS = 3_000L
        const val SETTLE_OTHER_ROUND_MS = 1_200L
    }
}

// ---------------------------------------------------------------------- 顶层健壮性工具

private const val LOG_TAG = "BaselineProfileGen"

private fun log(msg: String) {
    // println 会被 am instrument -r 捕获，也能在 logcat (System.out) 里看到
    println("[$LOG_TAG] $msg")
}

private fun sleep(ms: Long) {
    if (ms <= 0) return
    try {
        Thread.sleep(ms)
    } catch (_: InterruptedException) {
        // instrumentation 被中断（超时/手动停止）时不要吞掉状态
        Thread.currentThread().interrupt()
    }
}

/**
 * 单步隔离：任何异常都只让当前这一步跳过，绝不让 collect() 整体失败。
 * collect() 一旦抛异常就完全不写 profile 文件，这是最容易踩的坑。
 */
private inline fun <T> step(tag: String, block: () -> T): T? {
    return try {
        block()
    } catch (e: Exception) {
        log("skip <$tag>: ${e.javaClass.simpleName}: ${e.message}")
        null
    }
}
