package com.aynvora.astro.kp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KP249EngineTest {

    @Test
    fun testTableContainsExactly249Subdivisions() {
        val table = KP249Engine.TABLE
        assertEquals(249, table.size, "KP table must contain exactly 249 subdivisions")
    }

    @Test
    fun testBoundaryContinuityAndZeroGapsZeroOverlaps() {
        val table = KP249Engine.TABLE

        // First subdivision must start at 0.0°
        assertEquals(0.0, table.first().startDegrees, 1e-9, "First subdivision must start at 0°")
        // Last subdivision must end at 360.0°
        assertEquals(360.0, table.last().endDegrees, 1e-9, "Last subdivision must end at 360°")

        // Continuous boundaries across all 249 subdivisions
        for (i in 0 until table.size - 1) {
            val curr = table[i]
            val next = table[i + 1]

            assertEquals(curr.index + 1, next.index)
            assertTrue(curr.endDegrees > curr.startDegrees, "Subdivision ${curr.index} must have positive arc")
            assertEquals(
                curr.endDegrees,
                next.startDegrees,
                1e-9,
                "Subdivision ${curr.index} end must equal Subdivision ${next.index} start (gap/overlap detected)"
            )
        }
    }

    @Test
    fun testSignBoundarySplitsPreserveLords() {
        // Sub 1: Ashwini 1 -> Ketu Star, Ketu Sub in Aries (Mars)
        val sub1 = KP249Engine.getByIndex(1)
        assertEquals("Aries", sub1.signName)
        assertEquals("Mars", sub1.signLord)
        assertEquals("Ashwini", sub1.nakshatraName)
        assertEquals("Ketu", sub1.starLord)
        assertEquals("Ketu", sub1.subLord)

        // Sub 249: Revati last sub -> Mercury Star, Saturn Sub in Pisces (Jupiter)
        val sub249 = KP249Engine.getByIndex(249)
        assertEquals("Pisces", sub249.signName)
        assertEquals("Jupiter", sub249.signLord)
        assertEquals("Revati", sub249.nakshatraName)
        assertEquals("Mercury", sub249.starLord)
        assertEquals("Saturn", sub249.subLord)
    }

    @Test
    fun testFindSubdivisionAtArbitraryLongitudes() {
        // 0.0° -> Sub 1
        assertEquals(1, KP249Engine.findSubdivision(0.0).index)
        // 359.99° -> Sub 249
        assertEquals(249, KP249Engine.findSubdivision(359.99).index)
        // Wraparound: 360.0° -> Sub 1 or Sub 249
        assertTrue(KP249Engine.findSubdivision(360.0).index in listOf(1, 249))
        // Negative degree: -0.5° (equivalent to 359.5°) -> Sub 249
        assertEquals(249, KP249Engine.findSubdivision(-0.5).index)
    }
}
