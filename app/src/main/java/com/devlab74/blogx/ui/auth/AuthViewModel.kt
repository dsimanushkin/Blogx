package com.devlab74.blogx.ui.auth

import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.repository.auth.AuthRepository
import com.devlab74.blogx.ui.BaseViewModel
import com.devlab74.blogx.ui.auth.state.AuthStateEvent
import com.devlab74.blogx.ui.auth.state.AuthViewState
import com.devlab74.blogx.ui.auth.state.LoginFields
import com.devlab74.blogx.ui.auth.state.RegistrationFields
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.ErrorHandling.Companion.INVALID_STATE_EVENT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@AuthScope
class AuthViewModel
@Inject
constructor(
    private val authRepository: AuthRepository
): BaseViewModel<AuthViewState>() {

    override fun handleNewData(data: AuthViewState) {
        data.authToken?.let { authToken ->
            setAuthToken(authToken)
        }
    }

    override fun setStateEvent(stateEvent: StateEvent) {

        val job: Flow<DataState<AuthViewState>> = when(stateEvent){

            is AuthStateEvent.LoginAttemptEvent -> {
                authRepository.attemptLogin(
                    stateEvent = stateEvent,
                    username = stateEvent.username,
                    password = stateEvent.password
                )
            }

            is AuthStateEvent.RegisterAttemptEvent -> {
                authRepository.attemptRegistration(
                    stateEvent = stateEvent,
                    email = stateEvent.email,
                    username = stateEvent.username,
                    password = stateEvent.password,
                    confirmPassword = stateEvent.confirmPassword
                )
            }

            is AuthStateEvent.CheckPreviousAuthEvent -> {
                authRepository.checkPreviousAuthUser(stateEvent)
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

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    fun setRegistrationFields(registrationFields: RegistrationFields){
        val update = getCurrentViewStateOrNew()
        if(update.registrationFields == registrationFields){
            return
        }
        update.registrationFields = registrationFields
        setViewState(update)
    }

    fun setLoginFields(loginFields: LoginFields){
        val update = getCurrentViewStateOrNew()
        if(update.loginFields == loginFields){
            return
        }
        update.loginFields = loginFields
        setViewState(update)
    }

    private fun setAuthToken(authToken: AuthToken){
        val update = getCurrentViewStateOrNew()
        if(update.authToken == authToken){
            return
        }
        update.authToken = authToken
        setViewState(update)
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}