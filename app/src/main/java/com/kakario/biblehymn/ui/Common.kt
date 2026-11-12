package com.kakario.biblehymn.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.sp

/** 뒤로가기 화살표가 있는 상단바 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
            }
        },
        actions = actions,
    )
}

/** 상단바용 작은 버튼: 그림 + 아래 글자 */
@Composable
fun IconLabelButton(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.widthIn(min = 46.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(painterResource(icon), contentDescription = label, modifier = Modifier.size(22.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

/** 이름(글자) 입력 창: [취소] [저장] */
@Composable
fun TextInputDialog(
    title: String,
    initial: String,
    placeholder: String = "",
    singleLine: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var value by remember { mutableStateOf(TextFieldValue(initial, TextRange(0, initial.length))) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = singleLine,
                placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus),
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
        confirmButton = { TextButton(onClick = { onSave(value.text.trim()) }) { Text("저장") } },
    )
}

// ───────────────────────── 이북판: 슬라이드 없이 즉시 넘김 ─────────────────────────

/** 이북리더기용 빌드인지 */
internal val IsEink: Boolean = com.kakario.biblehymn.BuildConfig.IS_EINK

/** 페이지 이동: 폰판은 미끄러지듯, 이북판은 바로 바뀜 (이잉크 잔상·깜빡임 방지) */
internal suspend fun androidx.compose.foundation.pager.PagerState.goTo(page: Int) {
    if (IsEink) scrollToPage(page) else animateScrollToPage(page)
}

/**
 * 이북판: 좌우로 밀면 슬라이드 없이 바로 이전/다음 페이지로.
 * (이북판은 페이저 자체 밀기를 꺼 두고 이걸로 대신한다. 폰판에서는 아무것도 안 함)
 */
internal fun androidx.compose.ui.Modifier.einkSwipe(
    vertical: Boolean = false,
    onSwipe: (dir: Int) -> Unit,
): androidx.compose.ui.Modifier =
    if (!IsEink) this
    else if (vertical) this.pointerInput(Unit) {
        // 좌우·위아래 어느 쪽으로 밀어도: 왼쪽/위로 밀면 다음, 오른쪽/아래로 밀면 이전
        val threshold = 48.dp.toPx()
        var dx = 0f
        var dy = 0f
        detectDragGestures(
            onDragStart = { dx = 0f; dy = 0f },
            onDragEnd = {
                val d = if (kotlin.math.abs(dx) >= kotlin.math.abs(dy)) dx else dy
                if (d <= -threshold) onSwipe(1) else if (d >= threshold) onSwipe(-1)
            },
            onDragCancel = { dx = 0f; dy = 0f },
        ) { change, amount ->
            change.consume()
            dx += amount.x
            dy += amount.y
        }
    }
    else this.pointerInput(Unit) {
        val threshold = 48.dp.toPx()
        var total = 0f
        detectHorizontalDragGestures(
            onDragStart = { total = 0f },
            onDragEnd = {
                // 손가락을 왼쪽으로 밀면 다음, 오른쪽으로 밀면 이전
                if (total <= -threshold) onSwipe(1) else if (total >= threshold) onSwipe(-1)
                total = 0f
            },
            onDragCancel = { total = 0f },
        ) { change, dx ->
            change.consume()
            total += dx
        }
    }

/** 성경 권·장 번호 칸: 폰판은 옅은 바탕, 이북판은 바탕 없이 테두리만 */
@androidx.compose.runtime.Composable
internal fun tileColor(): androidx.compose.ui.graphics.Color =
    if (IsEink) androidx.compose.material3.MaterialTheme.colorScheme.background
    else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant

@androidx.compose.runtime.Composable
internal fun tileBorder(): androidx.compose.foundation.BorderStroke? =
    if (IsEink) androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.onBackground) else null
