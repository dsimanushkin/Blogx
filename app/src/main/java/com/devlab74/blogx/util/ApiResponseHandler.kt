package com.devlab74.blogx.util

import com.devlab74.blogx.util.ErrorHandling.Companion.NETWORK_ERROR

/**
 * Generic ApiResponseHandler Class
 */

abstract class ApiResponseHandler<ViewState, Data>(
    private val response: ApiResult<Data?>,
    private val stateEvent: StateEvent
) {

    suspend fun getResult(): DataState<ViewState>{
        return when(response){
            is ApiResult.GenericError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent.errorInfo()}\n\nReason: ${response.errorMessage.toString()}",
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    ),
                    stateEvent = stateEvent
                )
            }

            is ApiResult.NetworkError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent.errorInfo()}\n\nReason: $NETWORK_ERROR",
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    ),
                    stateEvent = stateEvent
                )
            }

            is ApiResult.Success -> {
                if(response.value == null){
                    DataState.error(
                        response = Response(
                            message = "${stateEvent.errorInfo()}\n\nReason: Data is NULL.",
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        ),
                        stateEvent = stateEvent
                    )
                }
                else{
                    handleSuccess(resultObj = response.value)
                }
            }
        }
    }
    abstract suspend fun handleSuccess(resultObj: Data): DataState<ViewState>
}