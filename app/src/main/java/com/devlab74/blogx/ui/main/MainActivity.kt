package com.devlab74.blogx.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.ActivityMainBinding
import com.devlab74.blogx.ui.BaseActivity
import com.devlab74.blogx.ui.auth.AuthActivity
import com.devlab74.blogx.ui.main.account.ChangePasswordFragment
import com.devlab74.blogx.ui.main.account.UpdateAccountFragment
import com.devlab74.blogx.ui.main.blog.UpdateBlogFragment
import com.devlab74.blogx.ui.main.blog.ViewBlogFragment
import com.devlab74.blogx.util.BOTTOM_NAV_BACKSTACK_KEY
import com.devlab74.blogx.util.BottomNavController
import com.devlab74.blogx.util.setUpNavigation
import timber.log.Timber

class MainActivity: BaseActivity(),
    BottomNavController.NavGraphProvider,
    BottomNavController.OnNavigationGraphChanged,
    BottomNavController.OnNavigationReselectedListener
{
    private lateinit var binding: ActivityMainBinding

    private val bottomNavController: BottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            binding.mainNavHostFragment.id,
            R.id.nav_blog,
            this,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupBottomNavigationView(savedInstanceState)

        subscribeObservers()
    }

    private fun subscribeObservers() {
        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Timber.d("MainActivity: subscribeObservers: AuthToken: $authToken")
            if (authToken == null || authToken.accountId == "" || authToken.authToken == null) {
                navAuthActivity()
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
                val backstack = BottomNavController.BackStack()
                backstack.addAll(items.toTypedArray())
                bottomNavController.setupBottomNavigationBackStack(backstack)
            }
        }
    }

    override fun getNavGraphId(itemId: Int) = when(itemId) {
        R.id.nav_blog -> R.navigation.nav_blog
        R.id.nav_account -> R.navigation.nav_account
        R.id.nav_create_blog -> R.navigation.nav_create_blog
        else -> R.navigation.nav_blog
    }

    override fun onGraphChange() {
        expandAppBar()
    }

    override fun expandAppBar() {
        binding.appBar.setExpanded(true)
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

    private fun setupActionBar() {
        setSupportActionBar(binding.toolBar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun displayProgressBar(bool: Boolean) {
        if (bool) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}