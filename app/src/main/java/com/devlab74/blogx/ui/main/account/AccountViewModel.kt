package com.devlab74.blogx.ui.main.account

import androidx.lifecycle.LiveData
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.repository.main.AccountRepository
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.BaseViewModel
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.main.account.state.AccountStateEvent
import com.devlab74.blogx.ui.main.account.state.AccountViewState
import com.devlab74.blogx.util.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
): BaseViewModel<AccountStateEvent, AccountViewState>() {
    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        when(stateEvent) {
            is AccountStateEvent.GetAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.getAccountProperties(authToken)
                }?: AbsentLiveData.create()
            }
            is AccountStateEvent.UpdateAccountPropertiesEvent -> {
                return AbsentLiveData.create()
            }
            is AccountStateEvent.ChangePasswordEvent -> {
                return AbsentLiveData.create()
            }
            is AccountStateEvent.None -> {
                return AbsentLiveData.create()
            }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties == accountProperties) {
            return
        }
        update.accountProperties = accountProperties
        _viewState.value = update
    }

    fun logout() {
        sessionManager.logout()
    }
}