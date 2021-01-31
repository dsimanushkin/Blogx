package com.devlab74.blogx.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devlab74.blogx.databinding.ActivityAuthBinding
import com.devlab74.blogx.ui.BaseActivity

class AuthActivity : BaseActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}