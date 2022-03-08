package com.devlab74.blogx.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.devlab74.blogx.BaseApplication
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.ActivityMainBinding
import com.devlab74.blogx.models.AUTH_TOKEN_BUNDLE_KEY
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.ui.BaseActivity
import com.devlab74.blogx.ui.auth.AuthActivity
import com.devlab74.blogx.ui.main.account.ChangePasswordFragment
import com.devlab74.blogx.ui.main.account.UpdateAccountFragment
import com.devlab74.blogx.ui.main.blog.UpdateBlogFragment
import com.devlab74.blogx.ui.main.blog.ViewBlogFragment
import com.devlab74.blogx.util.BOTTOM_NAV_BACKSTACK_KEY
import com.devlab74.blogx.util.BottomNavController
import com.devlab74.blogx.util.setUpNavigation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

/**
 * MainActivity Class
 */

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity: BaseActivity(),
    BottomNavController.OnNavigationGraphChanged,
    BottomNavController.OnNavigationReselectedListener
{
    private lateinit var binding: ActivityMainBinding

    override fun inject() {
        (application as BaseApplication).mainComponent().inject(this)
    }

    @Inject
    @Named("AccountFragmentFactory")
    lateinit var accountFragmentFactory: FragmentFactory

    @Inject
    @Named("BlogFragmentFactory")
    lateinit var blogFragmentFactory: FragmentFactory

    @Inject
    @Named("CreateBlogFragmentFactory")
    lateinit var createBlogFragmentFactory: FragmentFactory

    private val bottomNavController: BottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            binding.mainNavHostFragment.id,
            R.id.menu_nav_blog,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupBottomNavigationView(savedInstanceState)

        restoreSession(savedInstanceState)
        subscribeObservers()
    }

    override fun onGraphChange() {
        expandAppBar()
    }

    private fun subscribeObservers() {
        sessionManager.cachedToken.observe(this, Observer { authToken ->
            if (authToken == null || authToken.accountId == "" || authToken.authToken == null) {
                navAuthActivity()
                finish()
            }
        })
    }

    private fun setupBottomNavigationView(savedInstanceState: Bundle?){
        binding.bottomNavigationView.setUpNavigation(bottomNavController, this)
        if (savedInstanceState == null) {
            bottomNavController.setupBottomNavigationBackStack(null)
            bottomNavController.onNavigationItemSelected()
        }
        else{
            (savedInstanceState[BOTTOM_NAV_BACKSTACK_KEY] as IntArray?)?.let { items ->
                val backStack = BottomNavController.BackStack()
                backStack.addAll(items.toTypedArray())
                bottomNavController.setupBottomNavigationBackStack(backStack)
            }
        }
    }

    override fun onReselectNavItem(navController: NavController, fragment: Fragment) = when(fragment) {
        is ViewBlogFragment -> navController.navigate(R.id.action_viewBlogFragment_to_blogFragment)
        is UpdateBlogFragment -> navController.navigate(R.id.action_updateBlogFragment_to_blogFragment)
        is UpdateAccountFragment -> navController.navigate(R.id.action_updateAccountFragment_to_accountFragment)
        is ChangePasswordFragment -> navController.navigate(R.id.action_changePasswordFragment_to_accountFragment)
        else -> {
            // Do nothing
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun restoreSession(savedInstanceState: Bundle?){
        savedInstanceState?.get(AUTH_TOKEN_BUNDLE_KEY)?.let{ authToken ->
            sessionManager.setValue(authToken as AuthToken)
        }
    }

    override fun expandAppBar() {
        binding.appBar.setExpanded(true)
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
        (application as BaseApplication).releaseMainComponent()
    }

    override fun displayProgressBar(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolBar)
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(
            AUTH_TOKEN_BUNDLE_KEY,
            sessionManager.cachedToken.value
        )

        outState.putIntArray(
            BOTTOM_NAV_BACKSTACK_KEY,
            bottomNavController.navigationBackStack.toIntArray()
        )

        super.onSaveInstanceState(outState)
    }
}