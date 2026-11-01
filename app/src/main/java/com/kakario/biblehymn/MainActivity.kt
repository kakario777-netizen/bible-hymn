package com.kakario.biblehymn

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import com.kakario.biblehymn.ui.UserActivity
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kakario.biblehymn.data.Prefs
import com.kakario.biblehymn.data.SongBook
import com.kakario.biblehymn.ui.AppTheme
import com.kakario.biblehymn.ui.BibleBooksScreen
import com.kakario.biblehymn.ui.BibleChaptersScreen
import com.kakario.biblehymn.ui.BibleReaderScreen
import com.kakario.biblehymn.ui.BibleSearchScreen
import com.kakario.biblehymn.ui.BookmarksScreen
import com.kakario.biblehymn.ui.HomeScreen
import com.kakario.biblehymn.ui.HymnListScreen
import com.kakario.biblehymn.ui.HymnViewerScreen

/** 앱 안의 화면들 */
sealed interface Screen {
    data object Home : Screen
    data object BibleBooks : Screen
    data class BibleChapters(val book: Int) : Screen
    data class BibleReader(val book: Int, val chapter: Int, val verse: Int = 1) : Screen
    data object Bookmarks : Screen
    data class BibleSearch(val book: Int?) : Screen
    data class HymnList(val book: SongBook) : Screen
    data class HymnViewer(val book: SongBook, val number: Int) : Screen
}

class MainActivity : ComponentActivity() {
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        UserActivity.lastTouch = SystemClock.elapsedRealtime()
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = Prefs(applicationContext)
        setContent {
            var themeMode by remember { mutableIntStateOf(prefs.themeMode) }
            val systemDark = isSystemInDarkTheme()
            val dark = when (themeMode) {
                1 -> false
                2 -> true
                else -> systemDark
            }
            // 상단 상태바·하단 버튼 아이콘 색도 배경에 맞춤
            LaunchedEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            AppTheme(dark = dark) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App(
                        prefs = prefs,
                        themeMode = themeMode,
                        onThemeMode = {
                            themeMode = it
                            prefs.themeMode = it
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun App(prefs: Prefs, themeMode: Int, onThemeMode: (Int) -> Unit) {
    val stack = remember { mutableStateListOf<Screen>(Screen.Home) }

    fun push(s: Screen) {
        stack.add(s)
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun replaceTop(s: Screen) {
        stack[stack.lastIndex] = s
    }

    BackHandler(enabled = stack.size > 1) { pop() }

    when (val screen = stack.last()) {
        Screen.Home -> HomeScreen(
            prefs = prefs,
            themeMode = themeMode,
            onThemeMode = onThemeMode,
            onBible = { push(Screen.BibleBooks) },
            onContinueBible = { book, chapter, verse ->
                push(Screen.BibleReader(book, chapter, verse))
            },
            onBookmarks = { push(Screen.Bookmarks) },
            onHymn = { push(Screen.HymnList(SongBook.UNIFIED)) },
            onMySoul = { push(Screen.HymnList(SongBook.MYSOUL)) },
        )

        Screen.BibleBooks -> BibleBooksScreen(
            onBack = ::pop,
            onSearch = { push(Screen.BibleSearch(null)) },
            onBook = { book, chapterCount ->
                if (chapterCount == 1) push(Screen.BibleReader(book, 1))
                else push(Screen.BibleChapters(book))
            },
        )

        is Screen.BibleChapters -> BibleChaptersScreen(
            book = screen.book,
            onBack = ::pop,
            onSearch = { push(Screen.BibleSearch(screen.book)) },
            onChapter = { push(Screen.BibleReader(screen.book, it)) },
        )

        is Screen.BibleReader -> key(screen.book, screen.chapter, screen.verse) {
            BibleReaderScreen(
                book = screen.book,
                chapter = screen.chapter,
                verse = screen.verse,
                prefs = prefs,
                onBack = ::pop,
            )
        }

        is Screen.BibleSearch -> BibleSearchScreen(
            initialBook = screen.book,
            onBack = ::pop,
            onOpen = { b, c, v -> push(Screen.BibleReader(b, c, v)) },
        )

        Screen.Bookmarks -> BookmarksScreen(
            prefs = prefs,
            onBack = ::pop,
            onOpen = { b, c, v -> push(Screen.BibleReader(b, c, v)) },
        )

        is Screen.HymnList -> HymnListScreen(
            book = screen.book,
            prefs = prefs,
            onBack = ::pop,
            onHymn = { push(Screen.HymnViewer(screen.book, it)) },
        )

        is Screen.HymnViewer -> HymnViewerScreen(
            book = screen.book,
            number = screen.number,
            prefs = prefs,
            onBack = ::pop,
        )
    }
}
