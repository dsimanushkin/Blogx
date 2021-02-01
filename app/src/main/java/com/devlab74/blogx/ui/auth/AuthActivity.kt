package com.devlab74.blogx.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.devlab74.blogx.databinding.ActivityAuthBinding
import com.devlab74.blogx.ui.BaseActivity
import com.devlab74.blogx.ui.main.MainActivity
import com.devlab74.blogx.viewmodels.ViewModelProviderFactory
import timber.log.Timber
import javax.inject.Inject

class AuthActivity : BaseActivity() {

    private lateinit var binding: ActivityAuthBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(this, Observer {
            it.authToken?.let {
                sessionManager.login(it)
            }
        })

        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Timber.d("AuthActivity: subscribeObservers: AuthToken: $authToken")
            if (authToken != null && authToken.accountPk != -1 && authToken.authToken != null) {
                navMainActivity()
            }
        })
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}