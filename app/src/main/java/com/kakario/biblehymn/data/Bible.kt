package com.kakario.biblehymn.data

import android.content.Context

/** 성경 한 권의 정보. index는 1(창세기)~66(요한계시록). */
data class BibleBook(
    val index: Int,
    val name: String,
    val short: String,
    val chapterCount: Int,
) {
    val isNewTestament: Boolean get() = index >= 40
}

data class Verse(val number: Int, val text: String)

/**
 * 개역한글 성경 데이터 (assets/bible).
 * - books.txt : 번호|이름|약어|장수
 * - 01.txt ~ 66.txt : 장<TAB>절<TAB>본문
 */
object Bible {
    @Volatile
    private var booksCache: List<BibleBook>? = null
    private val bookTextCache = HashMap<Int, List<List<Verse>>>()

    fun books(context: Context): List<BibleBook> {
        booksCache?.let { return it }
        synchronized(this) {
            booksCache?.let { return it }
            val list = context.assets.open("bible/books.txt").bufferedReader(Charsets.UTF_8).use { r ->
                r.readLines()
                    .filter { it.isNotBlank() }
                    .map { line ->
                        val p = line.split('|')
                        BibleBook(p[0].trim().toInt(), p[1].trim(), p[2].trim(), p[3].trim().toInt())
                    }
            }
            booksCache = list
            return list
        }
    }

    fun book(context: Context, index: Int): BibleBook? = books(context).firstOrNull { it.index == index }

    /** 해당 권의 모든 장. 결과[장-1] = 그 장의 절 목록 */
    fun chapters(context: Context, index: Int): List<List<Verse>> {
        synchronized(bookTextCache) { bookTextCache[index]?.let { return it } }
        val byChapter = sortedMapOf<Int, MutableList<Verse>>()
        context.assets.open("bible/%02d.txt".format(index)).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach { line ->
                val p = line.split('\t', limit = 3)
                if (p.size == 3) {
                    val c = p[0].toIntOrNull() ?: return@forEach
                    val v = p[1].toIntOrNull() ?: return@forEach
                    byChapter.getOrPut(c) { mutableListOf() }.add(Verse(v, p[2]))
                }
            }
        }
        val result: List<List<Verse>> = byChapter.values.map { it.toList() }
        synchronized(bookTextCache) {
            // 메모리 절약: 최근 몇 권만 기억
            if (bookTextCache.size > 6) bookTextCache.clear()
            bookTextCache[index] = result
        }
        return result
    }
}
