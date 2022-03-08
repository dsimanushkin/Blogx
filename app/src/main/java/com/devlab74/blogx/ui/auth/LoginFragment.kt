package com.devlab74.blogx.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.devlab74.blogx.databinding.FragmentLoginBinding
import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.ui.auth.state.AuthStateEvent
import com.devlab74.blogx.ui.auth.state.LoginFields
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

/**
 * LoginFragment Class
 */

@FlowPreview
@ExperimentalCoroutinesApi
@AuthScope
class LoginFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory
): BaseAuthFragment(viewModelFactory) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()

        binding.loginButton.setOnClickListener {
            login()
        }
    }

    private fun login() {
        saveLoginFields()
        viewModel.setStateEvent(
            AuthStateEvent.LoginAttemptEvent(
                binding.inputUsername.text.toString(),
                binding.inputPassword.text.toString()
            )
        )
    }

    private fun saveLoginFields(){
        viewModel.setLoginFields(
            LoginFields(
                binding.inputUsername.text.toString(),
                binding.inputPassword.text.toString()
            )
        )
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.loginFields?.let { loginFields ->
                loginFields.loginUsername?.let {
                    binding.inputUsername.setText(it)
                }
                loginFields.loginPassword?.let {
                    binding.inputPassword.setText(it)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        saveLoginFields()
        _binding = null
    }
}