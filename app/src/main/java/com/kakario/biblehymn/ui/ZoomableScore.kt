package com.kakario.biblehymn.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
fun ZoomableScore(pages: List<String>, modifier: Modifier = Modifier) {
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
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { tap ->
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
        Column(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .verticalScroll(scroll)
                .padding(vertical = 4.dp),
        ) {
            pages.forEach { ScorePage(it) }
        }
    }
}

@Composable
private fun ScorePage(path: String) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(ScoreImages.cached(path), path) {
        if (value == null) value = withContext(Dispatchers.IO) { ScoreImages.load(context, path) }
    }
    val b = bitmap
    if (b == null) {
        Box(Modifier.fillMaxWidth().aspectRatio(0.75f), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Image(
            bitmap = b,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            filterQuality = FilterQuality.High,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(b.width.toFloat() / b.height.toFloat()),
        )
    }
}
