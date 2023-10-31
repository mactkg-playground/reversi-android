package net.makerbox.reversi.android.views
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import net.makerbox.reversi.android.dataType.Disk

private const val lineWidth: Float = 2f

class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var cellViews: MutableList<CellView> = mutableListOf()
    private var actions: MutableList<CellSelectionAction> = mutableListOf()

    val boardWidth: Int = 8
    val boardHeight: Int = 8
    val xRange: IntRange = 0 until boardWidth
    val yRange: IntRange = 0 until boardHeight

    var listener: BoardViewSelectListener? = null

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#000000")) // Set your desired color

        val params = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
        for (y in yRange) {
            val rowLayout = LinearLayout(context)
            rowLayout.orientation = HORIZONTAL
            rowLayout.layoutParams = params

            for (x in xRange) {
                val cellView = CellView(context)
                cellView.layoutParams = params
                rowLayout.addView(cellView)
                cellViews.add(cellView)
            }

            addView(rowLayout)
        }

        reset()

        for (y in yRange) {
            for (x in xRange) {
                val cellView = cellViewAt(x, y)
                val action = CellSelectionAction(this, x, y)
                actions.add(action)
//                cellView?.setOnClickListener { action.selectCell() }
            }
        }
    }

    fun reset() {
        cellViews.forEach { it.setDisk(null, false) }

        setDisk(Disk.LIGHT, boardWidth / 2 - 1, boardHeight / 2 - 1, false)
        setDisk(Disk.DARK, boardWidth / 2, boardHeight / 2 - 1, false)
        setDisk(Disk.DARK, boardWidth / 2 - 1, boardHeight / 2, false)
        setDisk(Disk.LIGHT, boardWidth / 2, boardHeight / 2, false)
    }

    private fun cellViewAt(x: Int, y: Int): CellView? {
        if (x in xRange && y in yRange) {
            val index = y * boardWidth + x
            return cellViews.getOrNull(index)
        }
        return null
    }

    fun diskAt(x: Int, y: Int): Disk? {
        return cellViewAt(x, y)?.disk
    }

    fun setDisk(disk: Disk?, x: Int, y: Int, animated: Boolean, completion: ((Boolean) -> Unit)? = null) {
        val cellView = cellViewAt(x, y) ?: return
        cellView.setDisk(disk, animated, completion)
    }
}

interface BoardViewSelectListener {
    fun didSelectCellAt(boardView: BoardView, x: Int, y: Int)
}

private class CellSelectionAction(
    private val boardView: BoardView,
    private val x: Int,
    private val y: Int
) : View.OnClickListener {

    override fun onClick(view: View?) {
        boardView.listener?.didSelectCellAt(boardView, x, y)
    }
}