package com.aynvora.contracts

/**
 * Deterministic outcome of an AYNVORA operation.
 * Prevents throwing unhandled exceptions across boundaries.
 */
sealed interface AynvoraResult<out T> {

    data class Success<out T>(
        val value: T,
        val metadata: AynvoraResponseMetadata? = null,
    ) : AynvoraResult<T>

    sealed interface Failure : AynvoraResult<Nothing> {
        val message: String

        data class InvalidInput(
            val field: String,
            override val message: String,
        ) : Failure

        data class UnsupportedConfiguration(
            override val message: String,
        ) : Failure

        data class CalculationFailure(
            val code: String,
            override val message: String,
        ) : Failure

        data class NotFound(
            val resourceId: String,
            override val message: String,
        ) : Failure

        data class SecurityViolation(
            override val message: String,
        ) : Failure

        data class InternalFailure(
            override val message: String,
            val causeThrowable: Throwable? = null,
        ) : Failure

        data class InternalError(
            val errorClass: String,
            override val message: String,
            val causeThrowable: Throwable? = null,
        ) : Failure
    }

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.value
}

inline fun <T> AynvoraResult<T>.onSuccess(action: (value: T) -> Unit): AynvoraResult<T> {
    if (this is AynvoraResult.Success) action(value)
    return this
}

inline fun <T> AynvoraResult<T>.onFailure(action: (failure: AynvoraResult.Failure) -> Unit): AynvoraResult<T> {
    if (this is AynvoraResult.Failure) action(this)
    return this
}
