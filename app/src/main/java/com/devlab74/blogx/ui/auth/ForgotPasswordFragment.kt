package com.devlab74.blogx.ui.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.devlab74.blogx.databinding.FragmentForgotPasswordBinding
import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AuthScope
class ForgotPasswordFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory
): BaseAuthFragment(viewModelFactory) {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cancelActiveJobs()
    }

    private val webInteractionCallback = object: WebAppInterface.OnWebInteractionCallback {

        override fun onError(errorMessage: String) {
            Timber.d("onError: $errorMessage")
            uiCommunicationListener.onResponseReceived(
                response = Response(
                    message = errorMessage,
                    uiComponentType = UIComponentType.Dialog,
                    messageType = MessageType.Error
                ),
                stateMessageCallback = object: StateMessageCallback{
                    override fun removeMessageFromStack() {
                        viewModel.clearStateMessage()
                    }
                }
            )
        }

        override fun onSuccess(email: String) {
            Timber.d("onSuccess: a reset link will be sent to $email.")
            onPasswordResetLinkSent()
        }

        override fun onLoading(isLoading: Boolean) {
            Timber.d("onLoading...")
            uiCommunicationListener.displayProgressBar(isLoading)
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

    @SuppressLint("SetJavaScriptEnabled")
    fun loadPasswordResetWebView(){
        uiCommunicationListener.displayProgressBar(true)
        binding.webview.webViewClient = object: WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                uiCommunicationListener.displayProgressBar(false)
            }
        }
        binding.webview.loadUrl(Constants.PASSWORD_RESET_URL)
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.addJavascriptInterface(WebAppInterface(webInteractionCallback), "AndroidTextListener")
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

        interface OnWebInteractionCallback{
            fun onSuccess(email: String)

            fun onError(errorMessage: String)

            fun onLoading(isLoading: Boolean)
        }
    }

    fun onPasswordResetLinkSent(){
        CoroutineScope(Main).launch{
            binding.parentView.removeView(binding.webview)
            binding.webview.destroy()

            val animation = TranslateAnimation(
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}