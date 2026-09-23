package com.aynvora.designsystem.components

import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.components.navigation.AynvoraNavigationItem
import com.aynvora.designsystem.components.sheets.AynvoraBottomSheetDefaults
import dev.ishant.cottonsheet.CottonSheetController
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
    fun cottonSheetControllerManagesStack() {
        val controller = dev.ishant.cottonsheet.CottonSheetController()
        assertEquals(0, controller.stack.size)

        controller.show { }
        assertEquals(1, controller.stack.size)

        controller.show { }
        assertEquals(2, controller.stack.size)

        controller.dismiss()
        assertEquals(1, controller.stack.size)

        controller.dismissAll()
        assertEquals(0, controller.stack.size)
    }

    @Test
    fun bottomSheetDefaultsAdhereToDesignSystemTokens() {
        assertEquals(560.dp, AynvoraBottomSheetDefaults.SheetMaxWidth)
        assertEquals(16.dp, AynvoraBottomSheetDefaults.SheetCornerRadius)
    }

    @Test
    fun popBoxControllerManagesStack() {
        val controller = dev.ishant.popbox.PopBoxController()
        assertEquals(0, controller.stack.size)

        controller.show { }
        assertEquals(1, controller.stack.size)

        controller.show { }
        assertEquals(2, controller.stack.size)

        controller.dismiss()
        assertEquals(1, controller.stack.size)

        controller.dismissAll()
        assertEquals(0, controller.stack.size)
    }

    @Test
    fun dialogDefaultsAdhereToDesignSystemTokens() {
        assertEquals(16.dp, com.aynvora.designsystem.components.dialogs.AynvoraDialogDefaults.DialogCornerRadius)
        assertEquals(24.dp, com.aynvora.designsystem.components.dialogs.AynvoraDialogDefaults.DialogHorizontalPadding)
        assertEquals(24.dp, com.aynvora.designsystem.components.dialogs.AynvoraDialogDefaults.DialogContentPadding)
    }
}

