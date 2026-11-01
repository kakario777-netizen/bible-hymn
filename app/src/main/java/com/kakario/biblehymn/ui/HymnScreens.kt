package com.kakario.biblehymn.ui

import android.widget.Toast
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.derivedStateOf
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.kakario.biblehymn.data.Lyrics
import com.kakario.biblehymn.data.LyricsStore
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
import com.kakario.biblehymn.data.Prefs
import com.kakario.biblehymn.data.SongBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
private fun rememberHymns(book: SongBook): List<Hymn>? {
    val context = LocalContext.current
    val hymns by produceState<List<Hymn>?>(null, book) {
        value = withContext(Dispatchers.IO) { Hymns.all(context, book) }
    }
    return hymns
}

/** 찬송 목록 + 번호/제목 검색 */
@Composable
fun HymnListScreen(
    book: SongBook,
    prefs: Prefs,
    onBack: () -> Unit,
    onHymn: (Int) -> Unit,
) {
    val hymns = rememberHymns(book)
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
        val last = prefs.lastSong(book)
        if (hymns != null && query.isEmpty() && last > 3) listState.scrollToItem(last - 3)
    }

    var showSettings by remember { mutableStateOf(false) }
    var fitScore by remember { mutableStateOf(prefs.fitScore) }

    Scaffold(topBar = {
        BackTopBar(book.title, onBack, actions = {
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
                val showScroller = filtered.size > 30
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(end = if (showScroller) 40.dp else 0.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        items(filtered, key = { it.number }) { h ->
                            HymnRow(h, highlighted = h.number == prefs.lastSong(book)) { onHymn(h.number) }
                            HorizontalDivider(
                                Modifier.padding(start = 76.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            )
                        }
                    }
                    if (showScroller) {
                        FastScroller(listState, filtered, Modifier.align(Alignment.CenterEnd))
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
            color = if (h.hasScore) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
    book: SongBook,
    number: Int,
    prefs: Prefs,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val hymns = rememberHymns(book)
    val pager = rememberPagerState(initialPage = (number - 1).coerceIn(0, book.count - 1)) {
        hymns?.size ?: book.count
    }
    val scope = rememberCoroutineScope()
    val fitScore = remember { prefs.fitScore }
    val tapPaging = remember { prefs.tapPaging }
    KeepScreenOn(remember { prefs.screenTimeout })
    var showLyrics by remember { mutableStateOf(prefs.hymnShowLyrics) }
    var lyricStyle by remember { mutableStateOf(ReaderStyle.load(prefs)) }
    var showStyle by remember { mutableStateOf(false) }
    fun updateStyle(st: ReaderStyle) {
        lyricStyle = st
        st.save(prefs)
    }
    val onTapSide: ((Int) -> Unit)? = if (tapPaging) {
        { dir ->
            val target = pager.currentPage + dir
            if (target in 0 until pager.pageCount) scope.launch { pager.animateScrollToPage(target) }
        }
    } else {
        null
    }

    val current = hymns?.getOrNull(pager.currentPage)

    LaunchedEffect(pager.currentPage) {
        prefs.setLastSong(book, pager.currentPage + 1)
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = if (current == null) "" else "${current.number}장  ${current.title}",
                onBack = onBack,
                actions = {
                    if (showLyrics) FontButton(selected = showStyle) { showStyle = !showStyle }
                    ScoreLyricsToggle(showLyrics) {
                        showLyrics = it
                        prefs.hymnShowLyrics = it
                        if (!it) showStyle = false
                    }
                },
            )
        },
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface).navigationBarsPadding()) {
                HorizontalDivider()
                if (showStyle && showLyrics) {
                    StylePanel(
                        style = lyricStyle,
                        onChange = ::updateStyle,
                        onReset = { updateStyle(ReaderStyle.Default) },
                        onClose = { showStyle = false },
                        forLyrics = true,
                    )
                } else Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        enabled = pager.currentPage > 0,
                        onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                    ) { Text("◀ 이전") }

                    Text(
                        "${pager.currentPage + 1} / ${pager.pageCount}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )

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
                val h = hymns[page]
                if (showLyrics) {
                    LyricsPage(book, h, lyricStyle, onTapSide)
                } else if (h.hasScore) {
                    ZoomableScore(h.pages, fitScreen = fitScore, onTapSide = onTapSide)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "${h.number}장은 악보 파일이 없어요",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** 상단바 [악보 | 가사] 전환 */
@Composable
private fun ScoreLyricsToggle(showLyrics: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .padding(end = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(3.dp),
    ) {
        listOf(false to "악보", true to "가사").forEach { (value, label) ->
            val selected = showLyrics == value
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(50),
                    )
                    .clickable { onChange(value) }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
    }
}

/** 가사 보기: 1절, 2절 … 순서대로 (후렴은 절마다 이어서) */
@Composable
internal fun LyricsPage(book: SongBook, h: Hymn, style: ReaderStyle, onTapSide: ((Int) -> Unit)?) {
    val context = LocalContext.current
    val lyrics by produceState<Lyrics?>(null, book, h.number) {
        value = withContext(Dispatchers.IO) { LyricsStore.get(context, book, h.number) }
    }
    val tapModifier = if (onTapSide != null) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { o ->
                when {
                    o.x < size.width * 0.3f -> onTapSide(-1)
                    o.x > size.width * 0.7f -> onTapSide(1)
                }
            })
        }
    } else {
        Modifier
    }
    val l = lyrics
    if (l == null) {
        Box(Modifier.fillMaxSize().then(tapModifier), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${h.number}장 ${h.title}", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text(
                    "가사 준비중",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        return
    }
    LyricsContent(l, style, Modifier.fillMaxSize().then(tapModifier))
}

@Composable
internal fun LyricsContent(l: Lyrics, style: ReaderStyle, modifier: Modifier = Modifier) {
    val lh = (style.size * style.lineSpacing).sp
    val multi = l.verses.size > 1
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = style.sideMargin.dp, end = style.sideMargin.dp, top = 16.dp, bottom = 48.dp),
    ) {
        item {
            Text(
                "${l.number}장  ${l.title}",
                fontSize = (style.size + 3).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = style.fontFamily,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
        l.verses.forEachIndexed { i, verse ->
            item {
                Column(Modifier.padding(bottom = (style.verseGap + 10f).dp)) {
                    if (multi) {
                        Text(
                            "${i + 1}절",
                            fontSize = (style.size * 0.7f).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        verse,
                        fontSize = style.size.sp,
                        lineHeight = lh,
                        fontFamily = style.fontFamily,
                        fontWeight = style.weight,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (l.refrain.isNotBlank()) {
                        Text(
                            "후렴",
                            fontSize = (style.size * 0.7f).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            l.refrain,
                            fontSize = style.size.sp,
                            lineHeight = lh,
                            fontFamily = style.fontFamily,
                            fontWeight = style.weight,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (l.amen) {
            item {
                Text(
                    "아멘",
                    fontSize = style.size.sp,
                    fontFamily = style.fontFamily,
                    fontWeight = style.weight,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

/**
 * 목록 오른쪽 빠른 스크롤바: 1·100·200… 표시, 누르거나 끌면 그 위치로 바로 이동.
 * 끄는 동안 지금 몇 장인지 옆에 크게 보여준다.
 */
@Composable
internal fun FastScroller(listState: LazyListState, items: List<Hymn>, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var dragging by remember { mutableStateOf(false) }
    val count = items.size
    val primary = MaterialTheme.colorScheme.primary
    BoxWithConstraints(
        modifier
            .fillMaxHeight()
            .width(40.dp)
            .padding(vertical = 10.dp),
    ) {
        val hPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val boxH = maxHeight
        val thumbH = 36.dp
        fun jump(y: Float) {
            val idx = ((y / hPx).coerceIn(0f, 1f) * (count - 1)).roundToInt()
            scope.launch { listState.scrollToItem(idx) }
        }
        val frac by remember(count) {
            derivedStateOf {
                val visible = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
                val max = (count - visible).coerceAtLeast(1)
                (listState.firstVisibleItemIndex.toFloat() / max).coerceIn(0f, 1f)
            }
        }
        // 트랙
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = 6.dp)
                .width(3.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(2.dp)),
        )
        // 100 단위 표시
        val marks = items.indices.filter { items[it].number == 1 || items[it].number % 100 == 0 || it == count - 1 }
        marks.forEach { idx ->
            val f = idx.toFloat() / (count - 1).coerceAtLeast(1)
            Text(
                "${items[idx].number}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (boxH - 12.dp) * f)
                    .padding(end = 13.dp),
            )
        }
        // 손잡이
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(y = (boxH - thumbH) * frac)
                .padding(end = 4.dp)
                .width(7.dp)
                .height(thumbH)
                .background(primary.copy(alpha = if (dragging) 1f else 0.7f), RoundedCornerShape(4.dp)),
        )
        // 터치 영역
        Box(
            Modifier
                .matchParentSize()
                .pointerInput(count) { detectTapGestures { jump(it.y) } }
                .pointerInput(count) {
                    detectVerticalDragGestures(
                        onDragStart = { dragging = true; jump(it.y) },
                        onDragEnd = { dragging = false },
                        onDragCancel = { dragging = false },
                    ) { change, _ -> jump(change.position.y) }
                },
        )
    }
    // 끄는 중 현재 번호 말풍선
    if (dragging) {
        val n = items.getOrNull(listState.firstVisibleItemIndex)?.number
        if (n != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text("${n}장", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
                }
            }
        }
    }
}
