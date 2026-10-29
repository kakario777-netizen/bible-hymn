package com.kakario.biblehymn.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/** 통일찬송가 한 곡. pages = 악보 이미지(assets 경로) — 두 쪽짜리 곡은 2개 */
data class Hymn(val number: Int, val title: String, val pages: List<String>)

/**
 * 통일찬송가 558장 (assets/hymn)
 * - titles.txt : 번호|제목
 * - score/001.gif … (두 쪽짜리는 046-1.gif, 046-2.gif)
 * - midi/001.mid … (반주 — 아직 비어 있음, 넣으면 바로 재생됨)
 */
object Hymns {
    const val COUNT = 558

    @Volatile
    private var cache: List<Hymn>? = null

    @Volatile
    private var midiFiles: Map<Int, String>? = null

    fun all(context: Context): List<Hymn> {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val files = context.assets.list("hymn/score")?.toSet() ?: emptySet()
            val list = context.assets.open("hymn/titles.txt").bufferedReader(Charsets.UTF_8).use { r ->
                r.readLines()
                    .filter { it.contains('|') }
                    .map { line ->
                        val parts = line.split('|', limit = 2)
                        val num = parts[0].trim().toInt()
                        val base = "%03d".format(num)
                        val pages = if ("$base.gif" in files) {
                            listOf("hymn/score/$base.gif")
                        } else {
                            (1..9).map { "$base-$it.gif" }.takeWhile { it in files }.map { "hymn/score/$it" }
                        }
                        Hymn(num, parts[1].trim(), pages)
                    }
            }
            cache = list
            return list
        }
    }

    /** 반주 MIDI 파일 경로 (없으면 null → '준비중') */
    fun midiPath(context: Context, number: Int): String? {
        val map = midiFiles ?: run {
            val names = context.assets.list("hymn/midi") ?: emptyArray()
            names.filter { it.endsWith(".mid", true) || it.endsWith(".midi", true) }
                .mapNotNull { name -> name.substringBefore('.').toIntOrNull()?.let { it to "hymn/midi/$name" } }
                .toMap()
                .also { midiFiles = it }
        }
        return map[number]
    }
}

/** 악보 이미지 로더 (최근 이미지는 메모리에 보관) */
object ScoreImages {
    private val cache = object : LruCache<String, ImageBitmap>(64 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int = value.width * value.height * 4
    }

    fun cached(path: String): ImageBitmap? = cache.get(path)

    fun load(context: Context, path: String): ImageBitmap? {
        cache.get(path)?.let { return it }
        return try {
            context.assets.open(path).use { input ->
                val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                BitmapFactory.decodeStream(input, null, opts)?.asImageBitmap()
            }?.also { cache.put(path, it) }
        } catch (e: Exception) {
            null
        }
    }
}
