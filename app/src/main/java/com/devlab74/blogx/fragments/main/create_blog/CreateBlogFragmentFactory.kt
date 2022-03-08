package com.devlab74.blogx.fragments.main.create_blog

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.ui.main.create_blog.CreateBlogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

/**
 * Fragment factory for Main -> CreateBlog part of the app, used for instantiate fragment
 */

@MainScope
class CreateBlogFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
): FragmentFactory() {

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun instantiate(classLoader: ClassLoader, className: String) =
        when(className) {
            CreateBlogFragment::class.java.name -> {
                CreateBlogFragment(viewModelFactory, requestManager)
            }
            else -> {
                CreateBlogFragment(viewModelFactory, requestManager)
            }
        }
}