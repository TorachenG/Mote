package com.torachen.mote.ui.ai.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AiTaskSchedulerTest {

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun `once in the past has no next occurrence`() {
        val after = at(2026, 1, 1, 10, 0)
        assertNull(AiTaskScheduler.nextOccurrence(AiScheduleSpec.Once(after - 1), after))
        assertEquals(
            after + 1_000,
            AiTaskScheduler.nextOccurrence(AiScheduleSpec.Once(after + 1_000), after),
        )
    }

    @Test
    fun `daily rolls to tomorrow when time passed`() {
        val after = at(2026, 1, 1, 10, 0)
        val next = AiTaskScheduler.nextOccurrence(AiScheduleSpec.Daily(9, 0), after)
        assertEquals(at(2026, 1, 2, 9, 0), next)
    }

    @Test
    fun `daily stays today when time is ahead`() {
        val after = at(2026, 1, 1, 10, 0)
        val next = AiTaskScheduler.nextOccurrence(AiScheduleSpec.Daily(11, 30), after)
        assertEquals(at(2026, 1, 1, 11, 30), next)
    }

    @Test
    fun `interval is anchored after now`() {
        val after = 1_000_000L
        assertEquals(
            after + 120_000L,
            AiTaskScheduler.nextOccurrence(AiScheduleSpec.Every(120), after),
        )
    }

    @Test
    fun `weekly finds the next selected day`() {
        val after = at(2026, 1, 1, 10, 0)
        val next = AiTaskScheduler.nextOccurrence(AiScheduleSpec.Weekly(setOf(1), 9, 0), after)
        assertTrue(next != null && next > after)
    }
}
