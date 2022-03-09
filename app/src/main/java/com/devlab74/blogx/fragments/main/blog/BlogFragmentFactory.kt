package com.devlab74.blogx.fragments.main.blog

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.ui.main.blog.BlogFragment
import com.devlab74.blogx.ui.main.blog.UpdateBlogFragment
import com.devlab74.blogx.ui.main.blog.ViewBlogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

/**
 * Fragment factory for Main -> Blog part of the app, used for instantiate fragment
 */

@MainScope
class BlogFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestOptions: RequestOptions,
    private val requestManager: RequestManager
): FragmentFactory() {
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun instantiate(classLoader: ClassLoader, className: String) =
        when(className) {
            BlogFragment::class.java.name -> {
                BlogFragment(viewModelFactory)
            }
            ViewBlogFragment::class.java.name -> {
                ViewBlogFragment(viewModelFactory, requestManager)
            }
            UpdateBlogFragment::class.java.name -> {
                UpdateBlogFragment(viewModelFactory, requestManager)
            }
            else -> {
                BlogFragment(viewModelFactory)
            }
        }
}