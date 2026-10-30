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
import com.kakario.biblehymn.ui.AppTheme
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
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val sample = Verse(16, "하나님이 세상을 이처럼 사랑하사 독생자를 주셨으니 이는 저를 믿는 자마다 멸망치 않고 영생을 얻게 하려 하심이니라")

    @Test
    fun fonts() {
        paparazzi.snapshot {
            AppTheme(dark = false) {
                Surface {
                    Column(Modifier.background(MaterialTheme.colorScheme.background).padding(12.dp)) {
                        ReaderFonts.forEachIndexed { i, (name, _) ->
                            listOf(false, true).forEach { bold ->
                                Text(
                                    "$name · ${if (bold) "굵게" else "보통"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                VerseRow(sample, ReaderStyle.Default.copy(family = i, bold = bold, size = 17f, verseGap = 2f))
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
}
