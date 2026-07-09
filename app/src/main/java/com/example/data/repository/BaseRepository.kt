package com.example.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Encapsulated wrapper representing operation outcomes.
 */
sealed interface Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>
    data class Error(val exception: Throwable, val message: String? = exception.message) : Resource<Nothing>
    object Loading : Resource<Nothing>
}

/**
 * Base Repository supplying safe execution logic to repositories for dealing with I/O and Db transactions.
 */
abstract class BaseRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Executes block on specified IO Dispatcher, catching database and network failures gracefully.
     */
    protected suspend fun <T> safeCall(
        block: suspend () -> T
    ): Resource<T> = withContext(ioDispatcher) {
        try {
            Resource.Success(block())
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}
