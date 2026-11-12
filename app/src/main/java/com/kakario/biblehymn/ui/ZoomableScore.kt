package com.kakario.biblehymn.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kakario.biblehymn.data.ScoreImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 악보 보기: 화면 너비에 맞춰 세로로 이어 붙이고,
 * 두 손가락으로 확대/축소, 두 번 탭하면 확대 ↔ 원래대로.
 * 확대하지 않은 상태에서는 한 손가락 좌우 스와이프가 옆 곡으로 넘어간다.
 */
@Composable
fun ZoomableScore(
    pages: List<String>,
    fitScreen: Boolean,
    modifier: Modifier = Modifier,
    /** 한 번 터치: -1 = 왼쪽, 0 = 가운데, +1 = 오른쪽 (확대 안 했을 때만) */
    onTapRegion: ((Int) -> Unit)? = null,
) {
    val context = LocalContext.current
    val bitmaps by produceState(initialValue = pages.map { ScoreImages.cached(it) }, pages) {
        if (value.any { it == null }) {
            value = withContext(Dispatchers.IO) { pages.map { ScoreImages.load(context, it) } }
        }
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val scroll = rememberScrollState()

    fun clamp(o: Offset, s: Float): Offset {
        val maxX = size.width * (s - 1f) / 2f
        val maxY = size.height * (s - 1f) / 2f
        return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
    }

    Box(
        modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.White)
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val fingers = event.changes.count { it.pressed }
                        if (fingers >= 2 || scale > 1.01f) {
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            val newScale = (scale * zoom).coerceIn(1f, 4f)
                            if (newScale <= 1.01f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                val target = offset + pan
                                val clamped = clamp(target, newScale)
                                // 위아래 끝에 닿으면 남은 만큼은 악보를 스크롤
                                val overflowY = target.y - clamped.y
                                if (overflowY != 0f) scroll.dispatchRawDelta(-overflowY / newScale)
                                scale = newScale
                                offset = clamped
                            }
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(onTapRegion != null) {
                detectTapGestures(
                    onTap = { tap ->
                        val cb = onTapRegion
                        if (cb != null && scale <= 1.01f) {
                            when {
                                tap.x < size.width * 0.3f -> cb(-1)
                                tap.x > size.width * 0.7f -> cb(1)
                                else -> cb(0)
                            }
                        }
                    },
                    onDoubleTap = { tap ->
                    if (scale > 1.01f) {
                        scale = 1f
                        offset = Offset.Zero
                    } else {
                        val s = 2.2f
                        scale = s
                        offset = clamp(
                            Offset((size.width / 2f - tap.x) * (s - 1f), (size.height / 2f - tap.y) * (s - 1f)),
                            s,
                        )
                    }
                })
            },
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        ) {
            val loaded = bitmaps.filterNotNull()
            val boxWidth = maxWidth
            val boxHeight = maxHeight
            if (loaded.size < pages.size) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                // 화면 너비에 맞췄을 때 전체 높이 → 화면보다 크면(설정 켰을 때) 한 화면에 들어오게 축소
                val totalHeight = loaded.sumOf { (boxWidth.value * it.height / it.width).toDouble() }.toFloat()
                val shrink = if (fitScreen && totalHeight > boxHeight.value) boxHeight.value / totalHeight else 1f
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    loaded.forEach { b ->
                        Image(
                            bitmap = b,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            filterQuality = FilterQuality.High,
                            colorFilter = if (IsEink) EinkScoreFilter else null,
                            modifier = Modifier
                                .width(boxWidth * shrink)
                                .aspectRatio(b.width.toFloat() / b.height.toFloat()),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 이북판 악보: 파일은 그대로 두고 화면에 그릴 때만
 * 흑백으로 바꾸고, 밝은 바탕(누런 종이색 등)은 완전 흰색으로 날린다.
 * 밝기 200 이상 → 흰색, 25 이하 → 검은색, 그 사이는 늘려서 진하게.
 */
private val EinkScoreFilter: androidx.compose.ui.graphics.ColorFilter = run {
    val lo = 25f
    val hi = 200f
    val k = 255f / (hi - lo)
    val b = -k * lo
    val r = 0.299f * k
    val g = 0.587f * k
    val bl = 0.114f * k
    androidx.compose.ui.graphics.ColorFilter.colorMatrix(
        androidx.compose.ui.graphics.ColorMatrix(
            floatArrayOf(
                r, g, bl, 0f, b,
                r, g, bl, 0f, b,
                r, g, bl, 0f, b,
                0f, 0f, 0f, 1f, 0f,
            )
        )
    )
}
