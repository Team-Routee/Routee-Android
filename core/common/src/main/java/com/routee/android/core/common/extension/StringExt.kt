package com.routee.android.core.common.extension

import java.text.BreakIterator

/**
 * 화면에 보이는 글자 단위의 길이
 *
 * 이모지처럼 여러 코드로 이루어진 글자도 1자로 센다
 */
fun CharSequence.graphemeLength(): Int {
    if (isEmpty()) return 0
    val iterator = BreakIterator.getCharacterInstance()
    iterator.setText(toString())
    var count = 0
    while (iterator.next() != BreakIterator.DONE) count++
    return count
}
