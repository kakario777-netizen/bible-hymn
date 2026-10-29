package com.kakario.biblehymn.data

import android.content.Context

/** 마지막으로 읽은 위치, 글자 크기 등 간단한 설정 저장 */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("bible_hymn", Context.MODE_PRIVATE)

    var lastBook: Int
        get() = sp.getInt("lastBook", 0)
        set(v) = sp.edit().putInt("lastBook", v).apply()

    var lastChapter: Int
        get() = sp.getInt("lastChapter", 1)
        set(v) = sp.edit().putInt("lastChapter", v).apply()

    var fontSize: Float
        get() = sp.getFloat("fontSize", 19f)
        set(v) = sp.edit().putFloat("fontSize", v).apply()

    var lastHymn: Int
        get() = sp.getInt("lastHymn", 0)
        set(v) = sp.edit().putInt("lastHymn", v).apply()
}
