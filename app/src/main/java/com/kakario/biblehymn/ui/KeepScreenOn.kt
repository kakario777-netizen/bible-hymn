package com.kakario.biblehymn.ui

import android.app.Activity
import android.os.SystemClock
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

/** 마지막으로 화면을 만진 시각 (MainActivity가 터치마다 갱신) */
object UserActivity {
    @Volatile
    var lastTouch: Long = SystemClock.elapsedRealtime()
}

/**
 * 성경·악보 화면이 떠 있는 동안 화면 꺼짐을 조절한다.
 * mode: 0 = 시스템 설정 그대로, 1 = 30분, 2 = 2시간, 3 = 안 꺼짐
 * 30분/2시간은 '마지막으로 만진 뒤' 그 시간이 지나면 시스템 설정대로 꺼지게 둔다.
 */
@Composable
fun KeepScreenOn(mode: Int) {
    val activity = LocalContext.current as? Activity ?: return
    val window = activity.window
    val limitMs = when (mode) {
        1 -> 30L * 60_000L
        2 -> 120L * 60_000L
        3 -> Long.MAX_VALUE
        else -> 0L
    }
    if (limitMs == 0L) return

    DisposableEffect(window) {
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    LaunchedEffect(limitMs) {
        UserActivity.lastTouch = SystemClock.elapsedRealtime()
        var on = false
        while (true) {
            val idle = SystemClock.elapsedRealtime() - UserActivity.lastTouch
            val keep = idle < limitMs
            if (keep != on) {
                if (keep) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                on = keep
            }
            delay(5_000L)
        }
    }
}
