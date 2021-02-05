package com.devlab74.blogx.ui.main.account

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.devlab74.blogx.R
import com.devlab74.blogx.di.Injectable
import com.devlab74.blogx.ui.DataStateChangeListener
import com.devlab74.blogx.ui.main.MainDependencyProvider
import com.devlab74.blogx.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.devlab74.blogx.ui.main.account.state.AccountViewState
import com.devlab74.blogx.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import com.devlab74.blogx.viewmodels.ViewModelProviderFactory
import dagger.android.support.DaggerFragment
import timber.log.Timber
import java.lang.ClassCastException
import java.lang.Exception
import javax.inject.Inject

abstract class BaseAccountFragment: Fragment(), Injectable {

    lateinit var stateChangeListener: DataStateChangeListener

    lateinit var dependencyProvider: MainDependencyProvider

    lateinit var viewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, dependencyProvider.getVMProviderFactory()).get(AccountViewModel::class.java)
        }?: throw Exception("Invalid Activity")

        cancelActiveJobs()

        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[ACCOUNT_VIEW_STATE_BUNDLE_KEY] as AccountViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    fun isViewModelInitialized() = ::viewModel.isInitialized

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.accountFragment, activity as AppCompatActivity)
    }

    fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity) {
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Timber.e("$context must implement DataStateChangeListener")
        }

        try {
            dependencyProvider = context as MainDependencyProvider
        } catch (e: ClassCastException) {
            Timber.e("$context must implement MainDependencyProvider")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (isViewModelInitialized()) {
            outState.putParcelable(
                ACCOUNT_VIEW_STATE_BUNDLE_KEY,
                viewModel.viewState.value
            )
        }

        super.onSaveInstanceState(outState)
    }
}