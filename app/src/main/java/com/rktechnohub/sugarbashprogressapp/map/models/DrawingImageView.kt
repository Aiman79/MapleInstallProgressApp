package com.rktechnohub.sugarbashprogressapp.map.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.map.model.MapDrawables
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Aiman on 01, June, 2024
 */
class DrawingImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
): AppCompatImageView(context, attrs, defStyle) {
//    private val paint = Paint()
    private val items = mutableListOf<String>()
    private val homeIcons = mutableListOf<Triple<Float, Float, String>>()
    private val paths = mutableListOf<Path>()
    private val paints = mutableListOf<Paint>()
    private var currentPath = Path()
    private var currentPaint = Paint()
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var endX: Float = 0f
    private var endY: Float = 0f
    private var isFirstClick = true
    private var maxHeight: Float = 100f // set a default height
    private var style: String = AppUtils.styleRed
    var mapDrawable: MapDrawables? = null


    init {
        currentPaint.color = Color.RED
        currentPaint.strokeWidth = 5f
        currentPaint.style = Paint.Style.STROKE
        currentPaint.alpha = 128
    }

    fun setStyle(style: String) {
        this.style = style
        currentPaint = when (style) {
            AppUtils.styleYellowDotted -> {
                val paint = Paint()
                paint.color = Color.YELLOW
                paint.pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f) // Adjust these values as needed
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }
            AppUtils.styleOrange -> {
                val paint = Paint()
                paint.color = context.getColor(R.color.orange)
                paint.pathEffect = PathEffect()
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }
            AppUtils.styleBlue -> {
                val paint = Paint()
                paint.color = context.getColor(R.color.blue)
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }
            AppUtils.styleStar -> {
                mapDrawable = MapDrawables(context, style)
                val paint = Paint()
                paint.color = Color.YELLOW
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }
            AppUtils.styleCircle -> {
                mapDrawable = MapDrawables(context, style)
                val paint = Paint()
                paint.color = Color.GREEN
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }
            AppUtils.styleRectangle -> {
                mapDrawable = MapDrawables(context, style)
                val paint = Paint()
                paint.color = Color.BLUE
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }
            else -> {
                val paint = Paint()
                paint.color = Color.RED
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }
        }
//        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (style == AppUtils.styleStar || style == AppUtils.styleCircle || style == AppUtils.styleRectangle){
                    addHomeIcon(event.x, event.y)
                } else {
                    if (isFirstClick) {
                        startX = event.x
                        startY = event.y
                        isFirstClick = false
                    } else {
                        endX = event.x
                        endY = event.y
                        drawLine()
                        isFirstClick = true
                    }
                }
            }
        }
        return true
    }

    private fun drawLine() {
        /*currentPaint = when (style) {
            AppUtils.styleYellowDotted -> {
                val paint = Paint()
                paint.color = Color.YELLOW
                paint.pathEffect =
                    DashPathEffect(floatArrayOf(20f, 10f), 0f) // Adjust these values as needed
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }

            AppUtils.styleOrange -> {
                val paint = Paint()
                paint.color = Color.BLACK
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }

            AppUtils.styleBlue -> {
                val paint = Paint()
                paint.color = context.getColor(R.color.blue)
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }

            else -> {
                val paint = Paint()
                paint.color = Color.RED
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
                paint.alpha = 128
                paint
            }
        }*/
        currentPath.reset()
        currentPath.moveTo(startX, startY)
        currentPath.lineTo(endX, endY)
        paths.add(currentPath)
        items.add(AppUtils.shapeLine)
        paints.add(currentPaint)
        currentPath = Path()
        invalidate()
        maxHeight = Math.max(maxHeight, endY)
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in paths.indices) {
            canvas.drawPath(paths[i], paints[i])
        }
//        if (style == AppUtils.styleStar || style == AppUtils.styleCircle || style == AppUtils.styleRectangle) {
            for (icon in homeIcons) {
                addHomeIcon(canvas, icon.first, icon.second, icon.third)
            }
//        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        var desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        // Ensure that the view has a minimum size even if there are no paths drawn yet
        desiredWidth = max(desiredWidth, widthSize)
        desiredHeight = max(desiredHeight, heightSize)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    private fun addHomeIcon(x: Float, y: Float) {
        homeIcons.add(Triple(x, y, style)) // Add the home icon position to the list
        items.add(AppUtils.shapeShapes)
        invalidate() // Trigger redraw
    }


    private fun addHomeIcon(canvas: Canvas, x: Float, y: Float, iconstyle: String) {
        // Draw a home icon at the clicked coordinates
//        AppUtils.getHomeIcon(context)
        var drawable: Drawable? = when (iconstyle) {
            AppUtils.styleStar -> mapDrawable?.starIcon
            AppUtils.styleCircle -> mapDrawable?.circleIcon
            AppUtils.styleRectangle -> mapDrawable?.rectIcon
            else -> null
        }

        // Calculate the bounds for the icon
        val iconWidth = drawable?.intrinsicWidth ?: 0
        val iconHeight = drawable?.intrinsicHeight ?: 0
        val left = x - iconWidth / 2
        val top = y - iconHeight / 2
        val right = left + iconWidth
        val bottom = top + iconHeight

        // Set the bounds for the drawable
        drawable?.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
//        mapDrawable?.homeIcon = drawable

        drawable?.draw(canvas)
        // Invalidate the view to trigger redraw
//        invalidate()

      /*  val homeIconWidth = 50 // Adjust this value as needed
        val homeIconHeight = 50 // Adjust this value as needed

        // Example of drawing a simple home icon (you can replace this with your own home icon)
        val homeIconPath = Path()
        val halfWidth = homeIconWidth / 2
        val halfHeight = homeIconHeight / 2
        homeIconPath.moveTo(x - halfWidth, y - halfHeight)
        homeIconPath.lineTo(x + halfWidth, y - halfHeight)
        homeIconPath.lineTo(x, y + halfHeight)
        homeIconPath.close()

        // Draw the home icon on the canvas
        val homePaint = Paint().apply {
            color = Color.BLUE // Change the color as needed
            style = Paint.Style.FILL
        }
        canvas.drawPath(homeIconPath, homePaint)

        // Invalidate the view to redraw with the home icon
        invalidate()*/
    }

   /* private fun addHomeIcon(canvas: Canvas, x: Float, y: Float) {
        val drawable = context.getDrawable(R.drawable.ic_vector_home)

        // Calculate the bounds for the icon
        val iconWidth = drawable?.intrinsicWidth ?: 0
        val iconHeight = drawable?.intrinsicHeight ?: 0
        val left = x - iconWidth / 2
        val top = y - iconHeight / 2
        val right = left + iconWidth
        val bottom = top + iconHeight

        // Set the bounds for the drawable
        drawable?.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

        // Draw the home icon onto the canvas
        drawable?.draw(canvas)
    }*/

    fun undo() {
       /* if (paths.isNotEmpty()) {
            paths.removeLast()
        }
        if (paints.isNotEmpty()) {
            paints.removeLast()
        }
        if (homeIcons.isNotEmpty()) {
            homeIcons.removeLast()
        }
        invalidate()*/
        when (items[items.size - 1]){
            AppUtils.shapeLine -> {
                if (paths.isNotEmpty()) {
                    paths.removeLast()
                }
                if (paints.isNotEmpty()) {
                    paints.removeLast()
                }
                invalidate()

            }

            else -> {
                if (homeIcons.isNotEmpty()) {
                    homeIcons.removeLast()
                }
                invalidate()
            }
        }
        items.removeLast()


    }

    fun saveAsBitmap(): Bitmap {
        val drawable = this.drawable
        val originalWidth = drawable.intrinsicWidth
        val originalHeight = drawable.intrinsicHeight

        // Create a new bitmap with the same dimensions as the original image
        val bitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.RGB_565)

        // Create a canvas with the new bitmap
        val canvas = Canvas(bitmap)

        // Draw the original image onto the canvas
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap.copy(Bitmap.Config.RGB_565, true)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        } else if (drawable is NinePatchDrawable) {
            val ninePatchDrawable = drawable as NinePatchDrawable
            val bitmap = Bitmap.createBitmap(ninePatchDrawable.intrinsicWidth, ninePatchDrawable.intrinsicHeight, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            ninePatchDrawable.setBounds(0, 0, canvas.width, canvas.height)
            ninePatchDrawable.draw(canvas)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        } else {
            // Handle other types of drawables here if needed
        }

        // Scale the canvas to match the original image's dimensions
        canvas.save()
        canvas.scale((originalWidth.toFloat() / measuredWidth), (originalHeight.toFloat() / measuredHeight))

        // Draw the shapes and lines onto the canvas
        draw(canvas)

        // Restore the canvas to its original state
        canvas.restore()

        return bitmap
    }

    fun saveAsBitmapN(): Bitmap {
        val drawable = this.drawable
        val originalWidth = drawable.intrinsicWidth
        val originalHeight = drawable.intrinsicHeight

        // Create a new bitmap with the same dimensions as the original image
        val bitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.RGB_565)

        // Create a canvas with the new bitmap
        val canvas = Canvas(bitmap)

        // Draw the original image onto the canvas
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap.copy(Bitmap.Config.RGB_565, true)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        } else if (drawable is NinePatchDrawable) {
            val ninePatchDrawable = drawable as NinePatchDrawable
            val bitmap = Bitmap.createBitmap(ninePatchDrawable.intrinsicWidth, ninePatchDrawable.intrinsicHeight, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            ninePatchDrawable.setBounds(0, 0, canvas.width, canvas.height)
            ninePatchDrawable.draw(canvas)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        } else {
            // Handle other types of drawables here if needed
        }

        // Scale the canvas to match the original image's dimensions
        canvas.save()
        canvas.scale((originalWidth.toFloat() / measuredWidth), (originalHeight.toFloat() / measuredHeight))

        // Draw the shapes and lines onto the canvas
        draw(canvas)

        // Restore the canvas to its original state
        canvas.restore()

        return bitmap
    }

    /*fun saveAsBitmap(): Bitmap {
        // Force the view to layout itself
        layout(0, 0, width, height)

        // Get the measured dimensions
        val measuredWidth = measuredWidth
        val measuredHeight = measuredHeight

        // Create a bitmap with the measured dimensions
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)

        // Create a canvas with the bitmap
        val canvas = Canvas(bitmap)

        // Draw the view onto the canvas
        draw(canvas)

        return bitmap
    }*/

   /* fun saveAsBitmap(): Bitmap {
        // Get the dimensions of the ImageView
        val measuredWidth = measuredWidth
        val measuredHeight = measuredHeight

        // Create a new bitmap with the same dimensions as the ImageView
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)

        // Create a canvas with the new bitmap
        val canvas = Canvas(bitmap)

        // Draw the original image onto the canvas
        val drawable = this.drawable
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        } else if (drawable is NinePatchDrawable) {
            val ninePatchDrawable = drawable as NinePatchDrawable
            val bitmap = Bitmap.createBitmap(ninePatchDrawable.intrinsicWidth, ninePatchDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            ninePatchDrawable.setBounds(0, 0, canvas.width, canvas.height)
            ninePatchDrawable.draw(canvas)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        } else {
            // Handle other types of drawables here if needed
        }

        // Draw the canvas onto the new bitmap
        draw(canvas)

        return bitmap
    }*/
}