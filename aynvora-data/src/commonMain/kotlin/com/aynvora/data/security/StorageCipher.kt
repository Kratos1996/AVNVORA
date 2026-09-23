package com.aynvora.data.security

/**
 * Platform-independent abstraction for storage payload encryption/decryption at rest.
 *
 * Designed to adhere to docs/12_SECURITY.md and docs/28_THREAT_MODEL.md without inventing
 * custom crypto. Concrete implementations delegate to platform security facilities
 * (Android Keystore / EncryptedSharedPreferences, iOS Keychain, or JVM Keystore).
 */
interface StorageCipher {
    fun encrypt(plainText: String): String
    fun decrypt(cipherText: String): String
}

/**
 * Default pass-through cipher for unencrypted or test storage environments.
 */
class NoOpStorageCipher : StorageCipher {
    override fun encrypt(plainText: String): String = plainText
    override fun decrypt(cipherText: String): String = cipherText
}
