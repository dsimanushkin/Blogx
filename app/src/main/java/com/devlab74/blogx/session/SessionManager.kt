package com.devlab74.blogx.session

import android.app.Application
import com.devlab74.blogx.persistence.AuthTokenDao

class SessionManager
constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
){

}