package com.aynvora.astro

import com.aynvora.astro.panchang.ReferenceComparisonStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ReferenceComparisonStatusTest {
    @Test
    fun exactAndToleranceMatchesAreDistinctStatuses() {
        assertEquals(7, ReferenceComparisonStatus.entries.size)
        assertNotEquals(
            ReferenceComparisonStatus.EXACT_MATCH,
            ReferenceComparisonStatus.TOLERANCE_MATCH
        )
    }
}
