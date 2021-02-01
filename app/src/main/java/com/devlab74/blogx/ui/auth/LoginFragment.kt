package com.devlab74.blogx.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.devlab74.blogx.databinding.FragmentLoginBinding
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.ui.auth.state.LoginFields
import com.devlab74.blogx.util.ApiEmptyResponse
import com.devlab74.blogx.util.ApiErrorResponse
import com.devlab74.blogx.util.ApiSuccessResponse
import timber.log.Timber

class LoginFragment : BaseAuthFragment() {

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

        binding.loginButton.setOnClickListener {
            viewModel.setAuthToken(
                AuthToken(
                    1,
                "asfsndfjsdngjkasd"
                )
            )
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.loginFields?.let { loginFields ->
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
        viewModel.setLoginFields(
            LoginFields(
                binding.inputUsername.text.toString(),
                binding.inputPassword.text.toString()
            )
        )
        _binding = null
    }
}