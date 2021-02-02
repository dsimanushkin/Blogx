package com.devlab74.blogx.ui.main.create_blog

import android.content.Context
import com.devlab74.blogx.ui.DataStateChangeListener
import dagger.android.support.DaggerFragment
import timber.log.Timber
import java.lang.ClassCastException

abstract class BaseCreateBlogFragment: DaggerFragment() {
    lateinit var stateChangeListener: DataStateChangeListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Timber.e("$context must implement DataStateChangeListener")
        }
    }
}