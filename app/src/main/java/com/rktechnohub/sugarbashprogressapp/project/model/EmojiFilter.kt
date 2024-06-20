package com.rktechnohub.sugarbashprogressapp.project.model

import android.text.InputFilter
import android.text.Spanned

/**
 * Created by Aiman on 20, May, 2024
 */
class EmojiFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        for (i in start until end) {
            val type = Character.getType(source!!.get(i)).toByte()
            if (type != Character.SURROGATE && type != Character.OTHER_SYMBOL) {
                return "" // Return an empty string to prevent non-emoji characters from being entered
            }
        }
        return null
    }

    fun CharSequence.codePointAt(index: Int): Int {
        return if (index in 0 until length - 1 && Character.isSurrogatePair(this[index], this[index + 1])) {
            Character.toCodePoint(this[index], this[index + 1])
        } else {
            this[index].code
        }
    }
}