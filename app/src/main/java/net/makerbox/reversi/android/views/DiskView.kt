package net.makerbox.reversi.android.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import net.makerbox.reversi.android.R
import net.makerbox.reversi.android.dataType.Disk

class DiskView : View {
    var disk: Disk = Disk.DARK
        private set

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        // Set up initial properties of the view
        isClickable = false
    }

    fun setDisk(color: Disk) {
        disk = color
        invalidate() // Redraw the view with the new color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint()
        paint.color = disk.color(context)
        paint.style = Paint.Style.FILL

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width.coerceAtMost(height) * 0.4).coerceAtLeast(1.0) // Adjust disk size

        canvas.drawCircle(centerX, centerY, radius.toFloat(), paint)
    }
}

fun Disk.color(context: Context): Int {
    return when (this) {
        Disk.DARK -> ContextCompat.getColor(context, R.color.dark)
        Disk.LIGHT -> ContextCompat.getColor(context, R.color.light)
    }
}