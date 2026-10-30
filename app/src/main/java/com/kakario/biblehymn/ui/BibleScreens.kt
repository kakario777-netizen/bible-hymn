package com.kakario.biblehymn.ui

import androidx.compose.foundation.background
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.kakario.biblehymn.R
import com.kakario.biblehymn.data.Bookmark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakario.biblehymn.data.Bible
import com.kakario.biblehymn.data.BibleBook
import com.kakario.biblehymn.data.Prefs
import com.kakario.biblehymn.data.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
private fun rememberBooks(): List<BibleBook>? {
    val context = LocalContext.current
    val books by produceState<List<BibleBook>?>(null) {
        value = withContext(Dispatchers.IO) { Bible.books(context) }
    }
    return books
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

/** 성경 목록 (구약 / 신약) */
@Composable
fun BibleBooksScreen(
    onBack: () -> Unit,
    onBook: (book: Int, chapterCount: Int) -> Unit,
) {
    val books = rememberBooks()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(topBar = { BackTopBar("성경 (개역한글)", onBack) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("구약 39권") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("신약 27권") })
            }
            if (books == null) {
                Loading()
            } else {
                val list = books.filter { it.isNewTestament == (tab == 1) }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 104.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(list, key = { it.index }) { b ->
                        Surface(
                            onClick = { onBook(b.index, b.chapterCount) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().height(72.dp),
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    b.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = if (b.name.length > 5) 14.sp else 16.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                )
                                Text(
                                    "${b.chapterCount}장",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 장 선택 */
@Composable
fun BibleChaptersScreen(
    book: Int,
    onBack: () -> Unit,
    onChapter: (Int) -> Unit,
) {
    val books = rememberBooks()
    val b = books?.firstOrNull { it.index == book }
    Scaffold(topBar = { BackTopBar(b?.name ?: "", onBack) }) { pad ->
        if (b == null) {
            Loading(Modifier.padding(pad))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 60.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(pad).fillMaxSize(),
            ) {
                items((1..b.chapterCount).toList()) { c ->
                    Surface(
                        onClick = { onChapter(c) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.aspectRatio(1f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("$c", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

/** 성경 본문 글꼴 목록 (설정값 순서와 같음) */
private val ReaderFonts: List<Pair<String, FontFamily>> = listOf(
    "기본" to FontFamily.Default,
    "나눔고딕" to FontFamily(Font(R.font.nanum_gothic)),
    "나눔명조" to FontFamily(Font(R.font.nanum_myeongjo)),
    "고운바탕" to FontFamily(Font(R.font.gowun_batang)),
)

/** 성경 본문 모양 */
private data class ReaderStyle(
    val size: Float,
    val family: Int,
    val bold: Boolean,
    val lineSpacing: Float,
    val verseGap: Float,
    val sideMargin: Float,
) {
    val fontFamily: FontFamily get() = ReaderFonts.getOrNull(family)?.second ?: FontFamily.Default
    val weight: FontWeight get() = if (bold) FontWeight.Bold else FontWeight.Normal

    fun save(prefs: Prefs) {
        prefs.fontSize = size
        prefs.fontFamily = family
        prefs.fontBold = bold
        prefs.lineSpacing = lineSpacing
        prefs.verseGap = verseGap
        prefs.sideMargin = sideMargin
    }

    companion object {
        val Default = ReaderStyle(19f, 0, false, 1.65f, 10f, 18f)
        fun load(prefs: Prefs) = ReaderStyle(
            prefs.fontSize, prefs.fontFamily, prefs.fontBold, prefs.lineSpacing, prefs.verseGap, prefs.sideMargin,
        )
    }
}

/** 성경 본문 — 성경 전체가 한 줄로 이어져서 좌우로 넘기면 다음 권까지 계속 넘어감 */
@Composable
fun BibleReaderScreen(
    book: Int,
    chapter: Int,
    verse: Int,
    prefs: Prefs,
    onBack: () -> Unit,
) {
    val books = rememberBooks()
    if (books == null) {
        Scaffold(topBar = { BackTopBar("", onBack) }) { pad -> Loading(Modifier.padding(pad)) }
    } else {
        BibleReaderLoaded(books, book, chapter, verse, prefs, onBack)
    }
}

@Composable
private fun BibleReaderLoaded(
    books: List<BibleBook>,
    startBook: Int,
    startChapter: Int,
    startVerse: Int,
    prefs: Prefs,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    // 각 권의 첫 장이 전체에서 몇 번째 페이지인지
    val firstPage = remember(books) {
        val arr = IntArray(books.size)
        var acc = 0
        books.forEachIndexed { i, b -> arr[i] = acc; acc += b.chapterCount }
        arr
    }
    val total = remember(books) { books.sumOf { it.chapterCount } }
    fun locate(page: Int): Pair<BibleBook, Int> {
        var i = firstPage.size - 1
        while (i > 0 && firstPage[i] > page) i--
        return books[i] to (page - firstPage[i] + 1)
    }
    val startIdx = books.indexOfFirst { it.index == startBook }.coerceAtLeast(0)
    val initialPage = (firstPage[startIdx] + startChapter - 1).coerceIn(0, total - 1)

    val pager = rememberPagerState(initialPage = initialPage) { total }
    val scope = rememberCoroutineScope()
    val (curBook, curChapter) = locate(pager.currentPage)

    var style by remember { mutableStateOf(ReaderStyle.load(prefs)) }
    fun updateStyle(s: ReaderStyle) {
        style = s
        s.save(prefs)
    }
    var showStyle by remember { mutableStateOf(false) }
    var showBookmarkDialog by remember { mutableStateOf(false) }
    val tapPaging = remember { prefs.tapPaging }

    // 장마다 스크롤 위치 (처음 연 장은 지정한 절로 바로 이동)
    val listStates = remember { mutableMapOf<Int, LazyListState>() }
    fun listStateFor(page: Int): LazyListState = listStates.getOrPut(page) {
        LazyListState(firstVisibleItemIndex = if (page == initialPage && startVerse > 1) startVerse else 0)
    }
    /** 지금 화면 맨 위에 보이는 절 번호 (0번 항목은 장 제목) */
    fun currentVerse(): Int = (listStates[pager.currentPage]?.firstVisibleItemIndex ?: 0).coerceAtLeast(1)

    fun goPage(p: Int) {
        if (p in 0 until total) scope.launch { pager.animateScrollToPage(p) }
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = "${curBook.name} ${curChapter}장",
                onBack = onBack,
                actions = {
                    IconLabelButton(R.drawable.ic_bookmark_continue, "이어읽기") {
                        val v = currentVerse()
                        prefs.saveContinue(curBook.index, curChapter, v)
                        Toast.makeText(context, "이어읽기 저장 · ${curBook.name} ${curChapter}장 ${v}절", Toast.LENGTH_SHORT).show()
                    }
                    IconLabelButton(R.drawable.ic_bookmark_add, "책갈피") { showBookmarkDialog = true }
                    FontButton(selected = showStyle) { showStyle = !showStyle }
                },
            )
        },
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface).navigationBarsPadding()) {
                HorizontalDivider()
                if (showStyle) {
                    StylePanel(
                        style = style,
                        onChange = ::updateStyle,
                        onReset = { updateStyle(ReaderStyle.Default) },
                        onClose = { showStyle = false },
                    )
                } else {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            enabled = pager.currentPage > 0,
                            onClick = { goPage(pager.currentPage - 1) },
                        ) { Text("◀ 이전 장") }
                        Text(
                            "$curChapter / ${curBook.chapterCount}",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                        TextButton(
                            enabled = pager.currentPage < total - 1,
                            onClick = { goPage(pager.currentPage + 1) },
                        ) { Text("다음 장 ▶") }
                    }
                }
            }
        },
    ) { pad ->
        HorizontalPager(
            state = pager,
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .pointerInput(tapPaging) {
                    if (tapPaging) {
                        detectTapGestures(onTap = { o ->
                            when {
                                o.x < size.width * 0.3f -> goPage(pager.currentPage - 1)
                                o.x > size.width * 0.7f -> goPage(pager.currentPage + 1)
                            }
                        })
                    }
                },
            beyondViewportPageCount = 1,
        ) { page ->
            val (b, c) = locate(page)
            ChapterPage(b, c, style, listStateFor(page))
        }
    }

    if (showBookmarkDialog) {
        val v = remember { currentVerse() }
        TextInputDialog(
            title = "책갈피 저장 · ${curBook.name} ${curChapter}장 ${v}절",
            initial = remember { prefs.nextBookmarkName() },
            onDismiss = { showBookmarkDialog = false },
            onSave = { name ->
                prefs.addBookmark(name.ifEmpty { prefs.nextBookmarkName() }, curBook.index, curChapter, v)
                showBookmarkDialog = false
                Toast.makeText(context, "책갈피를 저장했어요", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

/** 상단바 'T' 글꼴 버튼 */
@Composable
private fun FontButton(selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.widthIn(min = 46.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("T", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, lineHeight = 24.sp)
            Text("글꼴", fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** 아래에서 올라오는 글꼴 상세 설정 — 바꾸는 즉시 위 본문에 반영 */
@Composable
private fun StylePanel(
    style: ReaderStyle,
    onChange: (ReaderStyle) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 340.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("글꼴 설정", fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onReset) { Text("기본값") }
            TextButton(onClick = onClose) { Text("완료", fontWeight = FontWeight.Bold) }
        }

        PanelLabel("글꼴")
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            ReaderFonts.forEachIndexed { i, (name, family) ->
                ChoiceChip(name, selected = style.family == i, fontFamily = family) { onChange(style.copy(family = i)) }
                Spacer(Modifier.width(8.dp))
            }
        }

        PanelLabel("굵기")
        Row {
            ChoiceChip("보통", selected = !style.bold) { onChange(style.copy(bold = false)) }
            Spacer(Modifier.width(8.dp))
            ChoiceChip("굵게", selected = style.bold, bold = true) { onChange(style.copy(bold = true)) }
        }

        SliderRow("글자 크기", "${style.size.toInt()}", style.size, 13f..34f, 20) {
            onChange(style.copy(size = it))
        }
        SliderRow("줄 간격", String.format(Locale.US, "%.1f", style.lineSpacing), style.lineSpacing, 1.2f..2.4f, 11) {
            onChange(style.copy(lineSpacing = it))
        }
        SliderRow("절 간격", "${style.verseGap.toInt()}", style.verseGap, 0f..28f, 13) {
            onChange(style.copy(verseGap = it))
        }
        SliderRow("좌우 여백", "${style.sideMargin.toInt()}", style.sideMargin, 4f..48f, 10) {
            onChange(style.copy(sideMargin = it))
        }
    }
}

@Composable
private fun PanelLabel(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
    )
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    fontFamily: FontFamily? = null,
    bold: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            label,
            fontSize = 15.sp,
            fontFamily = fontFamily,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun SliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit,
) {
    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(64.dp))
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            steps = steps,
            modifier = Modifier.weight(1f).height(32.dp),
        )
        Text(valueText, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.width(34.dp))
    }
}

@Composable
private fun ChapterPage(book: BibleBook, chapter: Int, style: ReaderStyle, listState: LazyListState) {
    val context = LocalContext.current
    val verses by produceState<List<Verse>?>(null, book.index, chapter) {
        value = withContext(Dispatchers.IO) { Bible.chapters(context, book.index).getOrNull(chapter - 1) ?: emptyList() }
    }
    val list = verses
    if (list == null) {
        Loading()
        return
    }
    val lh = (style.size * style.lineSpacing).sp
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = style.sideMargin.dp, end = (style.sideMargin + 2f).dp, top = 12.dp, bottom = 40.dp),
    ) {
        item {
            Text(
                "${book.name} 제${chapter}장",
                fontSize = (style.size + 3).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = style.fontFamily,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }
        items(list, key = { it.number }) { v ->
            Row(Modifier.fillMaxWidth().padding(vertical = (style.verseGap / 2f).dp)) {
                Text(
                    "${v.number}",
                    fontSize = (style.size * 0.72f).sp,
                    lineHeight = lh,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width((style.size * 1.7f).dp).padding(end = 8.dp, top = 2.dp),
                )
                Text(
                    v.text,
                    fontSize = style.size.sp,
                    lineHeight = lh,
                    fontFamily = style.fontFamily,
                    fontWeight = style.weight,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 책갈피 화면: 맨 위 이어읽기(한 곳) + 책갈피 목록(무제한) */
@Composable
fun BookmarksScreen(
    prefs: Prefs,
    onBack: () -> Unit,
    onOpen: (book: Int, chapter: Int, verse: Int) -> Unit,
) {
    val books = rememberBooks()
    var marks by remember { mutableStateOf(prefs.bookmarks().sortedByDescending { it.createdAt }) }
    var renaming by remember { mutableStateOf<Bookmark?>(null) }
    var deleting by remember { mutableStateOf<Bookmark?>(null) }
    fun refresh() {
        marks = prefs.bookmarks().sortedByDescending { it.createdAt }
    }
    fun where(book: Int, chapter: Int, verse: Int): String {
        val name = books?.firstOrNull { it.index == book }?.name ?: ""
        return "$name ${chapter}장 ${verse}절"
    }
    val dateFmt = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREA) }

    Scaffold(topBar = { BackTopBar("책갈피", onBack) }) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionLabel("이어읽기") }
            item {
                val lb = prefs.lastBook
                if (lb in 1..66) {
                    MarkRow(
                        icon = R.drawable.ic_bookmark_continue,
                        title = "이어읽기",
                        sub = where(lb, prefs.lastChapter, prefs.lastVerse),
                        highlight = true,
                        onClick = { onOpen(lb, prefs.lastChapter, prefs.lastVerse) },
                    )
                } else {
                    EmptyNote("성경 화면 위쪽 [이어읽기]를 누르면 여기 저장돼요")
                }
            }
            item { SectionLabel("책갈피 ${marks.size}개", Modifier.padding(top = 10.dp)) }
            if (marks.isEmpty()) {
                item { EmptyNote("성경 화면 위쪽 [책갈피]를 눌러 원하는 만큼 저장할 수 있어요") }
            }
            items(marks, key = { it.id }) { m ->
                MarkRow(
                    icon = R.drawable.ic_bookmark,
                    title = m.name,
                    sub = where(m.book, m.chapter, m.verse) + "  ·  " + dateFmt.format(Date(m.createdAt)),
                    highlight = false,
                    onClick = { onOpen(m.book, m.chapter, m.verse) },
                    onEdit = { renaming = m },
                    onDelete = { deleting = m },
                )
            }
        }
    }

    renaming?.let { m ->
        TextInputDialog(
            title = "책갈피 이름 바꾸기",
            initial = m.name,
            onDismiss = { renaming = null },
            onSave = { name ->
                if (name.isNotEmpty()) prefs.renameBookmark(m.id, name)
                renaming = null
                refresh()
            },
        )
    }
    deleting?.let { m ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("책갈피 삭제") },
            text = { Text("'${m.name}' 책갈피를 지울까요?") },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("취소") } },
            confirmButton = {
                TextButton(onClick = {
                    prefs.deleteBookmark(m.id)
                    deleting = null
                    refresh()
                }) { Text("삭제") }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
    )
}

@Composable
private fun MarkRow(
    icon: Int,
    title: String,
    sub: String,
    highlight: Boolean,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(sub, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "이름 바꾸기") }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "삭제") }
            }
        }
    }
}
