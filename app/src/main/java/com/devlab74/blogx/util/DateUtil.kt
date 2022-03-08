package com.devlab74.blogx.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * This class is responsible for converting Server type Date String to Long
 * And converting Long to String Date Format
 */

class DateUtils {
    companion object{
        fun convertServerStringDateToLong(sd: String): Long{
            val stringDate = sd.removeRange(sd.indexOf("T") until sd.length)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            try {
                return sdf.parse(stringDate).time
            } catch (e: Exception) {
                throw Exception(e)
            }
        }

        fun convertLongToStringDate(longDate: Long): String{
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            try {
                return sdf.format(Date(longDate))
            } catch (e: Exception) {
                throw Exception(e)
            }
        }
    }
}