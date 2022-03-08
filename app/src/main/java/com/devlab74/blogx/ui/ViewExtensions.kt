package com.devlab74.blogx.ui

import android.app.Activity
import android.widget.Toast
import com.devlab74.blogx.util.StateMessageCallback

/**
 * ViewExtensions File
 */

fun Activity.displayToast(
    message: String,
    stateMessageCallback: StateMessageCallback
) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    stateMessageCallback.removeMessageFromStack()
}

interface AreYouSureCallback {
    fun proceed()
    fun cancel()
}