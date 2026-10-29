package com.kakario.biblehymn.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/** 찬송 한 곡. pages = 악보 이미지(assets 경로) — 두 쪽짜리는 2개, 악보가 없는 곡은 비어 있음 */
data class Hymn(val number: Int, val title: String, val pages: List<String>) {
    val hasScore: Boolean get() = pages.isNotEmpty()
}

/** 찬송가 책 종류 (assets 폴더 이름, 화면 제목, 전체 곡 수) */
enum class SongBook(val dir: String, val title: String, val count: Int) {
    UNIFIED("hymn", "통일찬송가", 558),
    MYSOUL("mysoul", "내 영의 노래", 417),
}

/**
 * 찬송 데이터 (assets/<dir>)
 * - titles.txt : 번호|제목
 * - score/001.gif(.jpg) … 두 쪽짜리는 046-1.gif, 046-2.gif
 * - midi/001.mid … (반주 — 아직 비어 있음, 넣으면 바로 재생됨)
 */
object Hymns {
    private val cache = HashMap<SongBook, List<Hymn>>()
    private val midiCache = HashMap<SongBook, Map<Int, String>>()

    fun all(context: Context, book: SongBook): List<Hymn> {
        synchronized(cache) { cache[book]?.let { return it } }
        val files = context.assets.list("${book.dir}/score")?.toSet() ?: emptySet()
        val titles = context.assets.open("${book.dir}/titles.txt").bufferedReader(Charsets.UTF_8).use { r ->
            r.readLines()
                .filter { it.contains('|') }
                .mapNotNull { line ->
                    val parts = line.split('|', limit = 2)
                    parts[0].trim().toIntOrNull()?.let { it to parts[1].trim() }
                }
                .toMap()
        }
        fun pagesOf(num: Int): List<String> {
            val base = "%03d".format(num)
            for (ext in listOf("gif", "jpg", "png", "webp")) {
                if ("$base.$ext" in files) return listOf("${book.dir}/score/$base.$ext")
            }
            for (ext in listOf("gif", "jpg", "png", "webp")) {
                val multi = (1..9).map { "$base-$it.$ext" }.takeWhile { it in files }
                if (multi.isNotEmpty()) return multi.map { "${book.dir}/score/$it" }
            }
            return emptyList()
        }
        val list = (1..book.count).map { n ->
            val pages = pagesOf(n)
            Hymn(n, titles[n] ?: "(악보 없음)", pages)
        }
        synchronized(cache) { cache[book] = list }
        return list
    }

    /** 반주 MIDI 파일 경로 (없으면 null → '준비중') */
    fun midiPath(context: Context, book: SongBook, number: Int): String? {
        val map = synchronized(midiCache) { midiCache[book] } ?: run {
            val names = try {
                context.assets.list("${book.dir}/midi") ?: emptyArray()
            } catch (e: Exception) {
                emptyArray()
            }
            names.filter { it.endsWith(".mid", true) || it.endsWith(".midi", true) }
                .mapNotNull { name -> name.substringBefore('.').toIntOrNull()?.let { it to "${book.dir}/midi/$name" } }
                .toMap()
                .also { synchronized(midiCache) { midiCache[book] = it } }
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
