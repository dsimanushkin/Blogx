package com.devlab74.blogx.fragments.main.blog

import android.content.Context
import android.os.Bundle
import androidx.annotation.NavigationRes
import androidx.navigation.fragment.NavHostFragment
import com.devlab74.blogx.ui.main.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * Custom Navigation Host class for Main -> Blog part of the app (Multiple Navigation Graphs in use)
 */

class BlogNavHostFragment : NavHostFragment() {
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onAttach(context: Context) {
        childFragmentManager.fragmentFactory = (activity as MainActivity).blogFragmentFactory
        super.onAttach(context)
    }

    companion object {
        private const val KEY_GRAPH_ID = "android-support-nav:fragment:graphId"

        @JvmStatic
        fun create(
            @NavigationRes graphId: Int = 0
        ): BlogNavHostFragment {
            var bundle: Bundle? = null
            if (graphId != 0) {
                bundle = Bundle()
                bundle.putInt(KEY_GRAPH_ID, graphId)
            }
            val result = BlogNavHostFragment()
            if (bundle != null) {
                result.arguments = bundle
            }
            return result

        }
    }
}