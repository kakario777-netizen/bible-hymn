package com.kakario.biblehymn.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.kakario.biblehymn.R
import com.kakario.biblehymn.data.Prefs

/** 첫 화면: 성경 / 찬송가 / 내 영의 노래 */
@Composable
fun HomeScreen(
    prefs: Prefs,
    onBible: () -> Unit,
    onContinueBible: (book: Int, chapter: Int, verse: Int) -> Unit,
    onBookmarks: () -> Unit,
    onHymn: () -> Unit,
    onMySoul: () -> Unit,
) {
    val context = LocalContext.current
    var subtitle by remember { mutableStateOf(prefs.homeSubtitle) }
    var editSubtitle by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "성경 · 찬송",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { editSubtitle = true }) {
                Icon(Icons.Filled.Settings, contentDescription = "설정", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (subtitle.isNotBlank()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, end = 12.dp),
            )
        }
        Spacer(Modifier.height(22.dp))

        Column(Modifier.padding(end = 12.dp)) {
            MenuCard(
                title = "성경",
                subtitle = "개역한글 · 구약 39권 · 신약 27권",
                mark = "†",
                container = MaterialTheme.colorScheme.primaryContainer,
                onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onBible,
                titleExtra = {
                    StrongChip(R.drawable.ic_bookmark_continue, "이어읽기") {
                        val b = prefs.lastBook
                        if (b in 1..66) onContinueBible(b, prefs.lastChapter, prefs.lastVerse)
                        else Toast.makeText(context, "저장된 이어읽기 위치가 없어요", Toast.LENGTH_SHORT).show()
                    }
                    Spacer(Modifier.width(6.dp))
                    StrongChip(R.drawable.ic_bookmark, "책갈피", onBookmarks)
                },
            )

            Spacer(Modifier.height(14.dp))

            MenuCard(
                title = "찬송가",
                subtitle = "통일찬송가 558장 · 악보",
                mark = "♪",
                container = MaterialTheme.colorScheme.secondaryContainer,
                onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onHymn,
            )

            Spacer(Modifier.height(14.dp))

            MySoulCard(onClick = onMySoul)
        }
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

@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    mark: String,
    container: Color,
    onContainer: Color,
    onClick: () -> Unit,
    titleExtra: @Composable () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    titleExtra()
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                    color = onContainer.copy(alpha = 0.75f),
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(contentAlignment = Alignment.Center) {
                Text(mark, fontSize = 40.sp, color = onContainer.copy(alpha = 0.35f))
            }
        }
    }
}

private val MySoulBlue = Color(0xFF0B3394)

/** 내 영의 노래 카드: 파란 바탕, 제목 위 물결 마크, 오른쪽 피아노줄 그림 */
@Composable
private fun MySoulCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MySoulBlue, contentColor = Color.White),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Swoosh(Modifier.width(150.dp).height(12.dp))
                Text("내 영의 노래", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                Text(
                    "417장 · 악보",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
            Spacer(Modifier.width(8.dp))
            PianoStrings(Modifier.width(34.dp).height(72.dp))
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
        // (줄 번호, 높이 비율) — 원본 표지처럼 아래쪽에 흩어진 별
        val stars = listOf(8 to 0.45f, 2 to 0.58f, 2 to 0.72f, 5 to 0.80f, 8 to 0.86f, 7 to 0.92f, 4 to 0.97f)
        stars.forEach { (line, y) -> sparkle(Offset(line * gap, size.height * y), 3.2.dp.toPx()) }
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
