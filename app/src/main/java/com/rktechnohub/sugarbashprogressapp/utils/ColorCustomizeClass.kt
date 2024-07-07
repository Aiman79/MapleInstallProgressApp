package com.rktechnohub.sugarbashprogressapp.utils

import android.content.Context
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.rktechnohub.sugarbashprogressapp.R

/**
 * Created by Aiman on 02, July, 2024
 */
object ColorCustomizeClass {
    val colorOnePercentage = 0
    val colorTwoPercentage = 50
    val colorThreePercentage = 100

    var colorOne = R.color.colorAccent
    var colorTwo = R.color.colorSecondary
    var colorThree = R.color.colorPrimary

    var isCustomColor = false

    fun changeColorOpOne(){
        isCustomColor = false
        colorOne = R.color.colorAccent
        colorTwo = R.color.colorSecondary
        colorThree = R.color.colorPrimary
    }

    fun changeColorOpTwo(){
        isCustomColor = false
        colorOne = R.color.colorPrimary
        colorTwo = R.color.colorSecondary
        colorThree = R.color.colorAccent
    }
    fun changeColorOpThree(){
        isCustomColor = false
        colorOne = R.color.color_green_extra_light
        colorTwo = R.color.color_green_light
        colorThree = R.color.colorPrimary
    }

    fun setCustomColors(color1: Int, color2: Int, color3: Int){
        isCustomColor = true

        if (color1 != 0){
            colorOne = color1
        }
        if (color2 != 0){
            colorTwo = color2
        }
        if (color3 != 0){
            colorThree = color3
        }
    }

    fun ProgressBar.updateProgress(progress: Int, context: Context) {
        val progressBarColor: Int = when (progress) {
            in 0..49 -> {
                if (isCustomColor){
                    colorOne
                } else {
                    ContextCompat.getColor(context, colorOne) // color1
                }
            }
            in 50..99 -> {
                if (isCustomColor){
                    colorTwo
                } else {
                    ContextCompat.getColor(context, colorTwo) // color1
                }

            } // color2
            100 -> {
                if (isCustomColor){
                    colorThree
                } else {
                    ContextCompat.getColor(context, colorThree) // color1
                }
            } // color3
            else -> {
                if (isCustomColor){
                    colorOne
                } else {
                    ContextCompat.getColor(context, colorOne) // color1
                }
            }
        // default color
        }
//        progressDrawable.colorFilter = PorterDuffColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN)
        val layerDrawable = progressDrawable as LayerDrawable
        val progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress)
        progressDrawable.clearColorFilter() // Clear any previous color filter
        progressDrawable.setColorFilter(PorterDuffColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN))


        setProgress(progress)
    }

    fun ProgressBar.setCustomProgressColor(progressBarColor: Int){
        val layerDrawable = progressDrawable as LayerDrawable
        val progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress)
        progressDrawable.clearColorFilter() // Clear any previous color filter
        progressDrawable.setColorFilter(PorterDuffColorFilter(progressBarColor, PorterDuff.Mode.SRC_IN))
    }
}