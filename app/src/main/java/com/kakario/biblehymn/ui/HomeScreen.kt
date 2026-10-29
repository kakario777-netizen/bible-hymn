package com.kakario.biblehymn.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakario.biblehymn.R
import com.kakario.biblehymn.data.Prefs

/** 첫 화면: 성경 / 찬송가 / 내 영의 노래 */
@Composable
fun HomeScreen(
    prefs: Prefs,
    themeMode: Int,
    onThemeMode: (Int) -> Unit,
    onBible: () -> Unit,
    onContinueBible: (book: Int, chapter: Int, verse: Int) -> Unit,
    onBookmarks: () -> Unit,
    onHymn: () -> Unit,
    onMySoul: () -> Unit,
) {
    val context = LocalContext.current
    var subtitle by remember { mutableStateOf(prefs.homeSubtitle) }
    var showSettings by remember { mutableStateOf(false) }
    var editSubtitle by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "성경 · 찬송",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { showSettings = true }) {
                Icon(Icons.Filled.Settings, contentDescription = "설정", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (subtitle.isNotBlank()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Spacer(Modifier.height(18.dp))

        // 카드 3개가 화면의 대부분(약 4/5)을 채우도록 높이를 나눠 가짐
        BibleCard(
            modifier = Modifier.weight(1f).heightIn(min = 120.dp),
            onClick = onBible,
            onContinue = {
                val b = prefs.lastBook
                if (b in 1..66) onContinueBible(b, prefs.lastChapter, prefs.lastVerse)
                else Toast.makeText(context, "저장된 이어읽기 위치가 없어요", Toast.LENGTH_SHORT).show()
            },
            onBookmarks = onBookmarks,
        )
        Spacer(Modifier.height(14.dp))
        HymnCard(modifier = Modifier.weight(1f).heightIn(min = 120.dp), onClick = onHymn)
        Spacer(Modifier.height(14.dp))
        MySoulCard(modifier = Modifier.weight(1f).heightIn(min = 120.dp), onClick = onMySoul)
        Spacer(Modifier.weight(0.3f))
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("설정") },
            text = {
                Column {
                    Text("배경색", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    listOf(0 to "시스템 설정", 1 to "흰색", 2 to "검은색").forEach { (mode, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onThemeMode(mode) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = themeMode == mode, onClick = { onThemeMode(mode) })
                            Text(label, fontSize = 16.sp)
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    Text("부제목", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { editSubtitle = true }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            subtitle.ifBlank { "제목 밑에 넣을 글 (비어 있음)" },
                            fontSize = 15.sp,
                            color = if (subtitle.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Text("편집", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSettings = false }) { Text("닫기") } },
        )
    }

    if (editSubtitle) {
        TextInputDialog(
            title = "제목 밑 부제목",
            initial = subtitle,
            placeholder = "예) 말씀과 찬양으로 하루를",
            onDismiss = { editSubtitle = false },
            onSave = {
                prefs.homeSubtitle = it
                subtitle = it
                editSubtitle = false
            },
        )
    }
}

/** 진한 색 작은 버튼 (책갈피 그림 + 글자) */
@Composable
private fun StrongChip(icon: Int, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Row(
            Modifier.padding(start = 9.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

// ───────────────────────── 성경 카드 ─────────────────────────

/** 성경: 가죽 성경 표지처럼 가장자리 스티치 + 오른쪽에 반투명 스티치 성경책 */
@Composable
private fun BibleCard(
    modifier: Modifier,
    onClick: () -> Unit,
    onContinue: () -> Unit,
    onBookmarks: () -> Unit,
) {
    val on = MaterialTheme.colorScheme.onPrimaryContainer
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = on),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind { stitchBorder(on.copy(alpha = 0.28f), inset = 7.dp.toPx(), radius = 16.dp.toPx()) },
        ) {
            StitchedBible(
                color = on,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 22.dp)
                    .width(78.dp)
                    .height(100.dp),
            )
            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp, end = 110.dp),
            ) {
                Text("성경", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text(
                    "개역한글 · 구약 39권 · 신약 27권",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                    color = on.copy(alpha = 0.75f),
                )
                Row {
                    StrongChip(R.drawable.ic_bookmark_continue, "이어읽기", onContinue)
                    Spacer(Modifier.width(6.dp))
                    StrongChip(R.drawable.ic_bookmark, "책갈피", onBookmarks)
                }
            }
        }
    }
}

/** 카드 가장자리를 따라 도는 점선 박음질 */
private fun DrawScope.stitchBorder(color: Color, inset: Float, radius: Float) {
    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - inset * 2, size.height - inset * 2),
        cornerRadius = CornerRadius(radius),
        style = Stroke(
            width = 1.4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 3.5.dp.toPx())),
        ),
    )
}

/** 반투명 가죽 성경책: 책등, 스티치 테두리, 표지 십자가 */
@Composable
private fun StitchedBible(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val r = 7.dp.toPx()
        val spine = w * 0.16f
        // 표지 (투명하게)
        drawRoundRect(color.copy(alpha = 0.10f), cornerRadius = CornerRadius(r))
        drawRoundRect(color.copy(alpha = 0.22f), cornerRadius = CornerRadius(r), style = Stroke(1.2.dp.toPx()))
        // 책등
        drawRoundRect(
            color.copy(alpha = 0.10f),
            size = Size(spine, h),
            cornerRadius = CornerRadius(r),
        )
        drawLine(color.copy(alpha = 0.22f), Offset(spine, 0f), Offset(spine, h), strokeWidth = 1.dp.toPx())
        // 책등 가로 줄
        listOf(0.14f, 0.20f, 0.80f, 0.86f).forEach { y ->
            drawLine(color.copy(alpha = 0.22f), Offset(2.dp.toPx(), h * y), Offset(spine - 2.dp.toPx(), h * y), strokeWidth = 1.dp.toPx())
        }
        // 표지 스티치
        val ins = 5.dp.toPx()
        drawRoundRect(
            color.copy(alpha = 0.38f),
            topLeft = Offset(spine + ins, ins),
            size = Size(w - spine - ins * 2, h - ins * 2),
            cornerRadius = CornerRadius(r * 0.6f),
            style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.5.dp.toPx()))),
        )
        // 십자가
        val cx = spine + (w - spine) / 2f
        val cy = h * 0.42f
        val arm = 2.6.dp.toPx()
        val c = color.copy(alpha = 0.40f)
        drawRoundRect(c, topLeft = Offset(cx - arm / 2, cy - h * 0.17f), size = Size(arm, h * 0.36f), cornerRadius = CornerRadius(arm / 2))
        drawRoundRect(c, topLeft = Offset(cx - w * 0.17f, cy - h * 0.08f), size = Size(w * 0.34f, arm), cornerRadius = CornerRadius(arm / 2))
        // 지퍼 손잡이 느낌의 작은 고리
        drawRoundRect(
            color.copy(alpha = 0.30f),
            topLeft = Offset(spine * 0.25f, h * 0.88f),
            size = Size(spine * 0.5f, h * 0.10f),
            cornerRadius = CornerRadius(2.dp.toPx()),
        )
    }
}

// ───────────────────────── 찬송가 카드 ─────────────────────────

/** 찬송가: 가운데 위쪽에 반투명 피아노 건반(도~솔, 검은건반 포함) */
@Composable
private fun HymnCard(modifier: Modifier, onClick: () -> Unit) {
    val on = MaterialTheme.colorScheme.onSecondaryContainer
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = on),
    ) {
        Box(Modifier.fillMaxSize()) {
            PianoKeys(
                color = on,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp)
                    .width(150.dp)
                    .height(54.dp),
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 20.dp, end = 24.dp),
            ) {
                Text("찬송가", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text(
                    "통일찬송가 558장 · 악보",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                    color = on.copy(alpha = 0.75f),
                )
            }
        }
    }
}

/** 흰 건반 5개(도레미파솔) + 검은 건반 3개(도#·레#·파#) — 반투명 */
@Composable
private fun PianoKeys(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val n = 5
        val gap = 2.dp.toPx()
        val kw = (size.width - gap * (n - 1)) / n
        val kh = size.height
        val r = 5.dp.toPx()
        for (i in 0 until n) {
            val x = i * (kw + gap)
            val path = Path().apply {
                // 위는 각지고 아래만 둥근 건반
                moveTo(x, 0f)
                lineTo(x + kw, 0f)
                lineTo(x + kw, kh - r)
                quadraticBezierTo(x + kw, kh, x + kw - r, kh)
                lineTo(x + r, kh)
                quadraticBezierTo(x, kh, x, kh - r)
                close()
            }
            drawPath(path, Color.White.copy(alpha = 0.55f))
            drawPath(path, color.copy(alpha = 0.22f), style = Stroke(1.dp.toPx()))
        }
        // 검은 건반: 도-레, 레-미, 파-솔 사이
        val bw = kw * 0.58f
        val bh = kh * 0.60f
        listOf(1, 2, 4).forEach { k ->
            val cx = k * (kw + gap) - gap / 2
            drawRoundRect(
                color.copy(alpha = 0.55f),
                topLeft = Offset(cx - bw / 2, 0f),
                size = Size(bw, bh),
                cornerRadius = CornerRadius(3.dp.toPx()),
            )
        }
    }
}

// ───────────────────────── 내 영의 노래 카드 ─────────────────────────

private val MySoulBlue = Color(0xFF0B3394)

/** 내 영의 노래 카드: 파란 바탕, 제목 위 물결 마크, 오른쪽 피아노줄 그림 */
@Composable
private fun MySoulCard(modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MySoulBlue, contentColor = Color.White),
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Swoosh(Modifier.width(150.dp).height(12.dp))
                Text("내 영의 노래", fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                Text(
                    "417장 · 악보",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
            Spacer(Modifier.width(8.dp))
            PianoStrings(Modifier.width(38.dp).height(96.dp))
        }
    }
}

/** 제목 위 흰 물결(초승달 모양) 마크 */
@Composable
private fun Swoosh(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val p = Path().apply {
            moveTo(0f, h * 0.95f)
            cubicTo(w * 0.30f, h * 0.05f, w * 0.62f, h * 0.10f, w * 0.80f, h * 0.55f)
            cubicTo(w * 0.88f, h * 0.70f, w * 0.95f, h * 0.35f, w, 0f)
            cubicTo(w * 0.95f, h * 0.60f, w * 0.86f, h * 1.00f, w * 0.76f, h * 0.80f)
            cubicTo(w * 0.60f, h * 0.45f, w * 0.30f, h * 0.45f, 0f, h * 0.95f)
            close()
        }
        drawPath(p, Color.White)
    }
}

/** 피아노줄(세로 가는 줄)과 작은 별 반짝임 */
@Composable
private fun PianoStrings(modifier: Modifier) {
    Canvas(modifier) {
        val lines = 12
        val gap = size.width / (lines - 1)
        val stroke = 0.8.dp.toPx()
        for (i in 0 until lines) {
            val x = i * gap
            drawLine(Color.White.copy(alpha = 0.85f), Offset(x, 0f), Offset(x, size.height), strokeWidth = stroke)
        }
        val stars = listOf(8 to 0.45f, 2 to 0.58f, 2 to 0.72f, 5 to 0.80f, 8 to 0.86f, 7 to 0.92f, 4 to 0.97f)
        stars.forEach { (line, y) -> sparkle(Offset(line * gap, size.height * y), 3.4.dp.toPx()) }
    }
}

private fun DrawScope.sparkle(c: Offset, r: Float) {
    val k = r * 0.28f
    val p = Path().apply {
        moveTo(c.x, c.y - r)
        lineTo(c.x + k, c.y - k)
        lineTo(c.x + r, c.y)
        lineTo(c.x + k, c.y + k)
        lineTo(c.x, c.y + r)
        lineTo(c.x - k, c.y + k)
        lineTo(c.x - r, c.y)
        lineTo(c.x - k, c.y - k)
        close()
    }
    drawPath(p, Color.White)
}
