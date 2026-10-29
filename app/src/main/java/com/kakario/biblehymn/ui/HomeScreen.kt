package com.kakario.biblehymn.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakario.biblehymn.data.Bible
import com.kakario.biblehymn.data.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 첫 화면: 성경 / 찬송가 / 내 영의 노래 */
@Composable
fun HomeScreen(
    prefs: Prefs,
    onBible: () -> Unit,
    onContinueBible: (book: Int, chapter: Int) -> Unit,
    onHymn: () -> Unit,
) {
    val context = LocalContext.current
    var continueLabel by remember { mutableStateOf<String?>(null) }
    val lastBook = prefs.lastBook
    val lastChapter = prefs.lastChapter

    LaunchedEffect(lastBook, lastChapter) {
        if (lastBook in 1..66) {
            val name = withContext(Dispatchers.IO) { Bible.book(context, lastBook)?.name }
            continueLabel = name?.let { "$it ${lastChapter}장" }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text(
            "성경 · 찬송",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "무엇을 펼칠까요?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(24.dp))

        MenuCard(
            title = "성경",
            subtitle = "개역한글 · 구약 39권 · 신약 27권",
            mark = "†",
            container = MaterialTheme.colorScheme.primaryContainer,
            onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = onBible,
        ) {
            val label = continueLabel
            if (label != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    onClick = { onContinueBible(lastBook, lastChapter) },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(
                        "이어 읽기 · $label",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }

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

@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    mark: String,
    container: Color,
    onContainer: Color,
    onClick: () -> Unit,
    extra: @Composable ColumnScope.() -> Unit = {},
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
                Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                    color = onContainer.copy(alpha = 0.75f),
                )
                extra()
            }
            Spacer(Modifier.width(12.dp))
            Box(contentAlignment = Alignment.Center) {
                Text(mark, fontSize = 40.sp, color = onContainer.copy(alpha = 0.35f))
            }
        }
    }
}
