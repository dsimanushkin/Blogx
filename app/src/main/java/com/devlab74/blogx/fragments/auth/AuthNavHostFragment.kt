package com.devlab74.blogx.fragments.auth

import android.content.Context
import android.os.Bundle
import androidx.annotation.NavigationRes
import androidx.navigation.fragment.NavHostFragment
import com.devlab74.blogx.ui.auth.AuthActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * Custom Navigation Host class for Auth part of the app (Multiple Navigation Graphs in use)
 */

class AuthNavHostFragment : NavHostFragment() {
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onAttach(context: Context) {
        childFragmentManager.fragmentFactory = (activity as AuthActivity).fragmentFactory
        super.onAttach(context)
    }

    companion object {
        private const val KEY_GRAPH_ID = "android-support-nav:fragment:graphId"

        @JvmStatic
        fun create(
            @NavigationRes graphId: Int = 0
        ): AuthNavHostFragment {
            var bundle: Bundle? = null
            if (graphId != 0) {
                bundle = Bundle()
                bundle.putInt(KEY_GRAPH_ID, graphId)
            }
            val result = AuthNavHostFragment()
            if (bundle != null) {
                result.arguments = bundle
            }
            return result

        }
    }
}