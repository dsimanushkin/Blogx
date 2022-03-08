package com.devlab74.blogx.ui.main.account.state

import com.devlab74.blogx.util.StateEvent

/**
 * This sealed class is holding states when calling API/Cache related to Main -> Account part
 */

sealed class AccountStateEvent: StateEvent {
    object GetAccountPropertiesEvent : AccountStateEvent() {
        override fun errorInfo(): String {
            return "Error retrieving account properties."
        }

        override fun toString(): String {
            return "GetAccountPropertiesEvent"
        }
    }

    data class UpdateAccountPropertiesEvent(
        val email: String,
        val username: String
    ): AccountStateEvent() {
        override fun errorInfo(): String {
            return "Error updating account properties."
        }

        override fun toString(): String {
            return "UpdateAccountPropertiesEvent"
        }
    }

    data class ChangePasswordEvent(
        val currentPassword: String,
        val newPassword: String,
        val confirmNewPassword: String
    ): AccountStateEvent() {
        override fun errorInfo(): String {
            return "Error changing password."
        }

        override fun toString(): String {
            return "ChangePasswordEvent"
        }
    }

    object None : AccountStateEvent() {
        override fun errorInfo(): String {
            return "None"
        }

        override fun toString(): String {
            return "None"
        }
    }
}