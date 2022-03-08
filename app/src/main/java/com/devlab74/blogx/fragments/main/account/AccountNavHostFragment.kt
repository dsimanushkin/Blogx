package com.devlab74.blogx.fragments.main.account

import android.content.Context
import android.os.Bundle
import androidx.annotation.NavigationRes
import androidx.navigation.fragment.NavHostFragment
import com.devlab74.blogx.ui.main.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * Custom Navigation Host class for Main -> Account part of the app (Multiple Navigation Graphs in use)
 */

class AccountNavHostFragment : NavHostFragment() {
    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onAttach(context: Context) {
        childFragmentManager.fragmentFactory = (activity as MainActivity).accountFragmentFactory
        super.onAttach(context)
    }

    companion object {
        private const val KEY_GRAPH_ID = "android-support-nav:fragment:graphId"

        @JvmStatic
        fun create(
            @NavigationRes graphId: Int = 0
        ): AccountNavHostFragment {
            var bundle: Bundle? = null
            if (graphId != 0) {
                bundle = Bundle()
                bundle.putInt(KEY_GRAPH_ID, graphId)
            }
            val result = AccountNavHostFragment()
            if (bundle != null) {
                result.arguments = bundle
            }
            return result

        }
    }
}