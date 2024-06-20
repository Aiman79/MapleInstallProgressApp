package com.rktechnohub.sugarbashprogressapp.map.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.google.android.material.canvas.CanvasCompat
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils

/**
 * Created by Aiman on 06, June, 2024
 */
class MapDrawables(context: Context, style: String) {
    var starIcon: Drawable?
    var circleIcon: Drawable?
    var rectIcon: Drawable?

    init {

        starIcon = ContextCompat.getDrawable(context, R.drawable.ic_vector_star)

        circleIcon = ContextCompat.getDrawable(context, R.drawable.circle_red)

        rectIcon = ContextCompat.getDrawable(context, R.drawable.rect_blue_light)

    }


}