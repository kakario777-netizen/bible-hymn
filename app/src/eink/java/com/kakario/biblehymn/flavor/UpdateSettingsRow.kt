package com.kakario.biblehymn.flavor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakario.biblehymn.update.Updater

/** 이북판 설정: '새 버전 확인' 줄 + 확인·받기·설치 창. */
@Composable
fun UpdateSettingsRow() {
    val context = LocalContext.current
    val updater = remember { Updater.get(context) }
    val state by updater.state.collectAsState()

    Column(
        Modifier
            .fillMaxWidth()
            .clickable { updater.check(quiet = false) }
            .padding(vertical = 12.dp),
    ) {
        Text("새 버전 확인", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Text("지금 빌드 #${updater.currentBuild}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    UpdateDialog(state, updater)
}

@Composable
private fun UpdateDialog(state: Updater.State, updater: Updater) {
    @Composable
    fun simple(title: String, body: String, confirm: String? = "확인", onConfirm: () -> Unit = { updater.dismiss() }, dismiss: String? = null) =
        AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = { if (confirm != null) TextButton(onClick = onConfirm) { Text(confirm, fontWeight = FontWeight.Bold) } },
            dismissButton = { if (dismiss != null) TextButton(onClick = { updater.dismiss() }) { Text(dismiss) } },
        )

    when (state) {
        Updater.State.Idle -> Unit
        Updater.State.Checking -> simple("새 버전 확인", "확인하는 중…", confirm = null)
        is Updater.State.UpToDate -> simple("최신 버전이에요", "지금 빌드 #${state.build}")
        is Updater.State.Available -> simple(
            "새 버전이 있어요",
            "지금 빌드 #${state.current} → 새 빌드 #${state.release.build}\n받아서 바로 설치할까요?",
            confirm = "업데이트",
            onConfirm = { updater.download(state.release) },
            dismiss = "나중에",
        )
        is Updater.State.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text("받는 중 ${state.percent / 5 * 5}%") },
            // 이잉크라 5% 단위로만 다시 그린다
            text = { LinearProgressIndicator(progress = { (state.percent / 5 * 5) / 100f }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {},
        )
        is Updater.State.Ready -> simple(
            "설치 준비 완료",
            "'설치'를 누르면 앱 안에서 바로 업데이트돼요. 앱이 잠깐 꺼졌다가 다시 켜져요.\n" +
                "처음 한 번만 '이 출처 허용'을 켜고 '업데이트할까요?'에 답하면, 다음부터는 묻지 않고 바로 업데이트돼요." +
                (updater.lastError?.let { "\n(지난 시도 오류: $it)" } ?: ""),
            confirm = "설치",
            onConfirm = { updater.install(state.file) },
            dismiss = "닫기",
        )
        Updater.State.Installing -> simple("업데이트 설치 중…", "끝나면 앱이 잠깐 꺼졌다가 다시 켜져요.\n안 켜지면 '업데이트 완료' 알림을 눌러 주세요.", confirm = null)
        is Updater.State.Failed -> simple("업데이트", state.message)
    }
}
