package com.asksakis.freegate.notifications

import java.time.LocalTime

/** The "HH:mm-HH:mm" window in which Outside mode lets gate alerts through while at home. */
object HomeHours {
    const val DEFAULT = "22:00-07:00"
    private val spec = Regex("""^\s*(\d{1,2}):(\d{2})\s*-\s*(\d{1,2}):(\d{2})\s*$""")

    private fun parse(s: String): Pair<LocalTime, LocalTime>? {
        val m = spec.matchEntire(s) ?: return null
        return runCatching {
            LocalTime.of(m.groupValues[1].toInt(), m.groupValues[2].toInt()) to
                LocalTime.of(m.groupValues[3].toInt(), m.groupValues[4].toInt())
        }.getOrNull()
    }

    /** True when alerts may notify at [now]. Blank or broken text never mutes: a typo must not silence the gate. */
    fun isActive(s: String, now: LocalTime): Boolean {
        val (start, end) = parse(s) ?: return true
        if (start == end) return true
        return if (start < end) now >= start && now < end else now >= start || now < end
    }

    fun startText(s: String): String? = parse(s)?.first?.toString()
}
