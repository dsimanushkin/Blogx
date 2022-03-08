package com.devlab74.blogx.fragments.main.create_blog

import android.content.Context
import android.os.Bundle
import androidx.annotation.NavigationRes
import androidx.navigation.fragment.NavHostFragment
import com.devlab74.blogx.ui.main.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * Custom Navigation Host class for Main -> CreateBlog part of the app (Multiple Navigation Graphs in use)
 */

class CreateBlogNavHostFragment : NavHostFragment() {
    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onAttach(context: Context) {
        childFragmentManager.fragmentFactory = (activity as MainActivity).createBlogFragmentFactory
        super.onAttach(context)
    }

    companion object {
        private const val KEY_GRAPH_ID = "android-support-nav:fragment:graphId"

        @JvmStatic
        fun create(
            @NavigationRes graphId: Int = 0
        ): CreateBlogNavHostFragment {
            var bundle: Bundle? = null
            if (graphId != 0) {
                bundle = Bundle()
                bundle.putInt(KEY_GRAPH_ID, graphId)
            }
            val result = CreateBlogNavHostFragment()
            if (bundle != null) {
                result.arguments = bundle
            }
            return result

        }
    }
}