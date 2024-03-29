package com.devlab74.blogx.util

import com.devlab74.blogx.ui.AreYouSureCallback

/**
 * StateMessage class
 */

data class StateMessage(val response: Response)

data class Response(
    val message: String?,
    val uiComponentType: UIComponentType,
    val messageType: MessageType
)

sealed class UIComponentType{
    object Toast : UIComponentType()

    object Dialog : UIComponentType()

    class AreYouSureDialog(
        val callback: AreYouSureCallback
    ): UIComponentType()

    object None : UIComponentType()
}

sealed class MessageType{
    object Success : MessageType()

    object Error : MessageType()

    object Info : MessageType()

    object None : MessageType()
}

interface StateMessageCallback{
    fun removeMessageFromStack()
}