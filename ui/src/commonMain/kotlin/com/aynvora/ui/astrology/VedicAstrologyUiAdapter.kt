package com.aynvora.ui.astrology

import com.aynvora.core.models.AstrologySectionAvailability
import com.aynvora.core.models.KundaliSnapshot
import com.aynvora.core.models.KundaliSnapshotJson

/** Screen state is decoded from the persisted snapshot JSON; it contains no astrology formulas. */
data class VedicAstrologyUiModel(
    val snapshot: KundaliSnapshot,
    val sections: List<AstrologySectionAvailability>,
)

object VedicAstrologyJsonUiAdapter {
    fun map(payload: String): VedicAstrologyUiModel {
        val snapshot = KundaliSnapshotJson.decode(payload)
        require(snapshot.schemaVersion == KundaliSnapshot.CURRENT_SCHEMA_VERSION)
        return VedicAstrologyUiModel(snapshot, snapshot.availability)
    }
}
