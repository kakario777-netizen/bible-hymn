package com.kakario.biblehymn.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** 성경 책갈피 하나 */
data class Bookmark(
    val id: Long,
    val name: String,
    val book: Int,
    val chapter: Int,
    val verse: Int,
    val createdAt: Long,
)

/** 이어읽기 위치, 책갈피, 글자 크기 등 설정 저장 */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("bible_hymn", Context.MODE_PRIVATE)

    // ── 이어읽기 (한 곳만, 성경 화면의 [이어읽기] 버튼으로 저장) ──
    var lastBook: Int
        get() = sp.getInt("lastBook", 0)
        set(v) = sp.edit().putInt("lastBook", v).apply()

    var lastChapter: Int
        get() = sp.getInt("lastChapter", 1)
        set(v) = sp.edit().putInt("lastChapter", v).apply()

    var lastVerse: Int
        get() = sp.getInt("lastVerse", 1)
        set(v) = sp.edit().putInt("lastVerse", v).apply()

    fun saveContinue(book: Int, chapter: Int, verse: Int) {
        sp.edit().putInt("lastBook", book).putInt("lastChapter", chapter).putInt("lastVerse", verse).apply()
    }

    // ── 화면 설정 ──
    var fontSize: Float
        get() = sp.getFloat("fontSize", 19f)
        set(v) = sp.edit().putFloat("fontSize", v).apply()

    // ── 성경 글꼴 상세 설정 ──
    /** 0 = 시스템 기본, 1 = 나눔고딕, 2 = 나눔명조, 3 = 고운바탕 */
    var fontFamily: Int
        get() = sp.getInt("fontFamily", 0)
        set(v) = sp.edit().putInt("fontFamily", v).apply()

    /** 굵기: 0 = 보통, 1 = 굵게, 2 = 더 굵게 (예전 '굵게' 설정은 2로 이어받음) */
    var fontWeightLevel: Int
        get() = sp.getInt("fontWeightLevel", if (sp.getBoolean("fontBold", false)) 2 else 0)
        set(v) = sp.edit().putInt("fontWeightLevel", v).apply()

    /** 줄 간격 (글자 크기의 배수) */
    var lineSpacing: Float
        get() = sp.getFloat("lineSpacing", 1.65f)
        set(v) = sp.edit().putFloat("lineSpacing", v).apply()

    /** 절과 절 사이 간격 (dp) */
    var verseGap: Float
        get() = sp.getFloat("verseGap", 10f)
        set(v) = sp.edit().putFloat("verseGap", v).apply()

    /** 좌우 여백 (dp) */
    var sideMargin: Float
        get() = sp.getFloat("sideMargin", 18f)
        set(v) = sp.edit().putFloat("sideMargin", v).apply()

    /** 성경·악보 화면 꺼짐: 0 = 시스템 설정, 1 = 30분, 2 = 2시간, 3 = 안 꺼짐 */
    var screenTimeout: Int
        get() = sp.getInt("screenTimeout", 0)
        set(v) = sp.edit().putInt("screenTimeout", v).apply()

    /** 절 번호를 본문 앞(왼쪽 위)에 작게 붙이기. false면 왼쪽 칸에 따로 */
    var verseNumInline: Boolean
        get() = sp.getBoolean("verseNumInline", true)
        set(v) = sp.edit().putBoolean("verseNumInline", v).apply()

    /** 절 번호 크기 (본문 글자 크기의 배수) */
    var verseNumScale: Float
        get() = sp.getFloat("verseNumScale", 0.6f)
        set(v) = sp.edit().putFloat("verseNumScale", v).apply()

    /** 전체화면일 때 위쪽에 작게 '창세기 1장' 표시 */
    var fullscreenHeader: Boolean
        get() = sp.getBoolean("fullscreenHeader", true)
        set(v) = sp.edit().putBoolean("fullscreenHeader", v).apply()

    /** 위치 표시 자리: 0~2 = 상단 왼쪽/가운데/오른쪽, 3~5 = 하단 왼쪽/가운데/오른쪽 */
    var fullscreenHeaderPos: Int
        get() = sp.getInt("fullscreenHeaderPos", 1)
        set(v) = sp.edit().putInt("fullscreenHeaderPos", v).apply()

    var fullscreenHeaderSize: Float
        get() = sp.getFloat("fullscreenHeaderSize", 11f)
        set(v) = sp.edit().putFloat("fullscreenHeaderSize", v).apply()

    /** 화면 왼쪽/오른쪽을 터치하면 이전/다음으로 넘기기 */
    var tapPaging: Boolean
        get() = sp.getBoolean("tapPaging", com.kakario.biblehymn.BuildConfig.IS_EINK) // 이북판은 기본 켜짐
        set(v) = sp.edit().putBoolean("tapPaging", v).apply()

    var lastHymn: Int
        get() = sp.getInt("lastHymn", 0)
        set(v) = sp.edit().putInt("lastHymn", v).apply()

    /** 책별 마지막으로 본 곡 */
    fun lastSong(book: SongBook): Int =
        if (book == SongBook.UNIFIED) lastHymn else sp.getInt("lastSong_${book.name}", 0)

    fun setLastSong(book: SongBook, number: Int) {
        if (book == SongBook.UNIFIED) lastHymn = number
        else sp.edit().putInt("lastSong_${book.name}", number).apply()
    }

    /** 찬송 화면에서 악보 대신 가사로 보기 */
    var hymnShowLyrics: Boolean
        get() = sp.getBoolean("hymnShowLyrics", false)
        set(v) = sp.edit().putBoolean("hymnShowLyrics", v).apply()

    /** 큰 악보를 한 화면에 맞춰 축소 */
    var fitScore: Boolean
        get() = sp.getBoolean("fitScore", false)
        set(v) = sp.edit().putBoolean("fitScore", v).apply()

    /** 배경색: 0 = 시스템 설정, 1 = 흰색, 2 = 검은색 */
    var themeMode: Int
        get() = sp.getInt("themeMode", 0)
        set(v) = sp.edit().putInt("themeMode", v).apply()

    /** 첫 화면 제목 밑 부제목 (비어 있으면 표시 안 함) */
    var homeSubtitle: String
        get() = sp.getString("homeSubtitle", "") ?: ""
        set(v) = sp.edit().putString("homeSubtitle", v).apply()

    // ── 책갈피 (개수 제한 없음) ──
    fun bookmarks(): List<Bookmark> {
        val raw = sp.getString("bookmarks", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Bookmark(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    book = o.getInt("book"),
                    chapter = o.getInt("chapter"),
                    verse = o.optInt("verse", 1),
                    createdAt = o.optLong("createdAt", 0L),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveBookmarks(list: List<Bookmark>) {
        val arr = JSONArray()
        list.forEach { b ->
            arr.put(
                JSONObject()
                    .put("id", b.id)
                    .put("name", b.name)
                    .put("book", b.book)
                    .put("chapter", b.chapter)
                    .put("verse", b.verse)
                    .put("createdAt", b.createdAt),
            )
        }
        sp.edit().putString("bookmarks", arr.toString()).apply()
    }

    fun addBookmark(name: String, book: Int, chapter: Int, verse: Int) {
        val now = System.currentTimeMillis()
        saveBookmarks(bookmarks() + Bookmark(now, name, book, chapter, verse, now))
    }

    fun renameBookmark(id: Long, name: String) {
        saveBookmarks(bookmarks().map { if (it.id == id) it.copy(name = name) else it })
    }

    fun deleteBookmark(id: Long) {
        saveBookmarks(bookmarks().filter { it.id != id })
    }

    /** 새 책갈피 기본 이름: 책갈피 1, 책갈피 2 … (안 겹치게) */
    fun nextBookmarkName(): String {
        val names = bookmarks().map { it.name }.toSet()
        var n = bookmarks().size + 1
        while ("책갈피 $n" in names) n++
        return "책갈피 $n"
    }
}
