package com.devlab74.blogx.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devlab74.blogx.databinding.FragmentLoginBinding
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

        Timber.d("LoginFragment: ${viewModel.hashCode()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}