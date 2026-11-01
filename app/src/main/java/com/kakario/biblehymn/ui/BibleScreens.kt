package com.kakario.biblehymn.ui

import androidx.compose.foundation.background
import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontVariation
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.roundToInt
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

/** 굵기 3단계: 보통 / 굵게 / 더 굵게 */
internal val WeightLevels = listOf(FontWeight.Normal, FontWeight.SemiBold, FontWeight.ExtraBold)
internal val WeightLabels = listOf("보통", "굵게", "더 굵게")

/** 가변 글꼴(굵기를 자유롭게 바꿀 수 있는 글꼴)을 3단계 굵기로 */
@OptIn(ExperimentalTextApi::class)
private fun variableFamily(res: Int): FontFamily = FontFamily(
    WeightLevels.map { w -> Font(res, w, variationSettings = FontVariation.Settings(FontVariation.weight(w.weight))) },
)

/**
 * 성경 본문 글꼴 목록. 저장값은 이 순서의 번호라서 앞쪽 4개 순서는 바꾸지 않는다.
 * 글꼴마다 실제 파일의 굵기값으로 한 번씩만 등록 (같은 파일을 두 번 등록하면 굵기가 안 먹음).
 * 굵기 파일이 2개뿐인 글꼴(고운바탕)은 '굵게'와 '더 굵게'가 같다.
 */
internal val ReaderFonts: List<Pair<String, FontFamily>> = listOf(
    "기본" to FontFamily.Default,
    "나눔고딕" to FontFamily(
        Font(R.font.nanum_gothic, FontWeight.Normal),
        Font(R.font.nanum_gothic_bold, FontWeight.Bold),
        Font(R.font.nanum_gothic_extrabold, FontWeight.ExtraBold),
    ),
    "나눔명조" to FontFamily(
        Font(R.font.nanum_myeongjo, FontWeight.Normal),
        Font(R.font.nanum_myeongjo_bold, FontWeight.Bold),
        Font(R.font.nanum_myeongjo_extrabold, FontWeight.ExtraBold),
    ),
    "고운바탕" to FontFamily(
        Font(R.font.gowun_batang, FontWeight.Normal),
        Font(R.font.gowun_batang_bold, FontWeight.Bold),
    ),
    "프리텐다드" to variableFamily(R.font.pretendard),
    "본고딕" to variableFamily(R.font.noto_sans_kr),
    "IBM 플렉스" to FontFamily(
        Font(R.font.ibm_plex_regular, FontWeight.Normal),
        Font(R.font.ibm_plex_semibold, FontWeight.SemiBold),
        Font(R.font.ibm_plex_bold, FontWeight.Bold),
    ),
    "고딕 A1" to FontFamily(
        Font(R.font.gothic_a1_regular, FontWeight.Normal),
        Font(R.font.gothic_a1_semibold, FontWeight.SemiBold),
        Font(R.font.gothic_a1_extrabold, FontWeight.ExtraBold),
    ),
    "함렛" to variableFamily(R.font.hahmlet),
)

/** 글꼴 고르는 화면에 보여줄 순서 (읽기 편한 글꼴 먼저) */
private val FontDisplayOrder = listOf(0, 4, 5, 6, 7, 1, 2, 3, 8)

/** 위치 표시 자리 이름 */
private val HeaderPosLabels = listOf("상단 왼쪽", "상단 가운데", "상단 오른쪽", "하단 왼쪽", "하단 가운데", "하단 오른쪽")

/** 성경 본문 모양 */
internal data class ReaderStyle(
    val size: Float,
    val family: Int,
    val weightLevel: Int,
    val lineSpacing: Float,
    val verseGap: Float,
    val sideMargin: Float,
    val numInline: Boolean,
    val numScale: Float,
    val header: Boolean,
    val headerSize: Float,
    val headerPos: Int,
) {
    val fontFamily: FontFamily get() = ReaderFonts.getOrNull(family)?.second ?: FontFamily.Default
    val weight: FontWeight get() = WeightLevels.getOrElse(weightLevel) { FontWeight.Normal }

    fun save(prefs: Prefs) {
        prefs.fontSize = size
        prefs.fontFamily = family
        prefs.fontWeightLevel = weightLevel
        prefs.lineSpacing = lineSpacing
        prefs.verseGap = verseGap
        prefs.sideMargin = sideMargin
        prefs.verseNumInline = numInline
        prefs.verseNumScale = numScale
        prefs.fullscreenHeader = header
        prefs.fullscreenHeaderSize = headerSize
        prefs.fullscreenHeaderPos = headerPos
    }

    companion object {
        val Default = ReaderStyle(19f, 0, 0, 1.65f, 10f, 18f, true, 0.6f, true, 11f, 1)
        fun load(prefs: Prefs) = ReaderStyle(
            prefs.fontSize, prefs.fontFamily, prefs.fontWeightLevel, prefs.lineSpacing, prefs.verseGap, prefs.sideMargin,
            prefs.verseNumInline, prefs.verseNumScale,
            prefs.fullscreenHeader, prefs.fullscreenHeaderSize, prefs.fullscreenHeaderPos,
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
    var fullscreen by remember { mutableStateOf(false) }
    FullscreenEffect(fullscreen)
    var showBookmarkDialog by remember { mutableStateOf(false) }
    val tapPaging = remember { prefs.tapPaging }
    KeepScreenOn(remember { prefs.screenTimeout })

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
            if (!fullscreen) BackTopBar(
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
            if (!fullscreen) Column(Modifier.background(MaterialTheme.colorScheme.surface).navigationBarsPadding()) {
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
        val showHeader = fullscreen && style.header
        val headerTop = style.headerPos < 3
        val headerSpace = (style.headerSize * 1.6f + 10f).dp
        Box(Modifier.padding(pad).fillMaxSize()) {
        HorizontalPager(
            state = pager,
            modifier = Modifier
                .padding(
                    top = if (showHeader && headerTop) headerSpace else 0.dp,
                    bottom = if (showHeader && !headerTop) headerSpace else 0.dp,
                )
                .fillMaxSize()
                .pointerInput(tapPaging) {
                    detectTapGestures(onTap = { o ->
                        when {
                            tapPaging && o.x < size.width * 0.3f -> goPage(pager.currentPage - 1)
                            tapPaging && o.x > size.width * 0.7f -> goPage(pager.currentPage + 1)
                            o.x in size.width * 0.3f..size.width * 0.7f -> fullscreen = !fullscreen
                        }
                    })
                },
            beyondViewportPageCount = 1,
        ) { page ->
            val (b, c) = locate(page)
            ChapterPage(b, c, style, listStateFor(page))
        }
        if (showHeader) {
            val align = when (style.headerPos) {
                0 -> Alignment.TopStart
                2 -> Alignment.TopEnd
                3 -> Alignment.BottomStart
                4 -> Alignment.BottomCenter
                5 -> Alignment.BottomEnd
                else -> Alignment.TopCenter
            }
            Text(
                "${curBook.name} ${curChapter}장",
                fontSize = style.headerSize.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                maxLines = 1,
                modifier = Modifier
                    .align(align)
                    .padding(horizontal = 14.dp, vertical = 5.dp),
            )
        }
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
internal fun FontButton(selected: Boolean, onClick: () -> Unit) {
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

/**
 * 아래에서 올라오는 글꼴 설정 — 분류를 먼저 고르고 한 단계 들어가서 조절.
 * 바꾸는 즉시 위 본문에 반영된다.
 */
@Composable
internal fun StylePanel(
    style: ReaderStyle,
    onChange: (ReaderStyle) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
    initialSection: String? = null,
    /** 찬송 가사 화면용: 절 번호·전체화면 항목은 숨김 */
    forLyrics: Boolean = false,
) {
    var section by remember { mutableStateOf(initialSection) }
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 460.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (section == null) {
                Text(if (forLyrics) "가사 글꼴 설정" else "글꼴 설정", fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onReset) { Text("기본값") }
            } else {
                Text(
                    "‹  $section",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { section = null }
                        .padding(vertical = 8.dp),
                )
            }
            TextButton(onClick = onClose) { Text("완료", fontWeight = FontWeight.Bold) }
        }

        when (section) {
            null -> {
                val fontName = ReaderFonts.getOrNull(style.family)?.first ?: "기본"
                PanelCategory("글꼴", "$fontName · ${WeightLabels.getOrElse(style.weightLevel) { "" }}") { section = "글꼴" }
                PanelCategory(
                    "크기와 간격",
                    "글자 ${style.size.toInt()} · 줄 ${String.format(Locale.US, "%.1f", style.lineSpacing)} · 절 ${style.verseGap.toInt()} · 여백 ${style.sideMargin.toInt()}",
                ) { section = "크기와 간격" }
                if (!forLyrics) PanelCategory(
                    "절 번호",
                    "${if (style.numInline) "글 앞에 작게" else "왼쪽 칸"} · ${(style.numScale * 100).roundToInt()}%",
                ) { section = "절 번호" }
                if (!forLyrics) PanelCategory(
                    "전체화면 위치 표시",
                    if (style.header) "켜짐 · ${HeaderPosLabels.getOrElse(style.headerPos) { "" }} · 크기 ${style.headerSize.toInt()}" else "꺼짐",
                ) { section = "전체화면 위치 표시" }
            }

            "글꼴" -> {
                PanelLabel("글꼴")
                // 한 줄에 3개씩
                FontDisplayOrder.chunked(3).forEach { rowIdx ->
                    Row(Modifier.padding(bottom = 8.dp)) {
                        rowIdx.forEach { i ->
                            val (name, family) = ReaderFonts[i]
                            ChoiceChip(name, selected = style.family == i, fontFamily = family) { onChange(style.copy(family = i)) }
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                }
                PanelLabel("굵기")
                Row {
                    WeightLabels.forEachIndexed { i, label ->
                        ChoiceChip(label, selected = style.weightLevel == i, fontFamily = style.fontFamily, weight = WeightLevels[i]) {
                            onChange(style.copy(weightLevel = i))
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }

            "크기와 간격" -> {
                SliderRow("글자 크기", "${style.size.toInt()}", style.size, 13f..50f, 36) {
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

            "절 번호" -> {
                PanelLabel("위치")
                Row {
                    ChoiceChip("글 앞에 작게", selected = style.numInline) { onChange(style.copy(numInline = true)) }
                    Spacer(Modifier.width(8.dp))
                    ChoiceChip("왼쪽 칸", selected = !style.numInline) { onChange(style.copy(numInline = false)) }
                }
                SliderRow("번호 크기", "${(style.numScale * 100).roundToInt()}%", style.numScale, 0.4f..1.0f, 11) {
                    onChange(style.copy(numScale = it))
                }
            }

            "전체화면 위치 표시" -> {
                Text(
                    "성경 화면 가운데를 누르면 전체화면이 되고, 지금 위치(예: 창세기 1장)를 작게 보여줘요",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                PanelLabel("표시")
                Row {
                    ChoiceChip("켜기", selected = style.header) { onChange(style.copy(header = true)) }
                    Spacer(Modifier.width(8.dp))
                    ChoiceChip("끄기", selected = !style.header) { onChange(style.copy(header = false)) }
                }
                if (style.header) {
                    PanelLabel("자리")
                    HeaderPosLabels.chunked(3).forEachIndexed { r, row ->
                        Row(Modifier.padding(bottom = 8.dp)) {
                            row.forEachIndexed { c, label ->
                                val pos = r * 3 + c
                                ChoiceChip(label, selected = style.headerPos == pos) { onChange(style.copy(headerPos = pos)) }
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                    SliderRow("표시 크기", "${style.headerSize.toInt()}", style.headerSize, 8f..18f, 9) {
                        onChange(style.copy(headerSize = it))
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelCategory(title: String, summary: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(summary, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
    weight: FontWeight = FontWeight.Normal,
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
            fontWeight = weight,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
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
        items(list, key = { it.number }) { v -> VerseRow(v, style) }
    }
}

/** 절 한 줄: 번호 + 본문 */
@Composable
internal fun VerseRow(v: Verse, style: ReaderStyle) {
    val lh = (style.size * style.lineSpacing).sp
    val numColor = MaterialTheme.colorScheme.primary
    if (style.numInline) {
        // 번호를 본문 첫머리 왼쪽 위에 작게 붙여서 글이 화면 좌우를 다 쓰게
        val text = remember(v, style.size, style.numScale, numColor) {
            buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontSize = (style.size * style.numScale).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        color = numColor,
                        baselineShift = BaselineShift(0.45f),
                    ),
                ) { append("${v.number}") }
                append("\u2009")
                append(v.text)
            }
        }
        Text(
            text,
            fontSize = style.size.sp,
            lineHeight = lh,
            fontFamily = style.fontFamily,
            fontWeight = style.weight,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().padding(vertical = (style.verseGap / 2f).dp),
        )
    } else {
        Row(Modifier.fillMaxWidth().padding(vertical = (style.verseGap / 2f).dp)) {
            Text(
                "${v.number}",
                fontSize = (style.size * style.numScale).sp,
                lineHeight = lh,
                fontWeight = FontWeight.Bold,
                color = numColor,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width((style.size * style.numScale * 2.2f).dp)
                    .padding(end = 6.dp),
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

/** 전체화면: 상단 상태바·하단 버튼줄 숨기기 (가장자리에서 밀면 잠깐 보임) */
@Composable
private fun FullscreenEffect(on: Boolean) {
    val activity = LocalContext.current as? Activity ?: return
    DisposableEffect(on) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        if (on) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { controller.show(WindowInsetsCompat.Type.systemBars()) }
    }
}
