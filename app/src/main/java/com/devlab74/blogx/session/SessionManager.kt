package com.devlab74.blogx.session

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.persistence.AuthTokenDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
    private val authTokenDao: AuthTokenDao,
    val application: Application
){

    private val _cachedToken = MutableLiveData<AuthToken>()

    val cachedToken: LiveData<AuthToken> get() = _cachedToken

    fun login(newValue: AuthToken) {
        setValue(newValue)
    }

    fun logout() {
        Timber.d("logout...")

        GlobalScope.launch(IO) {
            var errorMessage: String? = null
            try {
                // Removing token from local DB
                cachedToken.value!!.accountId.let {
                    authTokenDao.nullifyToken(it)
                }
            } catch (e: CancellationException) {
                Timber.e("Logout: ${e.message}")
                errorMessage = e.message
            }
            catch (e: Exception) {
                Timber.e("Logout: ${e.message}")
                errorMessage += "\n ${e.message}"
            }
            finally {
                errorMessage?.let {
                    Timber.e("Logout: $errorMessage")
                }
                Timber.d("Logout: finally...")
                setValue(null)
            }
        }
    }

    fun setValue(newValue: AuthToken?) {
        GlobalScope.launch(Main) {
            if (_cachedToken.value != newValue) {
                _cachedToken.value = newValue
            }
        }
    }
}