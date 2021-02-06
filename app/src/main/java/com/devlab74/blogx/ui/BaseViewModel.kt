package com.devlab74.blogx.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devlab74.blogx.util.DataChannelManager
import com.devlab74.blogx.util.DataState
import com.devlab74.blogx.util.StateEvent
import com.devlab74.blogx.util.StateMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseViewModel<ViewState>: ViewModel() {

    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()

    val dataChannelManager: DataChannelManager<ViewState> = object: DataChannelManager<ViewState>() {
        override fun handleNewData(data: ViewState) {
            this@BaseViewModel.handleNewData(data)
        }
    }

    val viewState: LiveData<ViewState> get() = _viewState

    val numActiveJobs: LiveData<Int> = dataChannelManager.numActiveJobs

    val stateMessage: LiveData<StateMessage?> get() = dataChannelManager.messageStack.stateMessage

    // For debugging
    fun getMessageStackSize(): Int {
        return dataChannelManager.messageStack.size
    }

    fun setupChannel() = dataChannelManager.setupChannel()

    abstract fun handleNewData(data: ViewState)

    abstract fun setStateEvent(stateEvent: StateEvent)

    fun launchJob(
        stateEvent: StateEvent,
        jobFunction: Flow<DataState<ViewState>>
    ) {
        dataChannelManager.launchJob(stateEvent, jobFunction)
    }

    fun areAnyJobsActive(): Boolean {
        return dataChannelManager.numActiveJobs.value?.let {
            it > 0
        }?: false
    }

    fun isJobAlreadyActive(stateEvent: StateEvent): Boolean {
        Timber.d("isJobAlreadyActive?: ${dataChannelManager.isJobAlreadyActive(stateEvent)}")
        return dataChannelManager.isJobAlreadyActive(stateEvent)
    }

    fun getCurrentViewStateOrNew(): ViewState {
        return viewState.value?.let {
            it
        }?: initNewViewState()
    }

    fun setViewState(viewState: ViewState) {
        _viewState.value = viewState
    }

    fun clearStateMessage(index: Int = 0) {
        dataChannelManager.clearStateMessage(index)
    }

    open fun cancelActiveJobs() {
        if (areAnyJobsActive()) {
            Timber.d("cancel active jobs: ${dataChannelManager.numActiveJobs.value ?: 0}")
            dataChannelManager.cancelJobs()
        }
    }

    abstract fun initNewViewState(): ViewState
}