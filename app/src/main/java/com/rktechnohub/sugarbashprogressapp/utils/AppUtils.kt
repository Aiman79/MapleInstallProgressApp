package com.rktechnohub.sugarbashprogressapp.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import com.rktechnohub.sugarbashprogressapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Created by Aiman on 17, May, 2024
 */
object AppUtils {
    val styleRed = "red"
    val styleOrange = "orange"
    val styleYellowDotted = "yellowDotted"
    val styleBlue = "styleBlue"
    val styleStar = "styleStar"
    val styleCircle = "styleCircle"
    val styleRectangle = "styleRectangle"

    val shapeLine = "line"
    val shapeShapes = "shape"

//    var homeDrawable: Drawable? = null

    val roleSuperAdmin = 0
    val roleAdmin = 1
    val roleEmployee = 2
    val roleClient = 3

    val dateFormatMMMMdyyyy = "MMMM d, yyyy"
    val formatMMddyyyy = "dd/MM/yyyy"

    //database
    val userTable = "User"
//    val userTable = "User"

    fun getHomeIcon(context: Context){
        context.getDrawable(R.drawable.ic_vector_home)
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat(dateFormatMMMMdyyyy, Locale.ENGLISH)
        val date = Date()
        return dateFormat.format(date)
    }

    fun getFormattedDateObj(date: String, format: String): Date {
        val dateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        return dateFormat.parse(date)!!
    }

    fun getFormattedDate(date: Date): String {
        val dateFormat = SimpleDateFormat(dateFormatMMMMdyyyy, Locale.ENGLISH)
        return dateFormat.format(date)
    }

    fun getDifference(sDate: String, eDate: String): Int {

//        val format = "yyyy-MM-dd"
        val formatter = SimpleDateFormat(dateFormatMMMMdyyyy, Locale.getDefault())

        val startDate = formatter.parse(sDate)
        val endDate = formatter.parse(eDate)

        val diffInMilliseconds = endDate.time - startDate.time
        val diffInDays = TimeUnit.DAYS.convert(diffInMilliseconds, TimeUnit.MILLISECONDS)

        println("Difference between dates: $diffInDays days")
        return diffInDays.toInt()
    }

    fun isPastDate(dateS: String): Boolean{
        val dateFormat = SimpleDateFormat(dateFormatMMMMdyyyy, Locale.ENGLISH)
        val date = dateFormat.parse(dateS)
        val currentTime = System.currentTimeMillis()
        val dateToCheck = date.time

        return dateToCheck <= currentTime

    }

    fun checkIfEmoji(icon: String): Boolean{
        val uri = Uri.parse(icon)
        if (uri.scheme == "https" && uri.authority == "firebasestorage.googleapis.com") {
            println("It's a link to a Firebase Storage image!")
            return false
        } else {
            println("It's not a link to a Firebase Storage image!")
            return true
        }
    }
}