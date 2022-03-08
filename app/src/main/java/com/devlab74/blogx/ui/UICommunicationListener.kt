package com.devlab74.blogx.ui

import com.devlab74.blogx.util.Response
import com.devlab74.blogx.util.StateMessageCallback

/**
 * UICommunicationListener Interface
 */

interface UICommunicationListener {

    fun expandAppBar()

    fun hideSoftKeyboard()

    fun isStoragePermissionGranted(): Boolean

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

    fun displayProgressBar(isLoading: Boolean)

}