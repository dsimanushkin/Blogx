package com.devlab74.blogx.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.devlab74.blogx.databinding.ActivityMainBinding
import com.devlab74.blogx.ui.BaseActivity
import com.devlab74.blogx.ui.auth.AuthActivity
import timber.log.Timber

class MainActivity: BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.setOnClickListener {
            sessionManager.logout()
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Timber.d("MainActivity: subscribeObservers: AuthToken: $authToken")
            if (authToken == null || authToken.accountPk == -1 || authToken.authToken == null) {
                navAuthActivity()
            }
        })
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}