package com.kakario.biblehymn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.kakario.biblehymn.data.Prefs
import com.kakario.biblehymn.data.Verse
import com.kakario.biblehymn.R
import com.kakario.biblehymn.data.Lyrics
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import com.kakario.biblehymn.data.Hymn
import com.kakario.biblehymn.ui.AppTheme
import com.kakario.biblehymn.ui.FastScroller
import com.kakario.biblehymn.ui.LyricsContent
import com.kakario.biblehymn.ui.BackTopBar
import com.kakario.biblehymn.ui.FontButton
import com.kakario.biblehymn.ui.IconLabelButton
import com.kakario.biblehymn.ui.HomeScreen
import com.kakario.biblehymn.ui.ReaderFonts
import com.kakario.biblehymn.ui.ReaderStyle
import com.kakario.biblehymn.ui.StylePanel
import com.kakario.biblehymn.ui.VerseRow
import org.junit.Rule
import org.junit.Test

/** GitHub 빌드에서 화면을 이미지로 찍어 확인하기 위한 테스트 */
class ScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5.copy(screenHeight = 3200))

    private val sample = Verse(16, "하나님이 세상을 이처럼 사랑하사 독생자를 주셨으니 이는 저를 믿는 자마다 멸망치 않고 영생을 얻게 하려 하심이니라")

    @Test
    fun fonts() {
        paparazzi.snapshot {
            AppTheme(dark = false) {
                Surface {
                    Column(Modifier.background(MaterialTheme.colorScheme.background).padding(12.dp)) {
                        ReaderFonts.forEachIndexed { i, (name, _) ->
                            Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            (0..2).forEach { lv ->
                                VerseRow(
                                    Verse(lv + 1, "태초에 하나님이 천지를 창조하시니라 (굵기 ${lv + 1}단계)"),
                                    ReaderStyle.Default.copy(family = i, weightLevel = lv, size = 15f, verseGap = 0f, lineSpacing = 1.3f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun stylePanel() {
        paparazzi.snapshot {
            AppTheme(dark = false) {
                Surface {
                    Column(Modifier.fillMaxWidth()) {
                        VerseRow(sample, ReaderStyle.Default)
                        StylePanel(ReaderStyle.Default, onChange = {}, onReset = {}, onClose = {})
                        StylePanel(ReaderStyle.Default, onChange = {}, onReset = {}, onClose = {}, initialSection = "글꼴")
                        StylePanel(ReaderStyle.Default, onChange = {}, onReset = {}, onClose = {}, initialSection = "전체화면 위치 표시")
                    }
                }
            }
        }
    }

    @Test
    fun homeLight() {
        val prefs = Prefs(paparazzi.context)
        paparazzi.snapshot {
            AppTheme(dark = false) {
                Surface { HomeScreen(prefs, 0, {}, {}, { _, _, _ -> }, {}, {}, {}) }
            }
        }
    }

    @Test
    fun homeDark() {
        val prefs = Prefs(paparazzi.context)
        paparazzi.snapshot {
            AppTheme(dark = true) {
                Surface { HomeScreen(prefs, 2, {}, {}, { _, _, _ -> }, {}, {}, {}) }
            }
        }
    }

    @Test
    fun readerTopBar() {
        paparazzi.snapshot {
            AppTheme(dark = false) {
                Surface {
                    Column {
                        listOf("창세기 1장", "데살로니가전서 3장", "예레미야애가 5장").forEach { t ->
                            BackTopBar(t, onBack = {}, actions = {
                                IconLabelButton(R.drawable.ic_bookmark_continue, "이어읽기") {}
                                IconLabelButton(R.drawable.ic_bookmark_add, "책갈피") {}
                                FontButton(selected = false) {}
                            })
                        }
                    }
                }
            }
        }
    }

    @Test
    fun verseNumbers() {
        val v2 = Verse(17, "하나님이 그 아들을 세상에 보내신 것은 세상을 심판하려 하심이 아니요 저로 말미암아 세상이 구원을 받게 하려 하심이라")
        paparazzi.snapshot {
            AppTheme(dark = false) {
                Surface {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                        listOf(
                            "글 앞에 작게 (기본)" to ReaderStyle.Default,
                            "글 앞에 작게 · 번호 100%" to ReaderStyle.Default.copy(numScale = 1.0f),
                            "왼쪽 칸" to ReaderStyle.Default.copy(numInline = false),
                            "글자 크기 30" to ReaderStyle.Default.copy(size = 30f),
                        ).forEach { (label, st) ->
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            VerseRow(sample, st)
                            VerseRow(v2, st)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun lyrics() {
        val l = Lyrics(
            number = 5,
            title = "가사 화면 예시",
            verses = listOf(
                "첫째 절 예시 문장입니다 화면에 이렇게 이어서 보여요 길게 써서 줄바꿈도 확인합니다",
                "둘째 절 예시 문장입니다 화면에 이렇게 이어서 보여요",
                "셋째 절 예시 문장입니다",
            ),
            refrain = "후렴 예시 문장입니다 절마다 이어서 보여요",
            amen = true,
        )
        paparazzi.snapshot {
            AppTheme(dark = false) {
                Surface { LyricsContent(l, ReaderStyle.Default) }
            }
        }
    }

    @Test
    fun hymnListScroller() {
        val items = (1..558).map { Hymn(it, "찬송가 제목 예시 $it", emptyList()) }
        paparazzi.snapshot {
            AppTheme(dark = false) {
                Surface {
                    val state = rememberLazyListState(initialFirstVisibleItemIndex = 250)
                    Box(Modifier.fillMaxSize()) {
                        LazyColumn(state = state, modifier = Modifier.fillMaxSize().padding(end = 40.dp)) {
                            items(items) { h -> Text("${h.number}   ${h.title}", fontSize = 17.sp, modifier = Modifier.padding(16.dp)) }
                        }
                        FastScroller(state, items, Modifier.align(Alignment.CenterEnd))
                    }
                }
            }
        }
    }
}
