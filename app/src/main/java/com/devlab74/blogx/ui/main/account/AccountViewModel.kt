package com.devlab74.blogx.ui.main.account

import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.repository.main.AccountRepository
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.BaseViewModel
import com.devlab74.blogx.ui.main.account.state.AccountStateEvent
import com.devlab74.blogx.ui.main.account.state.AccountViewState
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.ErrorHandling.Companion.INVALID_STATE_EVENT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
* AccountViewModel class is responsible for setting new State Events and for setting fields
* as well as containing method responsible for logging out user
*/

@ExperimentalCoroutinesApi
@FlowPreview
@MainScope
class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    private val accountRepository: AccountRepository
): BaseViewModel<AccountViewState>() {

    override fun handleNewData(data: AccountViewState) {
        data.accountProperties?.let { accountProperties ->
            setAccountPropertiesData(accountProperties)
        }
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        sessionManager.cachedToken.value?.let { authToken ->
            val job: Flow<DataState<AccountViewState>> = when(stateEvent){

                is AccountStateEvent.GetAccountPropertiesEvent -> {
                    accountRepository.getAccountProperties(
                        stateEvent = stateEvent,
                        authToken = authToken
                    )
                }

                is AccountStateEvent.UpdateAccountPropertiesEvent -> {
                    accountRepository.saveAccountProperties(
                        stateEvent = stateEvent,
                        authToken = authToken,
                        email = stateEvent.email,
                        username = stateEvent.username
                    )
                }

                is AccountStateEvent.ChangePasswordEvent -> {
                    accountRepository.updatePassword(
                        stateEvent = stateEvent,
                        authToken = authToken,
                        currentPassword = stateEvent.currentPassword,
                        newPassword = stateEvent.newPassword,
                        confirmNewPassword = stateEvent.confirmNewPassword
                    )
                }

                else -> {
                    flow{
                        emit(
                            DataState.error(
                                response = Response(
                                    message = INVALID_STATE_EVENT,
                                    uiComponentType = UIComponentType.None,
                                    messageType = MessageType.Error
                                ),
                                stateEvent = stateEvent
                            )
                        )
                    }
                }
            }
            launchJob(stateEvent, job)
        }
    }

    private fun setAccountPropertiesData(accountProperties: AccountProperties){
        val update = getCurrentViewStateOrNew()
        if(update.accountProperties == accountProperties){
            return
        }
        update.accountProperties = accountProperties
        setViewState(update)
    }

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    fun logout(){
        sessionManager.logout()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}