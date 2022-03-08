package com.devlab74.blogx.ui.main.account.state

import android.os.Parcelable
import com.devlab74.blogx.models.AccountProperties
import kotlinx.android.parcel.Parcelize

/**
 * This class describes fields that Account part will hold
 */

const val ACCOUNT_VIEW_STATE_BUNDLE_KEY = "com.devlab74.blogx.ui.main.account.state.AccountViewState"

@Parcelize
class AccountViewState(
    var accountProperties: AccountProperties? = null
) : Parcelable