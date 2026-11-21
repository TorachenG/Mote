package com.torachen.mote.ui.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class AiBranchesTest {

    private fun message(
        id: Long,
        role: String,
        parent: Long? = null,
        selected: Long? = null,
    ) = AiUiMessage(
        id = id,
        role = role,
        initialContent = "m$id",
        parentId = parent,
        initialSelectedChildId = selected,
    )

    @Test
    fun `visible path follows selected children`() {
        val all = listOf(
            message(1, "user", selected = 2),
            message(2, "assistant", parent = 1, selected = 4),
            message(3, "user", parent = 1, selected = 6),
            message(4, "assistant", parent = 2),
            message(5, "assistant", parent = 2),
            message(6, "assistant", parent = 3),
        )
        assertEquals(listOf(1L, 2L, 4L), aiVisiblePath(all, 1).map { it.id })
        all.first { it.id == 2L }.selectedChildId = 5
        assertEquals(listOf(1L, 2L, 5L), aiVisiblePath(all, 1).map { it.id })
        all.first { it.id == 1L }.selectedChildId = 3
        assertEquals(listOf(1L, 3L, 6L), aiVisiblePath(all, 1).map { it.id })
    }

    @Test
    fun `version ids are siblings ordered by creation`() {
        val all = listOf(
            message(1, "user"),
            message(2, "assistant", parent = 1),
            message(3, "assistant", parent = 1),
            message(4, "assistant", parent = 1),
        )
        assertEquals(listOf(2L, 3L, 4L), aiVersionIds(all, 3))
        assertEquals(emptyList<Long>(), aiVersionIds(all, 99))
    }
}
