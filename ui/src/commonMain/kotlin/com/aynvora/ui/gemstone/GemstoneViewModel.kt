package com.aynvora.ui.gemstone

import androidx.lifecycle.viewModelScope
import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.core.event.AynvoraEffect
import com.aynvora.core.event.AynvoraEventDispatcher
import com.aynvora.core.gemstone.CertificateImageAnalyzer
import com.aynvora.core.gemstone.GemstoneAstrologyProfile
import com.aynvora.core.gemstone.GemstoneCatalog
import com.aynvora.core.gemstone.GemstoneCertificateInspectionResult
import com.aynvora.core.gemstone.GemstoneCompatibilityEngine
import com.aynvora.core.gemstone.GemstoneCompatibilityResult
import com.aynvora.core.gemstone.GemstoneDescriptor
import com.aynvora.core.gemstone.GemstoneInventoryItem
import com.aynvora.core.gemstone.GemstoneMetal
import com.aynvora.core.gemstone.GemstoneRecommendationEngine
import com.aynvora.core.gemstone.GemstoneRecommendationPackage
import com.aynvora.core.gemstone.GemstoneRepository
import com.aynvora.core.gemstone.GemstoneType
import com.aynvora.core.gemstone.GemstoneWearingContext
import com.aynvora.core.models.Rashi
import com.aynvora.core.report.GemstoneReportGenerator
import com.aynvora.core.report.GemstoneReportInput
import com.aynvora.core.report.ReportDocument
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.result.AynvoraResult
import com.aynvora.localization.report.AynvoraReportTextResolver
import com.aynvora.ui.base.AynvoraBaseViewModel
import kotlinx.coroutines.launch

enum class GemstoneTab {
    HOME,
    CATALOG,
    INVENTORY,
    COMPATIBILITY,
    RECOMMENDATIONS,
    CERTIFICATE,
    REPORT_PREVIEW,
}

data class GemstoneUiState(
    val currentTab: GemstoneTab = GemstoneTab.HOME,
    val catalog: List<GemstoneDescriptor> = GemstoneCatalog.NAVARATNA,
    val selectedGemstone: GemstoneDescriptor? = null,
    val inventory: List<GemstoneInventoryItem> = emptyList(),
    val isAddingGemstone: Boolean = false,
    val newGemType: GemstoneType = GemstoneType.RUBY,
    val newGemCarats: String = "3.5",
    val newGemMetal: GemstoneMetal = GemstoneMetal.GOLD,
    val newGemFinger: String = "Ring Finger",
    val newGemIsWorn: Boolean = true,
    val newGemNotes: String = "",
    val selectedLagna: Rashi = Rashi.ARIES,
    val selectedMoon: Rashi = Rashi.LEO,
    val recommendationPackage: GemstoneRecommendationPackage? = null,
    val compatGem1: GemstoneType = GemstoneType.RUBY,
    val compatGem2: GemstoneType = GemstoneType.BLUE_SAPPHIRE,
    val compatibilityResult: GemstoneCompatibilityResult? = null,
    val certificateResult: GemstoneCertificateInspectionResult? = null,
    val isInspectingCertificate: Boolean = false,
    val reportDocument: ReportDocument? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
)

sealed class GemstoneUiEvent(
    eventId: String,
    screenId: String = "gemstone_home",
    componentId: String = "gemstone",
) : AynvoraClickEvent(eventId, screenId, componentId) {

    data class SelectTab(val tab: GemstoneTab) :
        GemstoneUiEvent("gemstone.tab.selected", componentId = "tab_item")

    data class ViewGemstoneDetail(val descriptor: GemstoneDescriptor) :
        GemstoneUiEvent("gemstone.catalog.view_detail", componentId = "gem_card")

    data object BackToCatalog : GemstoneUiEvent("gemstone.detail.back", componentId = "back_button")

    data object OpenAddGemstone :
        GemstoneUiEvent("gemstone.inventory.open_add", componentId = "add_gem_fab")

    data object CancelAddGemstone :
        GemstoneUiEvent("gemstone.inventory.cancel_add", componentId = "cancel_button")

    data class UpdateNewGemType(val type: GemstoneType) :
        GemstoneUiEvent("gemstone.inventory.type_changed", componentId = "type_picker")

    data class UpdateNewGemCarats(val carats: String) :
        GemstoneUiEvent("gemstone.inventory.carats_changed", componentId = "carats_field")

    data class UpdateNewGemMetal(val metal: GemstoneMetal) :
        GemstoneUiEvent("gemstone.inventory.metal_changed", componentId = "metal_picker")

    data class UpdateNewGemFinger(val finger: String) :
        GemstoneUiEvent("gemstone.inventory.finger_changed", componentId = "finger_field")

    data class UpdateNewGemIsWorn(val isWorn: Boolean) :
        GemstoneUiEvent("gemstone.inventory.worn_changed", componentId = "worn_switch")

    data class UpdateNewGemNotes(val notes: String) :
        GemstoneUiEvent("gemstone.inventory.notes_changed", componentId = "notes_field")

    data object SubmitNewGemstone :
        GemstoneUiEvent("gemstone.inventory.submit", componentId = "save_button")

    data class DeleteInventoryItem(val id: String) :
        GemstoneUiEvent("gemstone.inventory.delete", componentId = "delete_button")

    data class ToggleWornStatus(val item: GemstoneInventoryItem) :
        GemstoneUiEvent("gemstone.inventory.toggle_worn", componentId = "worn_toggle")

    data class SelectLagna(val rashi: Rashi) :
        GemstoneUiEvent("gemstone.recs.lagna_changed", componentId = "lagna_picker")

    data class SelectMoon(val rashi: Rashi) :
        GemstoneUiEvent("gemstone.recs.moon_changed", componentId = "moon_picker")

    data object GenerateRecommendations :
        GemstoneUiEvent("gemstone.recs.generate", componentId = "generate_recs_button")

    data class SelectCompatGem1(val type: GemstoneType) :
        GemstoneUiEvent("gemstone.compat.gem1_changed", componentId = "gem1_picker")

    data class SelectCompatGem2(val type: GemstoneType) :
        GemstoneUiEvent("gemstone.compat.gem2_changed", componentId = "gem2_picker")

    data object RunCompatibilityCheck :
        GemstoneUiEvent("gemstone.compat.run_check", componentId = "check_compat_button")

    data class ProcessCertificateImage(val imageBytes: ByteArray, val isCamera: Boolean) :
        GemstoneUiEvent("gemstone.cert.image_captured", componentId = "image_input")

    data object GenerateReport :
        GemstoneUiEvent("gemstone.report.generate", componentId = "generate_report_button")

    data object DismissStatus :
        GemstoneUiEvent("gemstone.status.dismiss", componentId = "status_bar")
}

class GemstoneViewModel(
    private val repository: GemstoneRepository,
    private val certificateAnalyzer: CertificateImageAnalyzer,
    eventDispatcher: AynvoraEventDispatcher? = null,
) : AynvoraBaseViewModel<GemstoneUiEvent, GemstoneUiState, AynvoraEffect>(
    initialState = GemstoneUiState(),
    eventDispatcher = eventDispatcher,
) {
    init {
        registerEventHandler(::handleEvent)
        loadInventory()
        // Generate initial baseline recommendation
        computeRecommendations(currentState.selectedLagna, currentState.selectedMoon)
    }

    private fun loadInventory() {
        viewModelScope.launch {
            when (val res = repository.getInventory()) {
                is AynvoraResult.Success -> {
                    updateState { copy(inventory = res.value) }
                }

                is AynvoraResult.Failure -> {
                    // Fallback to empty inventory
                }
            }
        }
    }

    private fun computeRecommendations(lagna: Rashi, moon: Rashi) {
        val astro = GemstoneAstrologyProfile.createFromLagna(lagna, moon)
        val wearingContext = GemstoneWearingContext(currentState.inventory)
        val pkg = GemstoneRecommendationEngine.generateRecommendations(astro, wearingContext)
        updateState {
            copy(
                selectedLagna = lagna,
                selectedMoon = moon,
                recommendationPackage = pkg,
            )
        }
    }

    private suspend fun handleEvent(event: GemstoneUiEvent) {
        when (event) {
            is GemstoneUiEvent.SelectTab -> {
                updateState { copy(currentTab = event.tab, statusMessage = null) }
            }

            is GemstoneUiEvent.ViewGemstoneDetail -> {
                updateState { copy(selectedGemstone = event.descriptor) }
            }

            is GemstoneUiEvent.BackToCatalog -> {
                updateState { copy(selectedGemstone = null) }
            }

            is GemstoneUiEvent.OpenAddGemstone -> {
                updateState { copy(isAddingGemstone = true) }
            }

            is GemstoneUiEvent.CancelAddGemstone -> {
                updateState { copy(isAddingGemstone = false) }
            }

            is GemstoneUiEvent.UpdateNewGemType -> {
                updateState { copy(newGemType = event.type) }
            }

            is GemstoneUiEvent.UpdateNewGemCarats -> {
                updateState { copy(newGemCarats = event.carats) }
            }

            is GemstoneUiEvent.UpdateNewGemMetal -> {
                updateState { copy(newGemMetal = event.metal) }
            }

            is GemstoneUiEvent.UpdateNewGemFinger -> {
                updateState { copy(newGemFinger = event.finger) }
            }

            is GemstoneUiEvent.UpdateNewGemIsWorn -> {
                updateState { copy(newGemIsWorn = event.isWorn) }
            }

            is GemstoneUiEvent.UpdateNewGemNotes -> {
                updateState { copy(newGemNotes = event.notes) }
            }

            is GemstoneUiEvent.SubmitNewGemstone -> {
                val weight = currentState.newGemCarats.toDoubleOrNull() ?: 3.0
                val newItem = GemstoneInventoryItem(
                    id = "gem_inv_${currentState.newGemType.name.lowercase()}_${
                        kotlin.random.Random.nextInt(
                            1000,
                            9999
                        )
                    }",
                    type = currentState.newGemType,
                    approximateCaratWeight = weight,
                    metal = currentState.newGemMetal,
                    fingerOrPlacement = currentState.newGemFinger,
                    isCurrentlyWorn = currentState.newGemIsWorn,
                    notes = currentState.newGemNotes.ifBlank { null },
                )
                repository.saveInventoryItem(newItem)
                val updated =
                    listOf(newItem) + currentState.inventory.filter { it.id != newItem.id }
                updateState {
                    copy(
                        inventory = updated,
                        isAddingGemstone = false,
                        statusMessage = "Added ${newItem.type.sanskritName} to inventory.",
                    )
                }
                // Refresh recommendations with new inventory context
                computeRecommendations(currentState.selectedLagna, currentState.selectedMoon)
            }

            is GemstoneUiEvent.DeleteInventoryItem -> {
                repository.deleteInventoryItem(event.id)
                val updated = currentState.inventory.filter { it.id != event.id }
                updateState {
                    copy(
                        inventory = updated,
                        statusMessage = "Gemstone removed from inventory."
                    )
                }
                computeRecommendations(currentState.selectedLagna, currentState.selectedMoon)
            }

            is GemstoneUiEvent.ToggleWornStatus -> {
                val toggled = event.item.copy(isCurrentlyWorn = !event.item.isCurrentlyWorn)
                repository.saveInventoryItem(toggled)
                val updated =
                    currentState.inventory.map { if (it.id == toggled.id) toggled else it }
                updateState { copy(inventory = updated) }
                computeRecommendations(currentState.selectedLagna, currentState.selectedMoon)
            }

            is GemstoneUiEvent.SelectLagna -> {
                updateState { copy(selectedLagna = event.rashi) }
                computeRecommendations(event.rashi, currentState.selectedMoon)
            }

            is GemstoneUiEvent.SelectMoon -> {
                updateState { copy(selectedMoon = event.rashi) }
                computeRecommendations(currentState.selectedLagna, event.rashi)
            }

            is GemstoneUiEvent.GenerateRecommendations -> {
                computeRecommendations(currentState.selectedLagna, currentState.selectedMoon)
                updateState { copy(statusMessage = "Updated recommendations for ${currentState.selectedLagna.name} Lagna.") }
            }

            is GemstoneUiEvent.SelectCompatGem1 -> {
                updateState { copy(compatGem1 = event.type) }
            }

            is GemstoneUiEvent.SelectCompatGem2 -> {
                updateState { copy(compatGem2 = event.type) }
            }

            is GemstoneUiEvent.RunCompatibilityCheck -> {
                val res = GemstoneCompatibilityEngine.evaluatePair(
                    currentState.compatGem1,
                    currentState.compatGem2
                )
                updateState { copy(compatibilityResult = res) }
            }

            is GemstoneUiEvent.ProcessCertificateImage -> {
                updateState { copy(isInspectingCertificate = true) }
                when (val certRes = certificateAnalyzer.analyzeCertificate(event.imageBytes)) {
                    is AynvoraResult.Success -> {
                        updateState {
                            copy(
                                certificateResult = certRes.value,
                                isInspectingCertificate = false,
                                statusMessage = "Certificate metadata extracted successfully.",
                            )
                        }
                    }

                    is AynvoraResult.Failure -> {
                        updateState {
                            copy(
                                errorMessage = certRes.message,
                                isInspectingCertificate = false,
                            )
                        }
                    }
                }
            }

            is GemstoneUiEvent.GenerateReport -> {
                val pkg = currentState.recommendationPackage ?: return
                val generator = GemstoneReportGenerator()
                val resolver = AynvoraReportTextResolver(ReportLanguage.ENGLISH)
                val doc = generator.generate(
                    input = GemstoneReportInput(
                        language = ReportLanguage.ENGLISH,
                        generatedAtEpochMs = 1_700_000_000_000L,
                        recommendationPackage = pkg,
                        wearingContext = GemstoneWearingContext(currentState.inventory),
                    ),
                    resolver = resolver,
                )
                updateState {
                    copy(
                        reportDocument = doc,
                        currentTab = GemstoneTab.REPORT_PREVIEW,
                    )
                }
            }

            is GemstoneUiEvent.DismissStatus -> {
                updateState { copy(statusMessage = null, errorMessage = null) }
            }
        }
    }
}
