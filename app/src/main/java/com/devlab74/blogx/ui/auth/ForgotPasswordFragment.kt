package com.devlab74.blogx.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.devlab74.blogx.databinding.FragmentForgotPasswordBinding
import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.DataStateChangeListener
import com.devlab74.blogx.ui.Response
import com.devlab74.blogx.ui.ResponseType
import com.devlab74.blogx.util.Constants
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ClassCastException
import javax.inject.Inject

@AuthScope
class ForgotPasswordFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    val viewModel: AuthViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cancelActiveJobs()
    }

    lateinit var stateChangeListener: DataStateChangeListener

    val webInteractionCallback: WebAppInterface.OnWebInteractionCallback = object: WebAppInterface.OnWebInteractionCallback {
        override fun onSuccess(email: String) {
            Timber.d("onSuccess: a reset link will be sent to $email")
            onPasswordResetLinkSent()
        }

        override fun onError(errorMessage: String) {
            Timber.d("onError: $errorMessage")

            val dataState = DataState.error<Any>(
                response = Response(errorMessage, ResponseType.Dialog())
            )
            stateChangeListener.onDataStateChange(
                dataState = dataState
            )
        }

        override fun onLoading(isLoading: Boolean) {
            Timber.d("onLoading...")
            GlobalScope.launch(Main) {
                stateChangeListener.onDataStateChange(
                    DataState.loading(
                        isLoading = isLoading,
                        cachedData = null
                    )
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPasswordResetWebView()

        binding.returnToLauncherFragment.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun onPasswordResetLinkSent() {
        GlobalScope.launch(Main) {
            binding.parentView.removeView(binding.webview)
            binding.webview.destroy()

            val animation =  TranslateAnimation(
                binding.passwordResetDoneContainer.width.toFloat(),
                0f,
                0f,
                0f
            )
            animation.duration = 500
            binding.passwordResetDoneContainer.startAnimation(animation)
            binding.passwordResetDoneContainer.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadPasswordResetWebView() {
        stateChangeListener.onDataStateChange(
            DataState.loading(
                isLoading = true,
                cachedData = null
            )
        )

        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                stateChangeListener.onDataStateChange(
                    DataState.loading(
                        isLoading = false,
                        cachedData = null
                    )
                )
            }
        }

        binding.webview.loadUrl(Constants.PASSWORD_RESET_URL)
        binding.webview.settings.javaScriptEnabled = true

        binding.webview.addJavascriptInterface(WebAppInterface(webInteractionCallback), "AndroidTextListener")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    class WebAppInterface
    constructor(
        private val callback: OnWebInteractionCallback
    ) {
        @JavascriptInterface
        fun onSuccess(email: String) {
            callback.onSuccess(email)
        }

        @JavascriptInterface
        fun onError(errorMessage: String) {
            callback.onError(errorMessage)
        }

        @JavascriptInterface
        fun onLoading(isLoading: Boolean) {
            callback.onLoading(isLoading)
        }

        interface OnWebInteractionCallback {
            fun onSuccess(email: String)

            fun onError(errorMessage: String)

            fun onLoading(isLoading: Boolean)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Timber.e("$context must implement DataStateChangeListener")
        }
    }
}