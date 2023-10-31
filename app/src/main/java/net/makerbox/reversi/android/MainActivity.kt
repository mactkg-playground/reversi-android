package net.makerbox.reversi.android

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import net.makerbox.reversi.android.dataType.Disk
import net.makerbox.reversi.android.views.BoardView
import net.makerbox.reversi.android.views.DiskView
import android.widget.Button
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import net.makerbox.reversi.android.views.BoardViewSelectListener

class MainActivity : AppCompatActivity(), BoardViewSelectListener {
    private lateinit var mainHandler : Handler

    private lateinit var boardView: BoardView
    private lateinit var messageDiskView: DiskView
    private lateinit var messageLabel: TextView
    private lateinit var playerControls: Array<ChipGroup>
    private lateinit var countLabels: Array<TextView>
//    private lateinit var playerActivityIndicators: Array<UIActivityIndicatorView>
    private lateinit var resetButton: Button

//    private var messageDiskSizeConstraint: Constraint
    private var messageDiskSize: Float = 0.0f
    private var turn: Disk? = Disk.DARK
    private var animationCanceller: Canceller? = null
    private var playerCancellers: HashMap<Disk, Canceller?> = HashMap()
    fun isAnimating(): Boolean {
        return animationCanceller != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        boardView = findViewById(R.id.boardView)
        messageDiskView = findViewById(R.id.messageDiskView)
        messageLabel = findViewById(R.id.messageLabel)
        playerControls = arrayOf(findViewById(R.id.playerControl1), findViewById(R.id.playerControl2)) // レイアウトに合わせてIDを変更
        countLabels = arrayOf(findViewById(R.id.countLabel1), findViewById(R.id.countLabel2)) // 同様にレイアウトに合わせてIDを変更
//        playerActivityIndicators = arrayOf(findViewById(R.id.playerIndicator1), findViewById(R.id.playerIndicator2)) // 同様にレイアウトに合わせてIDを変更
        resetButton = findViewById(R.id.resetButton)
//        messageDiskSizeConstraint = findViewById(R.id.messageDiskView).layoutParams as Constraint

        mainHandler = Handler(mainLooper)

        // 新しいゲームの開始
//        newGame()
    }

    var viewHasAppeared = false
    override fun onResume() {
        super.onResume()

        if (viewHasAppeared) return
        viewHasAppeared = true
//        waitForPlayer()
    }

    // Reversi logics, Game management, Views, Inputs, Save and Load, Additional types のメソッドはここに変換される
    // 機能やロジックに合わせてKotlinに変換される
    // MARK: Reversi logics
    fun countDisks(side: Disk): Int {
        var count = 0

        for (y in boardView.yRange) {
            for (x in boardView.xRange) {
                if (boardView.diskAt(x, y) == side) {
                    count += 1
                }
            }
        }
        return count
    }

    fun sideWithMoreDisks(): Disk? {
        val darkCount = countDisks(Disk.DARK)
        val lightCount = countDisks(Disk.LIGHT)
        return if (darkCount == lightCount) {
            null
        } else {
            if (darkCount > lightCount) Disk.DARK else Disk.LIGHT
        }
    }

    fun flippedDiskCoordinatesByPlacingDisk(disk: Disk, x: Int, y: Int): List<Pair<Int, Int>> {
        val directions = arrayOf(
            Pair(-1, -1),
            Pair(0, -1),
            Pair(1, -1),
            Pair(1, 0),
            Pair(1, 1),
            Pair(0, 1),
            Pair(-1, 0),
            Pair(-1, 1)
        )
        val boardViewDisk = boardView.diskAt(x, y)
        if (boardViewDisk != null) return emptyList()

        val diskCoordinates = mutableListOf<Pair<Int, Int>>()

        directions.forEach { direction ->
            var x = x
            var y = y
            val diskCoordinatesInLine = mutableListOf<Pair<Int, Int>>()
            while (true) {
                x += direction.first
                y += direction.second
                val disk = Pair(disk, boardView.diskAt(x, y))
                when (disk) {
                    Pair(Disk.DARK, Disk.DARK), Pair(Disk.LIGHT, Disk.LIGHT) -> {
                        diskCoordinates.addAll(diskCoordinatesInLine)
                        break
                    }
                    Pair(Disk.DARK, Disk.LIGHT), Pair(Disk.LIGHT, Disk.DARK) -> {
                        diskCoordinatesInLine.add(Pair(x, y))
                    }
                    else -> break
                }
            }
        }
        return diskCoordinates
    }

    fun canPlaceDisk(disk: Disk, x: Int, y: Int): Boolean {
        return flippedDiskCoordinatesByPlacingDisk(disk, x, y).isNotEmpty()
    }

    fun validMoves(side: Disk): List<Pair<Int, Int>> {
        val coordinates = mutableListOf<Pair<Int, Int>>()

        for (y in boardView.yRange) {
            for (x in boardView.xRange) {
                if (canPlaceDisk(side, x, y)) {
                    coordinates.add(Pair(x, y))
                }
            }
        }
        return coordinates
    }

    fun placeDisk(disk: Disk, x: Int, y: Int, isAnimated: Boolean, completion: ((Boolean) -> Unit)?) {
        val diskCoordinates = flippedDiskCoordinatesByPlacingDisk(disk, x, y)
        if (diskCoordinates.isEmpty()) {
            throw DiskPlacementError(disk, x, y)
        }

        if (isAnimated) {
            val cleanUp = {
                animationCanceller = null
            }
            animationCanceller = Canceller(cleanUp)
            animateSettingDisks(listOf(Pair(x, y)) + diskCoordinates, disk) { isFinished ->
                if (animationCanceller?.isCancelled == false) {
                    cleanUp()
                    completion?.invoke(isFinished)
                    try {
//                        saveGame()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    updateCountLabels()
                }
            }
        } else {
            mainHandler.post {
                boardView.setDisk(disk, x, y, false)
                diskCoordinates.forEach { coordinate ->
                    boardView.setDisk(disk, coordinate.first, coordinate.second, false)
                }
                completion?.invoke(true)
                try {
//                    saveGame()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                updateCountLabels()
            }
        }
    }

    // Assume Disk and other required classes are properly defined
// Assume `animationCanceller` is defined as a field in the Activity

    private fun animateSettingDisks(coordinates: List<Pair<Int, Int>>, disk: Disk, completion: (Boolean) -> Unit) {
        if (coordinates.isEmpty()) {
            completion(true)
            return
        }

        val (x, y) = coordinates.first()
        val animationCanceller = animationCanceller ?: return  // Provide a reference to the animation canceller

        boardView.setDisk(disk, x, y, true) { isFinished ->
            if (animationCanceller.isCancelled) return@setDisk
            if (isFinished) {
                animateSettingDisks(coordinates.drop(1), disk, completion)
            } else {
                coordinates.forEach { (newX, newY) ->
                    boardView.setDisk(disk, newX, newY, false)
                }
                completion(false)
            }
        }
    }

    // MARK : Game management
    fun newGame() {
        boardView.reset()
        turn = Disk.DARK

//        playerControls.forEach { it = Player.MANUAL.ordinal }

        updateMessageViews()
        updateCountLabels()

//        saveGame() // Handle the saveGame function
    }

    fun waitForPlayer() {
        val turn = turn ?: return
//        when (Player.values()[playerControls[turn.index].selectedIndex]) {
//            Player.MANUAL -> Unit // Handle manual player
//            Player.COMPUTER -> playTurnOfComputer()
//        }
    }

    fun nextTurn() {
        var turn = turn ?: return

        turn = turn.flipped()

        if (validMoves(turn).isEmpty()) {
            if (validMoves(turn.flipped()).isEmpty()) {
                this.turn = null
                updateMessageViews()
            } else {
                this.turn = turn
                updateMessageViews()

                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Pass")
                    .setMessage("Cannot place a disk.")
                    .setPositiveButton("Dismiss") { _, _ -> nextTurn() }
                    .create()

                alertDialog.show()
            }
        } else {
            this.turn = turn
            updateMessageViews()
            waitForPlayer()
        }
    }

    fun playTurnOfComputer() {
        val turn = turn ?: return
        val validMoves = validMoves(turn)
        val (x, y) = validMoves.random()

//        playerActivityIndicators[turn.index].startAnimating()

        val cleanUp: () -> Unit = {
//            playerActivityIndicators[turn.index].stopAnimating()
            playerCancellers[turn] = null
        }

        val canceller = Canceller(cleanUp)
        playerCancellers[turn] = canceller

        mainHandler.postDelayed({
            if (canceller.isCancelled) return@postDelayed
            cleanUp()

            placeDisk(turn, x, y, true) {
                nextTurn()
            }
        }, 2000)
    }

    // MARK: Views
    // Equivalent functions of Swift's ViewController extension
    private fun updateCountLabels() {
        Disk.sides().forEachIndexed { index, side ->
            countLabels[index].text = countDisks(side).toString()
        }
    }

    private fun updateMessageViews() {
        when (turn) {
            is Disk -> {
                val side = turn!!
//                messageDiskSizeConstraint = messageDiskSize
                messageDiskView.setDisk(side) // Update the disk on the view
                messageLabel.text = "${side.name}'s turn" // Adjust the side's name access
            }
            else -> {
                val winner = sideWithMoreDisks()
                if (winner != null) {
//                    messageDiskSizeConstraint = messageDiskSize
                    messageDiskView.setDisk(winner) // Update the disk on the view
                    messageLabel.text = "${winner.name} won" // Adjust the winner's name access
                } else {
//                    messageDiskSizeConstraint = 0
                    messageLabel.text = "Tied"
                }
            }
        }
    }

    // MARK: Inputs
    private fun pressResetButton() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Do you really want to reset the game?")
            .setCancelable(true)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("OK") { _, _ ->
                animationCanceller?.cancel()
                animationCanceller = null

                Disk.sides().forEach { side ->
                    playerCancellers[side]?.cancel()
                    playerCancellers.remove(side)
                }

                newGame()
                waitForPlayer()
            }
            .create()

        alertDialog.show()
    }

    private fun changePlayerControlSegment(checkedId: Int) {
//        val side = Disk(index = playerControls.indexOf(findViewById(checkedId)))

        // Handle saveGame function
        // TODO
        // try? saveGame()

//        playerCancellers[side]?.cancel()

//        if (!isAnimating && side == turn && Player.values()[playerControls.checkedRadioButtonId] == Player.COMPUTER) {
//            playTurnOfComputer()
//        }
    }

    override fun didSelectCellAt(boardView: BoardView, x: Int, y: Int) {
       val turn = turn ?: return
       if (isAnimating()) return
       val selectedChip = findViewById<Chip>(playerControls[turn.index].checkedChipId)
        if (selectedChip.text.equals("Manual")) {
            placeDisk(turn, x, y, true) {
                nextTurn()
            }
        }
    }
}

enum class Player {
    MANUAL,
    COMPUTER
}

class Canceller {
    var isCancelled: Boolean = false
        private set

    private var cleanUp: (() -> Unit)?

    constructor(cleanUp: () -> Unit) {
        this.cleanUp = cleanUp
    }

    fun cancel() {
        if (isCancelled) return
        isCancelled = true
        cleanUp?.invoke()
        cleanUp = null
    }
}

class DiskPlacementError(disk: Disk, x: Int, y: Int) : Error("Cannot place $disk at ($x, $y).")

