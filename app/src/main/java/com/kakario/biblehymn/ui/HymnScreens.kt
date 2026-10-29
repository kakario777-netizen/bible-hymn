package com.kakario.biblehymn.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakario.biblehymn.data.Hymn
import com.kakario.biblehymn.data.Hymns
import com.kakario.biblehymn.data.MidiPlayer
import com.kakario.biblehymn.data.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
private fun rememberHymns(): List<Hymn>? {
    val context = LocalContext.current
    val hymns by produceState<List<Hymn>?>(null) {
        value = withContext(Dispatchers.IO) { Hymns.all(context) }
    }
    return hymns
}

/** 통일찬송가 목록 + 번호/제목 검색 */
@Composable
fun HymnListScreen(
    prefs: Prefs,
    onBack: () -> Unit,
    onHymn: (Int) -> Unit,
) {
    val hymns = rememberHymns()
    var query by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val q = query.trim()
    val filtered = remember(hymns, q) {
        val all = hymns ?: emptyList()
        when {
            q.isEmpty() -> all
            q.all { it.isDigit() } -> {
                val n = q.toIntOrNull()
                all.filter { it.number == n } + all.filter { it.number != n && it.number.toString().startsWith(q) }
            }
            else -> {
                val key = q.replace(" ", "")
                all.filter { it.title.replace(" ", "").contains(key) }
            }
        }
    }

    // 처음 들어올 때 마지막으로 본 곡 근처로 스크롤
    LaunchedEffect(hymns) {
        val last = prefs.lastHymn
        if (hymns != null && query.isEmpty() && last > 3) listState.scrollToItem(last - 3)
    }

    var showSettings by remember { mutableStateOf(false) }
    var fitScore by remember { mutableStateOf(prefs.fitScore) }

    Scaffold(topBar = {
        BackTopBar("통일찬송가", onBack, actions = {
            IconButton(onClick = { showSettings = true }) {
                Icon(Icons.Filled.Settings, contentDescription = "설정")
            }
        })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().imePadding()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text("장 번호 또는 제목으로 찾기") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Clear, contentDescription = "지우기") }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    filtered.firstOrNull()?.let { onHymn(it.number) }
                }),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )

            if (hymns == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.TopCenter) {
                    Text("찾는 찬송가가 없어요", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(filtered, key = { it.number }) { h ->
                        HymnRow(h, highlighted = h.number == prefs.lastHymn) { onHymn(h.number) }
                        HorizontalDivider(
                            Modifier.padding(start = 76.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        )
                    }
                }
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("찬송가 설정") },
            text = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            fitScore = !fitScore
                            prefs.fitScore = fitScore
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("큰 악보 화면 맞추기", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "한 화면에 다 안 들어가는 악보를 줄여서 스크롤 없이 한 번에 보여줘요",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = fitScore,
                        onCheckedChange = {
                            fitScore = it
                            prefs.fitScore = it
                        },
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showSettings = false }) { Text("닫기") } },
        )
    }
}

@Composable
private fun HymnRow(h: Hymn, highlighted: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (highlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "${h.number}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp),
        )
        Text(
            h.title,
            fontSize = 17.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(start = 16.dp),
        )
        if (h.pages.size > 1) {
            Text(
                "${h.pages.size}쪽",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/** 악보 보기 — 좌우로 넘기면 앞/뒤 장 */
@Composable
fun HymnViewerScreen(
    number: Int,
    prefs: Prefs,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val hymns = rememberHymns()
    val pager = rememberPagerState(initialPage = (number - 1).coerceIn(0, Hymns.COUNT - 1)) {
        hymns?.size ?: Hymns.COUNT
    }
    val scope = rememberCoroutineScope()
    val midi = remember { MidiPlayer(context.applicationContext) }
    val fitScore = remember { prefs.fitScore }
    DisposableEffect(Unit) { onDispose { midi.stop() } }

    val current = hymns?.getOrNull(pager.currentPage)

    LaunchedEffect(pager.currentPage) {
        prefs.lastHymn = pager.currentPage + 1
        // 다른 곡으로 넘기면 반주는 멈춤
        if (midi.playingNumber != null && midi.playingNumber != pager.currentPage + 1) midi.stop()
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = if (current == null) "" else "${current.number}장  ${current.title}",
                onBack = onBack,
            )
        },
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface).navigationBarsPadding()) {
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        enabled = pager.currentPage > 0,
                        onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                    ) { Text("◀ 이전") }

                    val n = pager.currentPage + 1
                    val hasMidi = remember(n) { Hymns.midiPath(context, n) != null }
                    if (hasMidi) {
                        val playing = midi.playingNumber == n
                        Button(onClick = {
                            if (playing) midi.stop()
                            else if (!midi.play(n)) Toast.makeText(context, "반주를 재생할 수 없어요", Toast.LENGTH_SHORT).show()
                        }) {
                            if (!playing) Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Text(if (playing) "■ 반주 멈춤" else " 반주 듣기")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "MIDI 반주는 준비중입니다", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Text(" 반주 (준비중)")
                        }
                    }

                    TextButton(
                        enabled = pager.currentPage < pager.pageCount - 1,
                        onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    ) { Text("다음 ▶") }
                }
            }
        },
    ) { pad ->
        if (hymns == null) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            HorizontalPager(
                state = pager,
                modifier = Modifier.padding(pad).fillMaxSize(),
                beyondViewportPageCount = 1,
                key = { hymns[it].number },
            ) { page ->
                ZoomableScore(hymns[page].pages, fitScreen = fitScore)
            }
        }
    }
}
