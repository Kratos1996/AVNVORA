package com.aynvora.core.garudapuran

import com.aynvora.core.result.AynvoraResult

class GetGarudaPuranSourceManifestUseCase(private val repository: GarudaPuranRepository) {
    fun execute(): GarudaSourceManifest = repository.getSourceManifest()
}

class GetGarudaPuranCatalogUseCase(private val repository: GarudaPuranRepository) {
    suspend fun execute(languageCode: String): AynvoraResult<GarudaPuranCatalog> =
        repository.getCatalog(languageCode)
}

class GetGarudaPuranTopicUseCase(private val repository: GarudaPuranRepository) {
    suspend fun execute(
        topicId: GarudaPuranTopicId,
        languageCode: String
    ): AynvoraResult<GarudaPuranTopicContent> =
        repository.getTopic(topicId, languageCode)
}

class GetGarudaPuranContentUseCase(private val repository: GarudaPuranRepository) {
    suspend fun execute(languageCode: String): AynvoraResult<List<GarudaPuranContentItem>> =
        repository.getAvailableContent(languageCode)
}
