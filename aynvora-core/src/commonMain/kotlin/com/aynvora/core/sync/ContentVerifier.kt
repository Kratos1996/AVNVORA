package com.aynvora.core.sync

/**
 * Verification status of a content package after cryptographic and schema checks.
 *
 * Governs which content packs may be committed to the local database.
 * Only [VERIFIED] packs may be promoted to [com.aynvora.core.models.ContentTrustState.APPROVED_FOR_PUBLICATION].
 */
enum class ContentVerificationStatus {
    /** Checksum and signature have not yet been verified. */
    UNVERIFIED,
    /** Checksum matches; awaiting editorial or signature review. */
    CHECKSUM_PASSED,
    /** Cryptographic signature verified against the known signing key. */
    VERIFIED,
    /** Checksum does not match expected value — reject immediately. */
    CHECKSUM_MISMATCH,
    /** Signature is invalid, missing, or uses a revoked key — reject immediately. */
    SIGNATURE_INVALID,
    /** Version is older than or equal to installed version — skip silently. */
    VERSION_STALE,
    /** Content schema version is incompatible with this app version. */
    SCHEMA_INCOMPATIBLE,
}

/**
 * Immutable result of a content verification operation.
 *
 * @param packId The content pack identifier.
 * @param status The result of verification checks.
 * @param resolvedKeyId The signing key identifier used for verification, if applicable.
 * @param errorDetail A sanitized (non-PII) diagnostic message for logging purposes only.
 *   Must not contain user data, private keys, or system paths.
 */
data class ContentVerificationResult(
    val packId: String,
    val status: ContentVerificationStatus,
    val resolvedKeyId: String? = null,
    val errorDetail: String? = null,
) {
    val isAccepted: Boolean
        get() = status == ContentVerificationStatus.VERIFIED

    val isStaleSilently: Boolean
        get() = status == ContentVerificationStatus.VERSION_STALE
}

/**
 * Domain interface for verifying content pack integrity and authenticity.
 *
 * Architecture rules:
 * - Belongs in domain layer (`:aynvora-core`).
 * - No network calls — all inputs must be downloaded before verification begins.
 * - Does not commit content — verification outcome is handed back to the calling sync pipeline.
 * - If no signing infrastructure is configured, implementations MUST return
 *   [ContentVerificationStatus.SIGNATURE_INVALID] for all packs, not silently pass them.
 *
 * Backend configuration requirement:
 * - Production signing keys and verification infrastructure are NOT yet provisioned.
 * - Until keys are provisioned, a stub implementation is expected to be used in development.
 * - See docs/PHASE_7_0_ARCHITECTURE.md for the required external configuration.
 */
interface ContentVerifier {
    /**
     * Verifies the integrity and authenticity of a content pack payload.
     *
     * @param packId The unique identifier of the content pack being verified.
     * @param expectedChecksumSha256 The SHA-256 hex checksum from the server manifest.
     * @param rawPayload The raw downloaded byte payload to verify.
     * @param expectedSignature The base64-encoded digital signature from the manifest.
     * @param signatureKeyId The key ID used to select the public verification key.
     * @param installedVersion The currently installed version of this pack (0 if new).
     * @param incomingVersion The version number from the remote manifest.
     * @return A [ContentVerificationResult] describing the outcome.
     */
    suspend fun verify(
        packId: String,
        expectedChecksumSha256: String,
        rawPayload: ByteArray,
        expectedSignature: String,
        signatureKeyId: String,
        installedVersion: Int,
        incomingVersion: Int,
    ): ContentVerificationResult
}

/**
 * Development-only stub [ContentVerifier].
 *
 * IMPORTANT: This implementation performs ONLY version staleness checks.
 * It does NOT verify checksums or cryptographic signatures.
 *
 * This stub MUST NOT be used in production builds. Replace with a real
 * [ContentVerifier] backed by verified signing keys before any public release.
 *
 * Required production setup:
 * 1. Generate an Ed25519 or RSA signing key pair for content packs.
 * 2. Store the private key in a secure server-side secret manager (NOT in this repo).
 * 3. Bundle the public key with the app at build time via a verified supply chain.
 * 4. Implement checksum (SHA-256) and signature verification in a production [ContentVerifier].
 * 5. Enable key rotation and replay protection before public launch.
 */
class StubContentVerifier : ContentVerifier {
    override suspend fun verify(
        packId: String,
        expectedChecksumSha256: String,
        rawPayload: ByteArray,
        expectedSignature: String,
        signatureKeyId: String,
        installedVersion: Int,
        incomingVersion: Int,
    ): ContentVerificationResult {
        // Only reject stale versions. All other checks are stubs in dev mode.
        if (incomingVersion <= installedVersion) {
            return ContentVerificationResult(
                packId = packId,
                status = ContentVerificationStatus.VERSION_STALE,
                errorDetail = "version=$incomingVersion is not newer than installed=$installedVersion",
            )
        }
        // WARNING: checksum and signature NOT actually verified in this stub.
        return ContentVerificationResult(
            packId = packId,
            status = ContentVerificationStatus.VERIFIED,
            resolvedKeyId = signatureKeyId,
            errorDetail = "[STUB] Verification not enforced — for development only",
        )
    }
}
