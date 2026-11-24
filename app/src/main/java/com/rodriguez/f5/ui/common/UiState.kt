package com.rodriguez.f5.ui.common

/**
 * The only shape a screen ever receives.
 *
 * Forcing every ViewModel through these four cases is what makes error
 * handling systematic instead of something each screen solves its own way:
 * a screen that forgets an error path will not compile the `when`.
 */
sealed interface UiState<out T> {

    data object Loading : UiState<Nothing>

    data class Success<T>(
        val data: T,
        /** True when the data comes from the local database and not the network. */
        val fromCache: Boolean = false,
        val syncedAtMillis: Long = 0L
    ) : UiState<T>

    data class Empty(val message: String) : UiState<Nothing>

    data class Error(val message: String) : UiState<Nothing>
}
