package com.kakario.biblehymn.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private const val SAMPLE = "요한복음 삼장 십육절. 하나님이 세상을 이처럼 사랑하사 독생자를 주셨으니 " +
    "이는 저를 믿는 자마다 멸망치 않고 영생을 얻게 하려 하심이니라."

/** 설정 안: 폰에 들어 있는 한국어 음성으로 성경 한 구절 미리 듣기 */
@Composable
fun VoicePreviewSection() {
    val context = LocalContext.current
    var ready by remember { mutableStateOf(false) }
    var engineLabel by remember { mutableStateOf("") }
    var speaking by remember { mutableStateOf(false) }
    val tts = remember {
        var ref: TextToSpeech? = null
        ref = TextToSpeech(context.applicationContext) { status ->
            val t = ref ?: return@TextToSpeech
            if (status == TextToSpeech.SUCCESS) {
                val r = t.setLanguage(Locale.KOREAN)
                ready = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
                engineLabel = engineName(context, t.defaultEngine)
                t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { speaking = false }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) { speaking = false }
                })
            }
        }
        ref!!
    }
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    Column {
        Text("성경 읽어주기 (폰 기본 음성)", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Text(
            if (engineLabel.isNotEmpty()) "지금 쓰는 음성: $engineLabel" else "폰 음성 엔진을 확인하는 중…",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
        )
        Row(Modifier.fillMaxWidth()) {
            Button(onClick = {
                if (!ready) {
                    Toast.makeText(context, "한국어 음성이 없어요. 아래 '폰 음성 설정'에서 한국어 음성을 받아 주세요", Toast.LENGTH_LONG).show()
                } else if (speaking) {
                    tts.stop()
                    speaking = false
                } else {
                    tts.speak(SAMPLE, TextToSpeech.QUEUE_FLUSH, null, "sample")
                    speaking = true
                }
            }) { Text(if (speaking) "멈추기" else "▶ 미리 듣기") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = { openTtsSettings(context) }) { Text("폰 음성 설정") }
        }
        Text(
            "폰 음성 설정에서 엔진(삼성/구글)과 목소리, 속도를 바꾼 뒤 다시 들어보세요.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private fun engineName(context: Context, pkg: String?): String = when {
    pkg == null -> ""
    pkg.contains("samsung", true) -> "삼성 음성"
    pkg.contains("google", true) -> "구글 음성"
    else -> try {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (e: Exception) {
        pkg
    }
}

private fun openTtsSettings(context: Context) {
    val intent = Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
        }
    }
}
