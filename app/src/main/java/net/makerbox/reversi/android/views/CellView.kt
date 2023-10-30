package net.makerbox.reversi.android.views

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import net.makerbox.reversi.android.R
import net.makerbox.reversi.android.dataType.Disk

private const val animationDuration: Float = 0.25f

class CellView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val button: View
    private val diskView: DiskView

    private var _disk: Disk? = null
    var disk: Disk?
        get() = _disk
        set(value) {
            setDisk(value, true)
        }

    init {
        button = View(context)
        button.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        button.setBackgroundColor(context.getColor(R.color.cell))

        diskView = DiskView(context)
        diskView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(button)
        addView(diskView)

        requestLayout()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        button.layout(left, top, right, bottom)
        layoutDiskView()
    }

    private fun layoutDiskView() {
        val cellSize = width.coerceAtMost(height).toFloat()
        val diskDiameter = cellSize * 0.8f
        val diskSize: Float = if (_disk == null || diskView.disk == _disk) {
            diskDiameter
        } else {
            0f
        }

        val left = ((cellSize - diskSize) / 2).toInt()
        val top = ((cellSize - diskSize) / 2).toInt()
        val right = (left + diskSize).toInt()
        val bottom = (top + diskSize).toInt()

        diskView.layout(left, top, right, bottom)
        diskView.alpha = if (_disk == null) 0.0f else 1.0f
    }

    fun setDisk(disk: Disk?, animated: Boolean, completion: ((Boolean) -> Unit)? = null) {
        val diskBefore = _disk
        _disk = disk
        val diskAfter = _disk

        if (animated) {
            when (diskBefore to diskAfter) {
                null to null -> completion?.invoke(true)
                null to diskAfter -> {
                    diskView.setDisk(diskAfter!!)
                    ValueAnimator.ofFloat(0f, animationDuration).apply {
                        addUpdateListener {
                            layoutDiskView()
                        }
                        doOnEnd {
                            completion?.invoke(true)
                        }
                        start()
                    }
                }
                diskBefore to null -> {
                    ValueAnimator.ofFloat(0f, animationDuration).apply {
                        addUpdateListener {
                            layoutDiskView()
                        }
                        doOnEnd {
                            completion?.invoke(true)
                        }
                        start()
                    }
                }
                diskBefore to diskAfter -> {
                    ValueAnimator.ofFloat(0f, animationDuration / 2).apply {
                        addUpdateListener {
                            layoutDiskView()
                        }
                        doOnEnd {
                            if (diskView.disk == _disk) {
                                completion?.invoke(true)
                            }

                            _disk?.let {
                                diskView.setDisk(it)
                                ValueAnimator.ofFloat(0f, animationDuration / 2).apply {
                                    addUpdateListener {
                                        layoutDiskView()
                                    }
                                    doOnEnd {
                                        completion?.invoke(true)
                                    }
                                    start()
                                }
                            } ?: run {
                                completion?.invoke(true)
                            }
                        }
                        start()
                    }
                }
            }
        } else {
            diskAfter?.let { diskView.setDisk(it) }
            completion?.invoke(true)
            requestLayout()
        }
    }

    fun addTarget(listener: OnClickListener) {
        button.setOnClickListener(listener)
    }
}