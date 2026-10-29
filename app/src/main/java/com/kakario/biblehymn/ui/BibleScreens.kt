package com.kakario.biblehymn.ui

import androidx.compose.foundation.background
import android.widget.Toast
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

/** 성경 본문 — 좌우로 넘기면 이전/다음 장 */
@Composable
fun BibleReaderScreen(
    book: Int,
    chapter: Int,
    verse: Int,
    prefs: Prefs,
    onBack: () -> Unit,
    onJump: (book: Int, chapter: Int) -> Unit,
) {
    val context = LocalContext.current
    val books = rememberBooks()
    val chapters by produceState<List<List<Verse>>?>(null, book) {
        value = withContext(Dispatchers.IO) { Bible.chapters(context, book) }
    }
    val b = books?.firstOrNull { it.index == book }
    val data = chapters
    if (books == null || b == null || data == null) {
        Scaffold(topBar = { BackTopBar("", onBack) }) { pad -> Loading(Modifier.padding(pad)) }
    } else {
        BibleReaderLoaded(books, b, data, chapter, verse, prefs, onBack, onJump)
    }
}

@Composable
private fun BibleReaderLoaded(
    books: List<BibleBook>,
    b: BibleBook,
    data: List<List<Verse>>,
    chapter: Int,
    verse: Int,
    prefs: Prefs,
    onBack: () -> Unit,
    onJump: (book: Int, chapter: Int) -> Unit,
) {
    val context = LocalContext.current
    val book = b.index
    var fontSize by remember { mutableFloatStateOf(prefs.fontSize) }
    val pageCount = data.size.coerceAtLeast(1)
    val initialPage = (chapter - 1).coerceIn(0, pageCount - 1)
    val pager = rememberPagerState(initialPage = initialPage) { pageCount }
    val scope = rememberCoroutineScope()
    val current = pager.currentPage + 1
    var showBookmarkDialog by remember { mutableStateOf(false) }

    // 장마다 스크롤 위치 (처음 연 장은 지정한 절로 바로 이동)
    val listStates = remember { mutableMapOf<Int, LazyListState>() }
    fun listStateFor(page: Int): LazyListState = listStates.getOrPut(page) {
        LazyListState(firstVisibleItemIndex = if (page == initialPage && verse > 1) verse else 0)
    }
    /** 지금 화면 맨 위에 보이는 절 번호 */
    fun currentVerse(): Int {
        val idx = listStates[pager.currentPage]?.firstVisibleItemIndex ?: 0
        val count = data.getOrNull(pager.currentPage)?.size ?: 1
        return idx.coerceIn(1, count.coerceAtLeast(1))
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = "${b.name} ${current}장",
                onBack = onBack,
                actions = {
                    IconLabelButton(R.drawable.ic_bookmark_continue, "이어읽기") {
                        val v = currentVerse()
                        prefs.saveContinue(book, current, v)
                        Toast.makeText(context, "이어읽기 저장 · ${b.name} ${current}장 ${v}절", Toast.LENGTH_SHORT).show()
                    }
                    IconLabelButton(R.drawable.ic_bookmark_add, "책갈피") { showBookmarkDialog = true }
                    TextButton(
                        onClick = {
                            fontSize = (fontSize - 1f).coerceAtLeast(13f)
                            prefs.fontSize = fontSize
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        modifier = Modifier.widthIn(min = 40.dp),
                    ) { Text("가-", fontSize = 14.sp) }
                    TextButton(
                        onClick = {
                            fontSize = (fontSize + 1f).coerceAtMost(34f)
                            prefs.fontSize = fontSize
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        modifier = Modifier.widthIn(min = 40.dp),
                    ) { Text("가+", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                },
            )
        },
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface).navigationBarsPadding()) {
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        enabled = !(book == 1 && current == 1),
                        onClick = {
                            if (current > 1) {
                                scope.launch { pager.animateScrollToPage(current - 2) }
                            } else {
                                val prev = books.firstOrNull { it.index == book - 1 }
                                if (prev != null) onJump(prev.index, prev.chapterCount)
                            }
                        },
                    ) { Text("◀ 이전 장") }
                    Text(
                        "$current / $pageCount",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                    TextButton(
                        enabled = !(book == 66 && current == pageCount),
                        onClick = {
                            if (current < pageCount) {
                                scope.launch { pager.animateScrollToPage(current) }
                            } else if (book < 66) {
                                onJump(book + 1, 1)
                            }
                        },
                    ) { Text("다음 장 ▶") }
                }
            }
        },
    ) { pad ->
        HorizontalPager(
            state = pager,
            modifier = Modifier.padding(pad).fillMaxSize(),
            beyondViewportPageCount = 1,
            key = { "$book-$it" },
        ) { page ->
            ChapterText(
                bookName = b.name,
                chapter = page + 1,
                verses = data.getOrNull(page) ?: emptyList(),
                fontSize = fontSize,
                listState = listStateFor(page),
            )
        }
    }

    if (showBookmarkDialog) {
        val v = remember { currentVerse() }
        TextInputDialog(
            title = "책갈피 저장 · ${b.name} ${current}장 ${v}절",
            initial = remember { prefs.nextBookmarkName() },
            onDismiss = { showBookmarkDialog = false },
            onSave = { name ->
                prefs.addBookmark(name.ifEmpty { prefs.nextBookmarkName() }, book, current, v)
                showBookmarkDialog = false
                Toast.makeText(context, "책갈피를 저장했어요", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

@Composable
private fun ChapterText(
    bookName: String,
    chapter: Int,
    verses: List<Verse>,
    fontSize: Float,
    listState: LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 20.dp, top = 12.dp, bottom = 40.dp),
    ) {
        item {
            Text(
                "$bookName 제${chapter}장",
                fontSize = (fontSize + 3).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }
        items(verses, key = { it.number }) { v ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Text(
                    "${v.number}",
                    fontSize = (fontSize * 0.72f).sp,
                    lineHeight = (fontSize * 1.65f).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width((fontSize * 1.7f).dp).padding(end = 8.dp, top = 2.dp),
                )
                Text(
                    v.text,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.65f).sp,
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
