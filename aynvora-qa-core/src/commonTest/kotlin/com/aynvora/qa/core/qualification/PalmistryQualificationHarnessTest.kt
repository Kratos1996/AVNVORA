package com.aynvora.qa.core.qualification

import com.aynvora.core.intelligence.CapabilityStatus
import com.aynvora.core.intelligence.ReleaseGateManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PalmistryQualificationHarnessTest {

    private val harness = PalmistryQualificationHarness()

    @Test
    fun testQualificationHarnessWithoutPhysicalHardwareReportsBlocked() {
        val report = harness.runQualificationSuite(
            testId = "qa_harness_test_01",
            deviceModel = "No Physical Device",
            androidVersion = "N/A",
            isPhysicalDeviceConnected = false,
        )

        assertEquals("BLOCKED", report.overallStatus)
        assertFalse(report.isPhysicalDeviceConnected)
        assertTrue(report.blockers.isNotEmpty())

        val physicalStages = report.stages.filter { stage ->
            stage.stageId in listOf(
                QualificationStageId.CAMERA_PERMISSION.name,
                QualificationStageId.CAMERA_OPEN.name,
                QualificationStageId.CAMERA_CAPTURE.name,
                QualificationStageId.HAND_DETECTION.name,
                QualificationStageId.HANDEDNESS_VERIFICATION.name,
                QualificationStageId.PDF_REOPEN.name,
                QualificationStageId.LIFECYCLE_TEST.name,
            )
        }

        // None of the physical stages may be converted to PASS
        for (stage in physicalStages) {
            assertEquals(QualificationStageStatus.BLOCKED.name, stage.status, "Stage ${stage.stageName} must be BLOCKED without physical device")
        }

        // Non-physical stages can still execute and PASS
        val nonPhysicalStages = report.stages.filter { stage ->
            stage.stageId in listOf(
                QualificationStageId.PALM_QUALITY.name,
                QualificationStageId.PALM_LINE_DETECTION.name,
                QualificationStageId.ANNOTATION.name,
                QualificationStageId.PALM_EVIDENCE.name,
                QualificationStageId.OFFLINE_VERIFICATION.name,
                QualificationStageId.PDF_GENERATION.name,
                QualificationStageId.REPEATED_ANALYSIS_TEST.name,
            )
        }

        for (stage in nonPhysicalStages) {
            assertEquals(QualificationStageStatus.PASS.name, stage.status, "Stage ${stage.stageName} should PASS local algorithmic contract")
        }
    }

    @Test
    fun testReleaseGateManagerPreventsPrematurePromotion() {
        // Capabilities requiring physical hardware
        val physicalCapIds = listOf("palm_camera", "palm_hand_detection", "palm_line_detection", "pdf_android_export")
        for (capId in physicalCapIds) {
            val entry = ReleaseGateManager.getEntry(capId)
            assertNotNull(entry, "Entry for $capId must exist in ReleaseGateManager")
            assertEquals(CapabilityStatus.FOUNDATION_ONLY, entry.status, "Capability $capId must remain FOUNDATION_ONLY")
            assertFalse(ReleaseGateManager.isEligibleForProduction(capId, isPhysicalDeviceAttached = false))
        }

        // Desktop PDF export has passed end-to-end testing
        val desktopPdfEntry = ReleaseGateManager.getEntry("pdf_desktop_export")
        assertNotNull(desktopPdfEntry)
        assertEquals(CapabilityStatus.IMPLEMENTED, desktopPdfEntry.status)
        assertTrue(ReleaseGateManager.isEligibleForProduction("pdf_desktop_export", isPhysicalDeviceAttached = false))
    }

    @Test
    fun testStructuredQaEvidenceOmitsBiometricPayloads() {
        val report = harness.runQualificationSuite(
            testId = "qa_harness_test_privacy",
            isPhysicalDeviceConnected = false,
        )

        // Ensure serialized evidence fields do not expose raw pixel arrays or landmark coordinates
        for (stage in report.stages) {
            assertFalse(stage.details.contains("ByteArray"), "Stage details must not contain raw byte arrays")
            assertFalse(stage.details.contains("Point3D"), "Stage details must not contain landmark coordinates")
            assertFalse(stage.details.contains("base64"), "Stage details must not contain base64 image strings")
        }
    }

    @Test
    fun testMockEvidenceCannotPromotePhysicalCapability() {
        val mockRecord = com.aynvora.core.intelligence.CapabilityQualificationRecord(
            qualificationId = "mock_test_qual_01",
            capabilityId = "palm_camera",
            device = "Mock Android Emulator",
            androidVersion = "14",
            appVersion = "10.40.0",
            timestampEpochMs = 1791100800000L,
            testScenario = "Simulated Camera Capture",
            result = com.aynvora.core.intelligence.QualificationResult.PASS,
            durationMs = 15,
            evidenceSource = com.aynvora.core.intelligence.EvidenceSource.MOCK,
            physicalDeviceRequired = true,
            physicalDeviceVerified = false,
        )

        // Even with physical device attached, MOCK evidence must be strictly rejected
        val isEligible = ReleaseGateManager.isEligibleForProduction(
            capabilityId = "palm_camera",
            isPhysicalDeviceAttached = true,
            overrideRecord = mockRecord,
        )
        assertFalse(isEligible, "MOCK evidence source must NEVER unlock physical capability")
    }

    @Test
    fun testCompletePhysicalEvidenceUnlocksPromotion() {
        val physicalRecord = com.aynvora.core.intelligence.CapabilityQualificationRecord(
            qualificationId = "phys_test_qual_01",
            capabilityId = "palm_camera",
            device = "Samsung Galaxy S23 Ultra (SM-S918B)",
            androidVersion = "16",
            appVersion = "10.40.0",
            timestampEpochMs = 1791100800000L,
            testScenario = "Real CameraX Preview & Sensor Capture",
            result = com.aynvora.core.intelligence.QualificationResult.PASS,
            confidence = 0.94f,
            durationMs = 38,
            error = null,
            evidenceSource = com.aynvora.core.intelligence.EvidenceSource.PHYSICAL_HARDWARE,
            physicalDeviceRequired = true,
            physicalDeviceVerified = true,
            artifactReference = "local://qa_reports/phys_camera_test_01.json",
        )

        val isEligible = ReleaseGateManager.isEligibleForProduction(
            capabilityId = "palm_camera",
            isPhysicalDeviceAttached = true,
            overrideRecord = physicalRecord,
        )
        assertTrue(isEligible, "Genuine PHYSICAL_HARDWARE evidence must unlock promotion")
    }
}

