package com.devlab74.blogx.ui.main.account

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentUpdateAccountBinding
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.ui.main.account.state.AccountStateEvent
import timber.log.Timber

class UpdateAccountFragment: BaseAccountFragment() {
    private var _binding: FragmentUpdateAccountBinding? = null
    private val binding get() = _binding!!

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

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.onDataStateChange(dataState)
            Timber.d("UpdateAccountFragment: $dataState")
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer {viewState ->
            if (viewState != null) {
                viewState.accountProperties?.let {
                    Timber.d("UpdateAccountFragment: ViewState: $it")
                    setAccountDataFields(it)
                }
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        binding.inputEmail.setText(accountProperties.email)
        binding.inputUsername.setText(accountProperties.username)
    }

    private fun saveChanges() {
        viewModel.setStateEvent(
            AccountStateEvent.UpdateAccountPropertiesEvent(
                binding.inputEmail.text.toString(),
                binding.inputUsername.text.toString()
            )
        )
        stateChangeListener.hideSoftKeyboard()
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