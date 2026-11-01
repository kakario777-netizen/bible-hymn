package com.kakario.biblehymn.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakario.biblehymn.data.Bible
import com.kakario.biblehymn.data.BibleBook
import com.kakario.biblehymn.data.SearchHit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val LIMIT = 3000

/** 성경 단어 검색: 전체 / 구약 / 신약 / (해당 권) */
@Composable
fun BibleSearchScreen(
    initialBook: Int?,
    onBack: () -> Unit,
    onOpen: (book: Int, chapter: Int, verse: Int) -> Unit,
) {
    val context = LocalContext.current
    val books by produceState<List<BibleBook>?>(null) {
        value = withContext(Dispatchers.IO) { Bible.books(context) }
    }
    var query by rememberSaveable { mutableStateOf("") }
    // 0 = 전체, 1 = 구약, 2 = 신약, 3 = 해당 권
    var scope by rememberSaveable { mutableStateOf(if (initialBook != null) 3 else 0) }
    var submitted by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { if (query.isEmpty()) focus.requestFocus() }

    // 입력을 멈추고 잠시 뒤 자동 검색 (2글자 이상)
    LaunchedEffect(query) {
        if (query.trim().length >= 2) {
            delay(450)
            submitted = query.trim()
        } else if (query.isBlank()) {
            submitted = ""
        }
    }

    val bookName = remember(books, initialBook) { books?.firstOrNull { it.index == initialBook }?.name }
    var loading by remember { mutableStateOf(false) }
    val hits by produceState<List<SearchHit>>(emptyList(), submitted, scope, books) {
        val all = books
        if (submitted.isBlank() || all == null) {
            value = emptyList()
            return@produceState
        }
        loading = true
        val set: Set<Int>? = when (scope) {
            1 -> all.filter { !it.isNewTestament }.map { it.index }.toSet()
            2 -> all.filter { it.isNewTestament }.map { it.index }.toSet()
            3 -> initialBook?.let { setOf(it) }
            else -> null
        }
        value = withContext(Dispatchers.IO) { Bible.search(context, submitted, set, LIMIT) }
        loading = false
    }

    Scaffold(topBar = { BackTopBar("성경 검색", onBack) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().imePadding()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text("찾을 단어 (예: 사랑, 믿음 소망)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = ""; submitted = "" }) { Icon(Icons.Filled.Clear, contentDescription = "지우기") }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    submitted = query.trim()
                    keyboard?.hide()
                }),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .focusRequester(focus),
            )
            Row(Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScopeChip("전체", scope == 0) { scope = 0 }
                ScopeChip("구약", scope == 1) { scope = 1 }
                ScopeChip("신약", scope == 2) { scope = 2 }
                if (bookName != null) ScopeChip(bookName, scope == 3) { scope = 3 }
            }

            val list = hits
            Text(
                when {
                    submitted.isBlank() -> "두 글자 이상 입력하면 찾아드려요"
                    loading -> "찾는 중…"
                    list.size >= LIMIT -> "${LIMIT}개 이상 나왔어요 · 단어를 더 길게 써 보세요"
                    else -> "${list.size}개 절"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            )
            if (loading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            val names = remember(books) { books?.associate { it.index to it.name } ?: emptyMap() }
            val mark = MaterialTheme.colorScheme.primaryContainer
            val markText = MaterialTheme.colorScheme.onPrimaryContainer
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(list, key = { "${it.book}-${it.chapter}-${it.verse}" }) { h ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(h.book, h.chapter, h.verse) }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                    ) {
                        Text(
                            "${names[h.book] ?: ""} ${h.chapter}:${h.verse}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            highlight(h.text, submitted, mark, markText),
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
private fun ScopeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(label, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), maxLines = 1)
    }
}

/** 찾은 단어를 형광펜처럼 표시 (띄어쓰기가 달라도 표시) */
internal fun highlight(text: String, query: String, bg: Color, fg: Color): AnnotatedString {
    val q = query.trim()
    if (q.isEmpty()) return AnnotatedString(text)
    // 공백을 무시하고 위치 찾기: 원문 글자 위치 표를 만든다
    val idx = ArrayList<Int>()
    val sb = StringBuilder()
    text.forEachIndexed { i, c -> if (c != ' ') { sb.append(c); idx.add(i) } }
    val compact = sb.toString()
    val qc = q.replace(" ", "")
    val ranges = ArrayList<IntRange>()
    if (qc.isNotEmpty()) {
        var from = 0
        while (true) {
            val f = compact.indexOf(qc, from)
            if (f < 0) break
            ranges.add(idx[f]..idx[f + qc.length - 1])
            from = f + qc.length
        }
    }
    return buildAnnotatedString {
        var pos = 0
        for (r in ranges) {
            if (r.first < pos) continue
            append(text.substring(pos, r.first))
            withStyle(SpanStyle(background = bg, color = fg, fontWeight = FontWeight.Bold)) { append(text.substring(r.first, r.last + 1)) }
            pos = r.last + 1
        }
        if (pos < text.length) append(text.substring(pos))
    }
}
