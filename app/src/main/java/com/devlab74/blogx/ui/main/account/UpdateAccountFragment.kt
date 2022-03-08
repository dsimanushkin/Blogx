package com.devlab74.blogx.ui.main.account

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentUpdateAccountBinding
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.devlab74.blogx.ui.main.account.state.AccountStateEvent
import com.devlab74.blogx.ui.main.account.state.AccountViewState
import com.devlab74.blogx.util.StateMessageCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

/**
 * UpdateAccountFragment Class
 */

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class UpdateAccountFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory
): BaseAccountFragment(viewModelFactory) {
    private var _binding: FragmentUpdateAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[ACCOUNT_VIEW_STATE_BUNDLE_KEY] as AccountViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
    }

    private fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->
            if(viewState != null){
                viewState.accountProperties?.let{
                    setAccountDataFields(it)
                }
            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer {
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->
            stateMessage?.let {
                uiCommunicationListener.onResponseReceived(
                    response = it.response,
                    stateMessageCallback = object: StateMessageCallback {
                        override fun removeMessageFromStack() {
                            viewModel.clearStateMessage()
                        }
                    }
                )
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        if (binding.inputEmail.text.isNullOrBlank()) {
            binding.inputEmail.setText(accountProperties.email)
        }
        if (binding.inputUsername.text.isNullOrBlank()) {
            binding.inputUsername.setText(accountProperties.username)
        }
    }

    private fun saveChanges() {
        viewModel.setStateEvent(
            AccountStateEvent.UpdateAccountPropertiesEvent(
                binding.inputEmail.text.toString(),
                binding.inputUsername.text.toString()
            )
        )
        uiCommunicationListener.hideSoftKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            saveChanges()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}