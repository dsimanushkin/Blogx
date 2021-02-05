package com.devlab74.blogx.ui.main.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.devlab74.blogx.databinding.FragmentChangePasswordBinding
import com.devlab74.blogx.ui.main.account.state.AccountStateEvent
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import timber.log.Timber

class ChangePasswordFragment: BaseAccountFragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.updatePasswordButton.setOnClickListener {
            viewModel.setStateEvent(
                AccountStateEvent.ChangePasswordEvent(
                    binding.inputCurrentPassword.text.toString(),
                    binding.inputNewPassword.text.toString(),
                    binding.inputConfirmNewPassword.text.toString()
                )
            )
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                Timber.d("ChangePasswordFragment: DataState: $dataState")
                if (dataState != null) {
                    dataState.data?.let { data ->
                        data.response?.let { event ->
                            if (event.peekContent().message == handleErrors(3006, activity?.application!!)) {
                                stateChangeListener.hideSoftKeyboard()
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}