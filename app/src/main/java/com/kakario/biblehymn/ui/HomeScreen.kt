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

            MenuCard(
                title = "내 영의 노래",
                subtitle = "(준비중)",
                mark = "♡",
                container = MaterialTheme.colorScheme.surfaceVariant,
                onContainer = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {
                    Toast.makeText(context, "내 영의 노래는 준비중입니다", Toast.LENGTH_SHORT).show()
                },
            )
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
