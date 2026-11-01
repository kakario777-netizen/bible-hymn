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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
    var section by remember { mutableStateOf<String?>(null) }
    var editSubtitle by remember { mutableStateOf(false) }
    var tapPaging by remember { mutableStateOf(prefs.tapPaging) }
    var screenTimeout by remember { mutableStateOf(prefs.screenTimeout) }

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
            onDismissRequest = { showSettings = false; section = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (section != null) {
                        Text(
                            "‹ ",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { section = null },
                        )
                    }
                    Text(section ?: "설정")
                }
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    when (section) {
                        null -> {
                            val themeLabel = listOf("시스템 설정", "흰색", "검은색").getOrElse(themeMode) { "" }
                            val offLabel = listOf("시스템 설정", "30분", "2시간", "안 꺼짐").getOrElse(screenTimeout) { "" }
                            SettingsCategory("화면 색", "배경색 · $themeLabel") { section = "화면 색" }
                            SettingsCategory("읽기 화면", "자동 꺼짐 $offLabel · 좌우 터치 ${if (tapPaging) "켜짐" else "꺼짐"}") { section = "읽기 화면" }
                            SettingsCategory("첫 화면", "부제목 · ${subtitle.ifBlank { "없음" }}") { section = "첫 화면" }
                        }
                        "화면 색" -> {
                            Text("배경색", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            listOf(0 to "시스템 설정", 1 to "흰색", 2 to "검은색").forEach { (mode, label) ->
                                RadioRow(label, themeMode == mode) { onThemeMode(mode) }
                            }
                        }
                        "읽기 화면" -> {
                            Text("화면 자동 꺼짐", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                "성경·악보를 보는 동안, 마지막으로 만진 뒤 이 시간까지 화면이 켜져 있어요",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            listOf(0 to "시스템 설정", 1 to "30분", 2 to "2시간", 3 to "안 꺼짐").forEach { (mode, label) ->
                                RadioRow(label, screenTimeout == mode) {
                                    screenTimeout = mode
                                    prefs.screenTimeout = mode
                                }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 10.dp))
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tapPaging = !tapPaging
                                        prefs.tapPaging = tapPaging
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("좌우 터치로 넘기기", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        "성경·악보 화면 왼쪽/오른쪽을 누르면 이전/다음으로",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(
                                    checked = tapPaging,
                                    onCheckedChange = {
                                        tapPaging = it
                                        prefs.tapPaging = it
                                    },
                                )
                            }
                        }
                        "첫 화면" -> {
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
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSettings = false; section = null }) { Text("닫기") } },
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

/** 성경: 아주 옅은 가죽 질감 + 가장자리 스티치 */
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
                .leatherGrain(on)
                .drawBehind { stitchBorder(on.copy(alpha = 0.28f), inset = 7.dp.toPx(), radius = 16.dp.toPx()) },
        ) {
            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 24.dp),
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

/**
 * 아주 옅은 가죽 질감: 작은 알갱이(밝은 점·어두운 점)를 고르게 흩뿌림.
 * 위치는 항상 같게(고정 시드) 계산해서 한 번만 만든다.
 */
private fun Modifier.leatherGrain(tint: Color): Modifier = drawWithCache {
    val rnd = java.util.Random(7L)
    val density = 1f / (9.dp.toPx() * 9.dp.toPx())
    val count = (size.width * size.height * density).toInt().coerceAtMost(6000)
    val xs = FloatArray(count) { rnd.nextFloat() * size.width }
    val ys = FloatArray(count) { rnd.nextFloat() * size.height }
    val rs = FloatArray(count) { (0.8f + rnd.nextFloat() * 1.8f) * 1.dp.toPx() }
    val dark = BooleanArray(count) { rnd.nextBoolean() }
    // 배경이 어두우면(글자색이 밝으면) 알갱이를 더 옅게
    val darkBg = tint.luminance() > 0.5f
    val darkColor = if (darkBg) Color.Black.copy(alpha = 0.05f) else tint.copy(alpha = 0.035f)
    val lightColor = Color.White.copy(alpha = if (darkBg) 0.025f else 0.06f)
    onDrawBehind {
        for (i in 0 until count) {
            drawCircle(if (dark[i]) darkColor else lightColor, radius = rs[i], center = Offset(xs[i], ys[i]))
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

// ───────────────────────── 찬송가 카드 ─────────────────────────

/** 찬송가: 제목 왼쪽, 음표 오른쪽 */
@Composable
private fun HymnCard(modifier: Modifier, onClick: () -> Unit) {
    val on = MaterialTheme.colorScheme.onSecondaryContainer
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = on),
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("찬송가", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text(
                    "통일찬송가 558장 · 악보",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                    color = on.copy(alpha = 0.75f),
                )
            }
            NoteCluster(on)
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

/** 설정 분류 한 줄 (누르면 한 단계 들어감) */
@Composable
private fun SettingsCategory(title: String, summary: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(summary, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, fontSize = 16.sp)
    }
}

/** 찬송가 카드 오른쪽 음표 묶음: 크기·기울기·진하기를 조금씩 달리해서 */
@Composable
private fun NoteCluster(color: Color) {
    Box(Modifier.size(width = 92.dp, height = 84.dp)) {
        Text(
            "♫",
            fontSize = 50.sp,
            color = color.copy(alpha = 0.34f),
            modifier = Modifier.align(Alignment.Center).offset(x = 6.dp, y = 4.dp),
        )
        Text(
            "♪",
            fontSize = 28.sp,
            color = color.copy(alpha = 0.26f),
            modifier = Modifier.align(Alignment.TopStart).offset(x = 2.dp, y = 0.dp).rotate(-14f),
        )
        Text(
            "♬",
            fontSize = 22.sp,
            color = color.copy(alpha = 0.22f),
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 0.dp, y = (-2).dp).rotate(10f),
        )
        Text(
            "♩",
            fontSize = 18.sp,
            color = color.copy(alpha = 0.18f),
            modifier = Modifier.align(Alignment.BottomStart).offset(x = 10.dp, y = (-6).dp).rotate(-6f),
        )
    }
}
