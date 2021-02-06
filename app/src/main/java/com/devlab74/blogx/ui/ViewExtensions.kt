package com.devlab74.blogx.ui

import android.app.Activity
import android.widget.Toast
import androidx.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog
import com.devlab74.blogx.R
import com.devlab74.blogx.util.StateMessageCallback

fun Activity.displayToast(
    @StringRes message: Int,
    stateMessageCallback: StateMessageCallback
) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    stateMessageCallback.removeMessageFromStack()
}

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