package com.devlab74.blogx.ui.main.account

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentAccountBinding
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.main.account.state.AccountStateEvent
import timber.log.Timber
import javax.inject.Inject

class AccountFragment: BaseAccountFragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        binding.changePassword.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }
        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer {dataState ->
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                dataState?.let {
                    it.data?.let { data ->
                        data.data?.let { event ->
                            event.getContentIfNotHandled()?.let { viewState ->
                                viewState.accountProperties?.let { accountProperties ->
                                    Timber.d("AccountFragment: DataState: $accountProperties")
                                    viewModel.setAccountPropertiesData(accountProperties)
                                }
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer {viewState ->
            viewState?.let {
                it.accountProperties?.let {
                    Timber.d("AccountFragment: ViewState: $it")
                    setAccountDataFields(it)
                }
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        binding.email.text = accountProperties.email
        binding.username.text = accountProperties.username
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_view_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.edit) {
            findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        viewModel.setStateEvent(
            AccountStateEvent.GetAccountPropertiesEvent()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}