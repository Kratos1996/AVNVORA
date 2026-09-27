package com.aynvora.designsystem.components

import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.components.dialogs.AynvoraDialogDefaults
import com.aynvora.designsystem.components.navigation.AynvoraNavigationItem
import com.aynvora.designsystem.components.sheets.AynvoraBottomSheetDefaults
import dev.ishant.cottonsheet.CottonSheetController
import dev.ishant.popbox.PopBoxController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AynvoraComponentsTest {

    @Test
    fun buttonVariantsAreDefined() {
        val variants = AynvoraButtonVariant.entries
        assertEquals(4, variants.size)
        assertNotNull(AynvoraButtonVariant.Primary)
        assertNotNull(AynvoraButtonVariant.Secondary)
        assertNotNull(AynvoraButtonVariant.Outlined)
        assertNotNull(AynvoraButtonVariant.Ghost)
    }

    @Test
    fun cardVariantsAreDefined() {
        val variants = AynvoraCardVariant.entries
        assertEquals(3, variants.size)
        assertNotNull(AynvoraCardVariant.Elevated)
        assertNotNull(AynvoraCardVariant.Outlined)
        assertNotNull(AynvoraCardVariant.Filled)
    }

    @Test
    fun statusChipVariantsAreDefined() {
        val variants = AynvoraStatusChipVariant.entries
        assertEquals(6, variants.size)
        assertNotNull(AynvoraStatusChipVariant.Available)
        assertNotNull(AynvoraStatusChipVariant.Offline)
        assertNotNull(AynvoraStatusChipVariant.InDevelopment)
        assertNotNull(AynvoraStatusChipVariant.Warning)
        assertNotNull(AynvoraStatusChipVariant.Error)
        assertNotNull(AynvoraStatusChipVariant.Neutral)
    }

    @Test
    fun navigationItemContractIsValid() {
        val item = AynvoraNavigationItem(
            id = "charts",
            title = "Kundali",
            icon = {},
        )
        assertEquals("charts", item.id)
        assertEquals("Kundali", item.title)
    }

    @Test
    fun cottonSheetControllerInstantiates() {
        val controller = CottonSheetController()
        assertNotNull(controller)
    }

    @Test
    fun bottomSheetDefaultsAdhereToDesignSystemTokens() {
        assertEquals(560.dp, AynvoraBottomSheetDefaults.SheetMaxWidth)
        assertEquals(16.dp, AynvoraBottomSheetDefaults.SheetCornerRadius)
    }

    @Test
    fun popBoxControllerInstantiates() {
        val controller = PopBoxController()
        assertNotNull(controller)
    }

    @Test
    fun dialogDefaultsAdhereToDesignSystemTokens() {
        assertEquals(16.dp, AynvoraDialogDefaults.DialogCornerRadius)
        assertEquals(24.dp, AynvoraDialogDefaults.DialogHorizontalPadding)
        assertEquals(24.dp, AynvoraDialogDefaults.DialogContentPadding)
    }
}
