package com.example.hassanalhawary.domain.model

/**
 * A generic sealed class representing the result of an operation that can either
 * succeed with a value of type [T] or fail with an error of type [E].
 *
 * @param T The type of the success value.
 * @param E The type of the error value.
 */
sealed class WisdomResult<out T, out E> {

    /**
     * Represents a successful result with a [value].
     */
    data class Success<out T>(val value: T) : WisdomResult<T, Nothing>()

    /**
     * Represents a failed result with an [error].
     */
    data class Failure<out E>(val error: E) : WisdomResult<Nothing, E>()

    // Helper properties
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

}