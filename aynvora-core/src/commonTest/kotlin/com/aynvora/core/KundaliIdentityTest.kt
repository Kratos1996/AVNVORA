package com.aynvora.core

import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.Coordinates
import com.aynvora.core.models.KundaliIdentity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class KundaliIdentityTest {
    @Test
    fun identityIsStableAndChangesWhenBirthOrCalculationInputsChange() {
        val birth = BirthData(
            BirthDate(2000, 1, 2), BirthTime(3, 4, 5),
            BirthPlace("Delhi", Coordinates(28.6139, 77.2090), "Asia/Kolkata", id = "IN-DL-DEL"),
        )
        val config = CalculationConfig()
        assertEquals(KundaliIdentity.profileId(birth, config), KundaliIdentity.profileId(birth, config))
        assertNotEquals(KundaliIdentity.fingerprint(birth, config), KundaliIdentity.fingerprint(birth, config.copy(houseSystem = com.aynvora.core.models.HouseSystem.WHOLE_SIGN)))
        assertNotEquals(KundaliIdentity.fingerprint(birth, config), KundaliIdentity.fingerprint(birth.copy(time = BirthTime(3, 4, 6)), config))
    }
}
