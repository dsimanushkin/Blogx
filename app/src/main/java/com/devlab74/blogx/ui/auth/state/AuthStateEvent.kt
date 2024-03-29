package com.devlab74.blogx.ui.auth.state

import com.devlab74.blogx.util.StateEvent

/**
 * This sealed class is holding states when calling API/Cache related to Auth part
 */

sealed class AuthStateEvent: StateEvent {

    data class LoginAttemptEvent(
        val username: String,
        val password: String
    ): AuthStateEvent() {
        override fun errorInfo(): String {
            return "Login attempt failed."
        }

        override fun toString(): String {
            return "AuthStateEvent"
        }
    }

    data class RegisterAttemptEvent(
        val email: String,
        val username: String,
        val password: String,
        val confirmPassword: String
    ): AuthStateEvent() {
        override fun errorInfo(): String {
            return "Register attempt failed."
        }

        override fun toString(): String {
            return "RegisterAttemptEvent"
        }
    }

    object CheckPreviousAuthEvent : AuthStateEvent() {
        override fun errorInfo(): String {
            return "Error checking for previously authenticated user."
        }

        override fun toString(): String {
            return "CheckPreviousAuthEvent"
        }
    }

    object None : AuthStateEvent() {
        override fun errorInfo(): String {
            return "None"
        }

        override fun toString(): String {
            return "None"
        }
    }
}