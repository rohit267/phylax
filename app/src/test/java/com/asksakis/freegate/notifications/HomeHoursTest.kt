package com.asksakis.freegate.notifications

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeHoursTest {
    private fun at(h: Int, m: Int = 0) = LocalTime.of(h, m)

    @Test fun overnightWindow() {
        val s = "22:00-07:00"
        assertTrue(HomeHours.isActive(s, at(22)))
        assertTrue(HomeHours.isActive(s, at(23, 30)))
        assertTrue(HomeHours.isActive(s, at(3)))
        assertTrue(HomeHours.isActive(s, at(6, 59)))
        assertFalse(HomeHours.isActive(s, at(7)))
        assertFalse(HomeHours.isActive(s, at(12)))
        assertFalse(HomeHours.isActive(s, at(21, 59)))
    }

    @Test fun sameDayWindow() {
        assertTrue(HomeHours.isActive("09:00-17:00", at(12)))
        assertFalse(HomeHours.isActive("09:00-17:00", at(18)))
    }

    @Test fun blankOrBrokenMeansAlwaysActive() {
        for (s in listOf("", "  ", "garbage", "25:00-07:00", "22:00")) assertTrue(s, HomeHours.isActive(s, at(12)))
    }

    @Test fun startTextForTheStatusLine() {
        assertEquals("22:00", HomeHours.startText("22:00-07:00"))
        assertEquals(null, HomeHours.startText("garbage"))
    }
}
