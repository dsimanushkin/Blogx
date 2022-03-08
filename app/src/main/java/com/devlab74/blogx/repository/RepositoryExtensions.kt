package com.devlab74.blogx.repository

import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.Constants.Companion.CACHE_TIMEOUT
import com.devlab74.blogx.util.Constants.Companion.NETWORK_TIMEOUT
import com.devlab74.blogx.util.ErrorHandling.Companion.CACHE_ERROR_TIMEOUT
import com.devlab74.blogx.util.ErrorHandling.Companion.NETWORK_ERROR_TIMEOUT
import com.devlab74.blogx.util.ErrorHandling.Companion.UNKNOWN_ERROR
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException

// Suspend Function is responsible for completing an API Call
suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): ApiResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(NETWORK_TIMEOUT){
                ApiResult.Success(apiCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is TimeoutCancellationException -> {
                    val code = 408 // timeout error code
                    ApiResult.GenericError(code, NETWORK_ERROR_TIMEOUT)
                }
                is IOException -> {
                    ApiResult.NetworkError
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    ApiResult.GenericError(
                        code,
                        errorResponse
                    )
                }
                else -> {
                    ApiResult.GenericError(
                        null,
                        UNKNOWN_ERROR
                    )
                }
            }
        }
    }
}

// Suspend Function is responsible for completing a Cache Call
suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher,
    cacheCall: suspend () -> T?
): CacheResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(CACHE_TIMEOUT){
                CacheResult.Success(cacheCall.invoke())
            }
        } catch (throwable: Throwable) {
            when (throwable) {
                is TimeoutCancellationException -> {
                    CacheResult.GenericError(CACHE_ERROR_TIMEOUT)
                }
                else -> {
                    CacheResult.GenericError(UNKNOWN_ERROR)
                }
            }
        }
    }
}

// Function is responsible for building an error in case if Cache or API Call failed
fun <ViewState> buildError(
    message: String,
    uiComponentType: UIComponentType,
    stateEvent: StateEvent?
): DataState<ViewState> {
    return DataState.error(
        response = Response(
            message = "${stateEvent?.errorInfo()}\n\nReason: $message",
            uiComponentType = uiComponentType,
            messageType = MessageType.Error
        ),
        stateEvent = stateEvent
    )
}

// This function is responsible for converting HTTP error into user friendly message
private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.response()?.errorBody()?.string()
    } catch (exception: Exception) {
        UNKNOWN_ERROR
    }
}