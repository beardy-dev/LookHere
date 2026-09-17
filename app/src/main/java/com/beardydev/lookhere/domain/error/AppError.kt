package com.beardydev.lookhere.domain.error

import java.io.IOException

sealed interface AppError {
    data object Network : AppError
    data object ApiKeyMissing : AppError
    data class Unknown(val message: String? = null) : AppError
}

fun Throwable.toAppError(): AppError = when (this) {
    is IOException -> AppError.Network
    else -> AppError.Unknown(message)
}
