package com.devlab74.blogx.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.devlab74.blogx.databinding.FragmentRegisterBinding
import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.ui.auth.state.AuthStateEvent
import com.devlab74.blogx.ui.auth.state.RegistrationFields
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

/**
 * RegisterFragment Class
 */

@FlowPreview
@ExperimentalCoroutinesApi
@AuthScope
class RegisterFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory
): BaseAuthFragment(viewModelFactory) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {
            register()
        }

        subscribeObservers()
    }

    private fun register() {
        viewModel.setStateEvent(
            AuthStateEvent.RegisterAttemptEvent(
                binding.inputEmail.text.toString(),
                binding.inputUsername.text.toString(),
                binding.inputPassword.text.toString(),
                binding.inputPasswordConfirm.text.toString()
            )
        )
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {viewState ->
            viewState.registrationFields?.let { registrationFields ->
                registrationFields.registrationEmail?.let {
                    binding.inputEmail.setText(it)
                }
                registrationFields.registrationUsername?.let {
                    binding.inputUsername.setText(it)
                }
                registrationFields.registrationPassword?.let {
                    binding.inputPassword.setText(it)
                }
                registrationFields.registrationConfirmPassword?.let {
                    binding.inputPasswordConfirm.setText(it)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setRegistrationFields(
            RegistrationFields(
                binding.inputEmail.text.toString(),
                binding.inputUsername.text.toString(),
                binding.inputPassword.text.toString(),
                binding.inputPasswordConfirm.text.toString()
            )
        )
        _binding = null
    }
}