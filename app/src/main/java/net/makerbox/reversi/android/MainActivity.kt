package net.makerbox.reversi.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.makerbox.reversi.android.dataType.Disk
import net.makerbox.reversi.android.views.BoardView
import net.makerbox.reversi.android.views.DiskView
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var boardView: BoardView
    private lateinit var messageDiskView: DiskView
    private lateinit var messageLabel: TextView
    private lateinit var playerControls: Array<UISegmentedControl>
    private lateinit var countLabels: Array<TextView>
    private lateinit var playerActivityIndicators: Array<UIActivityIndicatorView>
    private lateinit var resetButton: Button

    private var messageDiskSizeConstraint: Constraint
    private var messageDiskSize: Float = 0.0
    private var turn: Disk = Disk.dark
    private var animationCanceller: Canceller? = null
    private var playerCancellers: HashMap<Disk, Canceller> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        boardView = findViewById(R.id.boardView)
        messageDiskView = findViewById(R.id.messageDiskView)
        messageLabel = findViewById(R.id.messageLabel)
        playerControls = arrayOf(findViewById(R.id.playerControl1), findViewById(R.id.playerControl2)) // レイアウトに合わせてIDを変更
        countLabels = arrayOf(findViewById(R.id.countLabel1), findViewById(R.id.countLabel2)) // 同様にレイアウトに合わせてIDを変更
        playerActivityIndicators = arrayOf(findViewById(R.id.playerIndicator1), findViewById(R.id.playerIndicator2)) // 同様にレイアウトに合わせてIDを変更
        resetButton = findViewById(R.id.resetButton)
//        messageDiskSizeConstraint = findViewById(R.id.messageDiskView).layoutParams as Constraint


        // 新しいゲームの開始
        newGame()
    }

    var viewHasAppeared = false
    override fun onResume() {
        super.onResume()

        if (viewHasAppeared) return
        viewHasAppeared = true
        waitForPlayer()
    }

    // Reversi logics, Game management, Views, Inputs, Save and Load, Additional types のメソッドはここに変換される
    // 機能やロジックに合わせてKotlinに変換される
}