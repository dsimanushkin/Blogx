package com.devlab74.blogx.ui.main.blog

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.devlab74.blogx.R
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.ui.UICommunicationListener
import com.devlab74.blogx.ui.main.blog.viewmodels.BlogViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import java.lang.ClassCastException

/**
 * BaseBlogFragment class that will be extended in Fragments that are under Auth part
 * Used to decrease amount of code that can be reused
 */

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
abstract class BaseBlogFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): Fragment() {

    val viewModel: BlogViewModel by viewModels{
        viewModelFactory
    }

    lateinit var uiCommunicationListener: UICommunicationListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(activity as AppCompatActivity)
        setupChannel()
    }

    private fun setupChannel() = viewModel.setupChannel()

    private fun setupActionBarWithNavController(activity: AppCompatActivity) {
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.blogFragment))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Timber.e("$context must implement UICommunicationListener")
        }
    }
}