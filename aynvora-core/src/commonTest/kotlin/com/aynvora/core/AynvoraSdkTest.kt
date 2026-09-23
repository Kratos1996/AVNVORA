package com.aynvora.core

import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.Coordinates
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AynvoraSdkTest {

    private val sampleValidBirthData = BirthData(
        date = BirthDate(year = 1995, month = 5, day = 21),
        time = BirthTime(hour = 14, minute = 30, second = 0),
        place = BirthPlace(
            name = "Varanasi, India",
            coordinates = Coordinates(latitude = 25.3176, longitude = 82.9739),
            timezoneId = "Asia/Kolkata",
        ),
    )

    @Test
    fun publicApiConstructsValidRequest() {
        val request = ChartRequest(
            birthData = sampleValidBirthData,
            config = CalculationConfig(),
        )

        assertEquals("1995-05-21T14:30:00", request.birthData.toIsoDateTimeString())
        assertEquals(25.3176, request.birthData.place.coordinates.latitude)
        assertEquals(82.9739, request.birthData.place.coordinates.longitude)
        assertEquals("Asia/Kolkata", request.birthData.place.timezoneId)
    }

    @Test
    fun invalidBirthDateOrCoordinatesThrowIllegalArgumentExceptionOnConstruction() {
        // Invalid month
        assertFailsWith<IllegalArgumentException> {
            BirthDate(year = 2000, month = 13, day = 15)
        }

        // Invalid hour
        assertFailsWith<IllegalArgumentException> {
            BirthTime(hour = 25, minute = 0)
        }

        // Invalid latitude
        assertFailsWith<IllegalArgumentException> {
            Coordinates(latitude = 95.0, longitude = 0.0)
        }

        // Blank timezone
        assertFailsWith<IllegalArgumentException> {
            BirthPlace(coordinates = Coordinates(0.0, 0.0), timezoneId = "   ")
        }
    }

    @Test
    fun facadeDelegatesToEngineAndReturnsDecoupledResult() = runBlocking {
        val sdk = Aynvora.create()
        val result = sdk.calculateChart(sampleValidBirthData)

        assertTrue(result.isSuccess)
        val success = result as AynvoraResult.Success
        assertNotNull(success.value)

        // Verifies public response attributes
        assertEquals("0.2.0", success.value.engineVersion)
        assertEquals("CALCULATED", success.value.calculationStatus)
        assertEquals("1995-05-21T14:30:00", success.value.birthData.toIsoDateTimeString())

        // Requirement 4: Public result must not expose engine implementation types
        assertEquals("com.aynvora.core.models.ChartResult", success.value::class.qualifiedName)

        // Verifies engine metadata is present
        assertEquals("0.2.0", success.metadata.engineVersion)
        assertTrue(success.metadata.isDeterministic)
        assertTrue(success.metadata.supportedDomains.contains("PLANETARY_POSITIONS"))
    }

    @Test
    fun errorModelDistinguishesFailureCategories() {
        val inputFailure: AynvoraResult.Failure = AynvoraResult.Failure.InvalidInput(
            field = "latitude",
            message = "Latitude out of bounds",
        )
        val configFailure: AynvoraResult.Failure = AynvoraResult.Failure.UnsupportedConfiguration(
            message = "Profile not supported",
        )
        val calcFailure: AynvoraResult.Failure = AynvoraResult.Failure.CalculationFailure(
            code = "EPHEMERIS_UNAVAILABLE",
            message = "Ephemeris data missing",
        )
        val internalFailure: AynvoraResult.Failure = AynvoraResult.Failure.InternalFailure(
            message = "Unexpected calculation error",
        )

        assertEquals("latitude", (inputFailure as AynvoraResult.Failure.InvalidInput).field)
        assertTrue(configFailure is AynvoraResult.Failure.UnsupportedConfiguration)
        assertEquals("EPHEMERIS_UNAVAILABLE", (calcFailure as AynvoraResult.Failure.CalculationFailure).code)
        assertTrue(internalFailure is AynvoraResult.Failure.InternalFailure)
    }

    @Test
    fun engineMetadataCanBeDiscoveredWithoutCalculation() {
        val sdk = Aynvora.create()
        val metadata = sdk.getMetadata()

        assertEquals("0.2.0", metadata.engineVersion)
        assertTrue(metadata.isDeterministic)
    }
}

