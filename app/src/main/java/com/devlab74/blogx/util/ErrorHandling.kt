package com.devlab74.blogx.util

import android.app.Application
import com.devlab74.blogx.R

/**
 * This class with static fields is responsible for returning Errors text
 */

class ErrorHandling {
    companion object {

        const val NETWORK_ERROR = "Network Error"
        const val NETWORK_ERROR_TIMEOUT = "Network Timeout"
        const val CACHE_ERROR_TIMEOUT = "Cache Timeout"
        const val UNKNOWN_ERROR = "Unknown Error"
        const val INVALID_STATE_EVENT = "Invalid state event"

        fun handleErrors(statusCode: Int?, application: Application): String {
            return when(statusCode) {
                1011 -> application.getString(R.string.username_field_required)
                1012 -> application.getString(R.string.account_with_this_username_already_exists)
                1013 -> application.getString(R.string.username_too_short)
                1014 -> application.getString(R.string.username_too_long)

                1021 -> application.getString(R.string.email_field_required)
                1022 -> application.getString(R.string.account_with_this_email_already_exists)
                1023 -> application.getString(R.string.invalid_email_format)

                1031 -> application.getString(R.string.password_field_required)
                1032 -> application.getString(R.string.password_is_too_short)
                1033 -> application.getString(R.string.passwords_does_not_match)

                2011 -> application.getString(R.string.title_of_the_blog_is_required)
                2012 -> application.getString(R.string.title_of_the_blog_is_too_short)
                2013 -> application.getString(R.string.title_of_the_blog_is_too_long)

                2021 -> application.getString(R.string.body_of_the_blog_is_required)
                2022 -> application.getString(R.string.body_of_the_blog_is_too_short)

                2031 -> application.getString(R.string.image_is_required)

                5001 -> application.getString(R.string.invalid_token)

                3001 -> application.getString(R.string.successfully_registered_new_user)
                3002 -> application.getString(R.string.username_or_password_is_incorrect)
                3003 -> application.getString(R.string.successfully_authenticated)
                3004 -> application.getString(R.string.account_updated_successfully)
                3005 -> application.getString(R.string.current_password_is_incorrect)
                3006 -> application.getString(R.string.password_changed_successfully)
                3007 -> application.getString(R.string.record_not_found)
                3008 -> application.getString(R.string.account_information_retrieved_successfully)

                4001 -> application.getString(R.string.can_not_upload_this_image)
                4002 -> application.getString(R.string.blog_not_found)
                4003 -> application.getString(R.string.blog_deleted_successfully)
                4004 -> application.getString(R.string.you_do_not_have_permission_to_delete_this_blog)
                4005 -> application.getString(R.string.you_do_not_have_permission_to_update_this_blog)
                4006 -> application.getString(R.string.invalid_page)
                4007 -> application.getString(R.string.blog_list_retrieved_successfully)
                4008 -> application.getString(R.string.blog_retrieved_successfully)
                4009 -> application.getString(R.string.is_author_of_the_blog_retrieved_successfully)
                4010 -> application.getString(R.string.blog_updated_successfully)
                4011 -> application.getString(R.string.blog_created_successfully)

                9001 -> application.getString(R.string.unknown_error)
                9002 -> application.getString(R.string.unable_to_resolve_host)
                9003 -> application.getString(R.string.unable_to_do_operation_wo_internet)
                9004 -> application.getString(R.string.error_check_network_connection)
                9005 -> application.getString(R.string.generic_error)
                9006 -> application.getString(R.string.error_save_auth_token)
                9007 -> application.getString(R.string.error_save_account_properties)
                9008 -> application.getString(R.string.response_check_previous_auth_user_done)
                9009 -> application.getString(R.string.generic_success)

                else -> application.getString(R.string.unknown_error)
            }
        }
    }
}