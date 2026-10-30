plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    // 화면 캡처 테스트 (GitHub 빌드에서 화면을 이미지로 찍어 확인)
    id("app.cash.paparazzi") version "1.3.5" apply false
}
