package com.kakario.biblehymn.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
        BibleReaderLoaded(books, b, data, chapter, prefs, onBack, onJump)
    }
}

@Composable
private fun BibleReaderLoaded(
    books: List<BibleBook>,
    b: BibleBook,
    data: List<List<Verse>>,
    chapter: Int,
    prefs: Prefs,
    onBack: () -> Unit,
    onJump: (book: Int, chapter: Int) -> Unit,
) {
    val book = b.index
    var fontSize by remember { mutableFloatStateOf(prefs.fontSize) }
    val pageCount = data.size.coerceAtLeast(1)
    val pager = rememberPagerState(initialPage = (chapter - 1).coerceIn(0, pageCount - 1)) { pageCount }
    val scope = rememberCoroutineScope()
    val current = pager.currentPage + 1

    LaunchedEffect(book, current) {
        prefs.lastBook = book
        prefs.lastChapter = current
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = "${b.name} ${current}장",
                onBack = onBack,
                actions = {
                    TextButton(onClick = {
                        fontSize = (fontSize - 1f).coerceAtLeast(13f)
                        prefs.fontSize = fontSize
                    }) { Text("가-", fontSize = 15.sp) }
                    TextButton(onClick = {
                        fontSize = (fontSize + 1f).coerceAtMost(34f)
                        prefs.fontSize = fontSize
                    }) { Text("가+", fontSize = 19.sp, fontWeight = FontWeight.Bold) }
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
            )
        }
    }
}

@Composable
private fun ChapterText(bookName: String, chapter: Int, verses: List<Verse>, fontSize: Float) {
    val listState = rememberLazyListState()
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
