package com.aynvora.ui.astrology

import com.aynvora.core.models.AstroTableCell
import com.aynvora.core.models.AstroTableColumn
import com.aynvora.core.models.AstroValueType
import kotlin.test.Test
import kotlin.test.assertEquals

class AstrologyDataTableTest {
    @Test fun formatsTypedCellsWithoutChangingTheirValues() {
        assertEquals("123.457°", AstrologyTableFormatter.format(
            AstroTableCell(AstroValueType.ANGLE, numericValue = 123.456789),
            AstroTableColumn("longitude", "astrology.column.longitude", AstroValueType.ANGLE),
        ))
        assertEquals("Yes", AstrologyTableFormatter.format(
            AstroTableCell(AstroValueType.BOOLEAN, canonicalId = "true"),
            AstroTableColumn("retrograde", "astrology.column.retrograde", AstroValueType.BOOLEAN),
        ))
    }
}
