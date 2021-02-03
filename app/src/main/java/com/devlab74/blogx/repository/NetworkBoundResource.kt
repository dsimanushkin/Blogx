package com.devlab74.blogx.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.Response
import com.devlab74.blogx.ui.ResponseType
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.Constants.Companion.NETWORK_TIMEOUT
import com.devlab74.blogx.util.Constants.Companion.TESTING_CACHE_DELAY
import com.devlab74.blogx.util.Constants.Companion.TESTING_NETWORK_DELAY
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber

abstract class NetworkBoundResource<ResponseObject, CacheObject, ViewStateType>(
    application: Application,
    isNetworkAvailable: Boolean,
    isNetworkRequest: Boolean,
    shouldLoadFromCache: Boolean,
    shouldCancelIfNoInternet: Boolean
) {

    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob(application))
        setValue(DataState.loading(isLoading = true, cachedData = null))

        if (shouldLoadFromCache) {
            val dbSource = loadFromCache()
            result.addSource(dbSource) {
                result.removeSource(dbSource)
                setValue(DataState.loading(isLoading = true, cachedData = it))
            }
        }

        if (isNetworkRequest) {
            if (isNetworkAvailable) {
                doNetworkRequest(application)
            } else {
                if (shouldCancelIfNoInternet) {
                    onErrorReturn(errorMessage = handleErrors(9003, application), statusCode = 0, shouldUseDialog = true, shouldUseToast = false, application = application)
                } else {
                    doCacheRequest()
                }
            }
        } else {
            doCacheRequest()
        }
    }

    private fun doCacheRequest() {
        coroutineScope.launch {
            // Fake delay for testing cache
            delay(TESTING_CACHE_DELAY)

            // View data from cache ONLY and return
            createCacheRequestAndReturn()
        }
    }

    private fun doNetworkRequest(application: Application) {
        coroutineScope.launch {
            delay(TESTING_NETWORK_DELAY) // Network delay only for testing!

            withContext(Main) {
                // Making network call
                val apiResponse = createCall()
                result.addSource(apiResponse) {response ->
                    result.removeSource(apiResponse)

                    coroutineScope.launch {
                        handleNetworkCall(response, application)
                    }
                }
            }
        }
        GlobalScope.launch(IO) {
            delay(NETWORK_TIMEOUT)

            if (!job.isCompleted) {
                Timber.e("NetworkBoundResource: JOB NETWORK TIMEOUT")
                job.cancel(CancellationException(handleErrors(9002, application)))
            }
        }
    }

    suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>, application: Application) {
        when(response) {
            is ApiSuccessResponse -> {
                handleApiSuccessResponse(response)
            }
            is ApiErrorResponse -> {
                Timber.e("NetworkBoundResource: ${response.errorMessage}")
                onErrorReturn(errorMessage = response.errorMessage, statusCode = 0, shouldUseDialog = true, shouldUseToast = false, application = application)
            }
            is ApiEmptyResponse -> {
                Timber.e("NetworkBoundResource: Request returned NOTHING (HTTP 204)")
                onErrorReturn(errorMessage = "HTTP 204. Returned Nothing.", statusCode = 0, shouldUseDialog = true, shouldUseToast = false, application = application)
            }
        }
    }

    fun onCompleteJob(dataState: DataState<ViewStateType>) {
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dataState)
        }
    }

    private fun setValue(dataState: DataState<ViewStateType>) {
        result.value = dataState
    }

    fun onErrorReturn(errorMessage: String?, statusCode: Int?, shouldUseDialog: Boolean, shouldUseToast: Boolean, application: Application) {
        Timber.d("NetworkBoundResource: StatusCode: $statusCode")
        var msg = errorMessage ?: handleErrors(statusCode, application)
        Timber.d("NetworkBoundResource: ErrorMessage: $msg")
        var useDialog = shouldUseDialog
        var responseType: ResponseType = ResponseType.None()
        if (ErrorHandling.isNetworkError(msg, application)) {
            msg = handleErrors(9004, application)
            useDialog = false
        }
        if (shouldUseToast) {
            responseType = ResponseType.Toast()
        }
        if (useDialog) {
            responseType = ResponseType.Dialog()
        }

        // Complete job and emit DataState
        onCompleteJob(DataState.error(
            response = Response(
                message = msg,
                responseType = responseType
            )
        ))
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun initNewJob(application: Application): Job {
        Timber.d("initNewJob: called...")

        job = Job()
        job.invokeOnCompletion(onCancelling = true, invokeImmediately = true, handler = object : CompletionHandler {
            override fun invoke(cause: Throwable?) {
                if (job.isCancelled) {
                    Timber.e("NetworkBoundResource: Job has been cancelled.")
                    cause?.let {
                        // Show error dialog
                        onErrorReturn(errorMessage = it.message, statusCode = 0, shouldUseDialog = false, shouldUseToast = true, application = application)
                    }?: onErrorReturn(errorMessage = handleErrors(9001, application), statusCode = 0, shouldUseDialog = false, shouldUseToast = true, application = application)
                } else if (job.isCompleted) {
                    Timber.e("NetworkBoundResource: Job has been completed")
                    // Do nothing should be handled already
                }
            }
        })
        coroutineScope = CoroutineScope(IO + job)
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract suspend fun createCacheRequestAndReturn()

    abstract fun loadFromCache(): LiveData<ViewStateType>
    abstract suspend fun updateLocalDb(cachedObject: CacheObject?)

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)
    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>
    abstract fun setJob(job: Job)
}