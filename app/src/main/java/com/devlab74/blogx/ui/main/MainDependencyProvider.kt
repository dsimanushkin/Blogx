package com.devlab74.blogx.ui.main

import com.bumptech.glide.RequestManager
import com.devlab74.blogx.viewmodels.ViewModelProviderFactory

interface MainDependencyProvider {

    fun getVMProviderFactory(): ViewModelProviderFactory

    fun getGlideRequestManager(): RequestManager

}