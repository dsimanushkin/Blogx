package com.devlab74.blogx.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.Response
import com.devlab74.blogx.ui.ResponseType
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.Constants.Companion.NETWORK_TIMEOUT
import com.devlab74.blogx.util.Constants.Companion.TESTING_NETWORK_DELAY
import com.devlab74.blogx.util.ErrorHandling.Companion.ERROR_CHECK_NETWORK_CONNECTION
import com.devlab74.blogx.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.devlab74.blogx.util.ErrorHandling.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import com.devlab74.blogx.util.ErrorHandling.Companion.UNABLE_TO_RESOLVE_HOST
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber

abstract class NetworkBoundResource<ResponseObject, ViewStateType>(
    application: Application,
    isNetworkAvailable: Boolean
) {

    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob(application))
        setValue(DataState.loading(isLoading = true, cachedData = null))

        if (isNetworkAvailable) {
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
                    job.cancel(CancellationException(UNABLE_TO_RESOLVE_HOST))
                }
            }
        } else {
            onErrorReturn(errorMessage = UNABLE_TODO_OPERATION_WO_INTERNET, statusCode = 0, shouldUseDialog = true, shouldUseToast = false, application = application)
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
        if (msg == null) {
            msg = ERROR_UNKNOWN
        } else if (ErrorHandling.isNetworkError(msg)) {
            msg = ERROR_CHECK_NETWORK_CONNECTION
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
                    }?: onErrorReturn(errorMessage = ERROR_UNKNOWN, statusCode = 0, shouldUseDialog = false, shouldUseToast = true, application = application)
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

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)
    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>
    abstract fun setJob(job: Job)
}