package com.devlab74.blogx.util

class Constants {
    companion object {
        const val BASE_URL = "https://simanushkin.tk/blogx/"
        const val API_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZWNyZXQiOiJJX0F0ZV9Zb3VyX0NhdCIsImlhdCI6MTYwODQyNDc2NywiZXhwIjoxNzY2MjEyNzY3fQ.Bl2dESHEnj5aQ51j402Ss0mJFV45obPUPFt-YjNehFQ"
        const val PASSWORD_RESET_URL = "https://simanushkin.tk/password-reset"

        const val PAGINATION_PAGE_SIZE = 10

        const val NETWORK_TIMEOUT = 3000L
        const val CACHE_TIMEOUT = 2000L
        const val TESTING_NETWORK_DELAY = 0L // fake network delay for testing
        const val TESTING_CACHE_DELAY = 0L // fake cache delay for testing

        const val GALLERY_REQUEST_CODE = 201
        const val PERMISSIONS_REQUEST_READ_STORAGE = 301
        const val CROP_IMAGE_INTENT_CODE = 401
    }
}