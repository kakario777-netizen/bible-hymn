package com.kakario.biblehymn.data

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 찬송가 MIDI 반주 재생기.
 * 안드로이드 기본 MediaPlayer가 .mid 파일을 직접 재생한다.
 * 지금은 assets/hymn/midi 폴더가 비어 있어서 버튼이 '준비중'으로 표시된다.
 */
class MidiPlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    var playingNumber by mutableStateOf<Int?>(null)
        private set

    fun play(book: SongBook, number: Int): Boolean {
        stop()
        val path = Hymns.midiPath(context, book, number) ?: return false
        return try {
            val afd = context.assets.openFd(path)
            val mp = MediaPlayer()
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.setOnCompletionListener { stop() }
            mp.prepare()
            mp.start()
            player = mp
            playingNumber = number
            true
        } catch (e: Exception) {
            stop()
            false
        }
    }

    fun stop() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        player?.release()
        player = null
        playingNumber = null
    }
}
