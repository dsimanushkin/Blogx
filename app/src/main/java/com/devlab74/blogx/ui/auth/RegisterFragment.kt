package com.devlab74.blogx.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.devlab74.blogx.databinding.FragmentRegisterBinding
import com.devlab74.blogx.util.ApiEmptyResponse
import com.devlab74.blogx.util.ApiErrorResponse
import com.devlab74.blogx.util.ApiSuccessResponse
import timber.log.Timber

class RegisterFragment : BaseAuthFragment() {

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

        viewModel.testRegister().observe(viewLifecycleOwner, Observer { response ->
            when(response) {
                is ApiSuccessResponse -> {
                    Timber.d("REGISTRATION RESPONSE: ${response.body}")
                }
                is ApiErrorResponse -> {
                    Timber.d("REGISTRATION RESPONSE: ${response.errorMessage}")
                }
                is ApiEmptyResponse -> {
                    Timber.d("REGISTRATION RESPONSE: Empty Response")
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}