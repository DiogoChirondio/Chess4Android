package pt.isel.pdm.chess4android.history

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import pt.isel.pdm.chess4android.PuzzleGameDto
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.daily.MainActivity
import pt.isel.pdm.chess4android.game.views.BoardView

private const val PUZZLE_EXTRA = "PuzzleActivityLegacy.Extra.Puzzle"

class PuzzleActivity : AppCompatActivity() {

    companion object {
        fun buildIntentPuzzle(origin: Activity, puzzleDto: PuzzleGameDto): Intent {
            val msg = Intent(origin, PuzzleActivity::class.java)
            msg.putExtra(PUZZLE_EXTRA, puzzleDto)
            return msg
        }
    }

    private val viewModel: PuzzleActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        val puzzleDto = intent.getParcelableExtra<PuzzleGameDto>(PUZZLE_EXTRA)

        findViewById<TextView>(R.id.daily_puzzle).text = puzzleDto?.puzzleGame?.game?.id
        val attemptButton = findViewById<Button>(R.id.attempt_button)
        val boardView = findViewById<BoardView>(R.id.boardView)

        viewModel.boardView.observe(this) { board ->
            board.forEach{ piece ->
                boardView.setPiece(piece.key, piece.value)
            }
        }

        viewModel.showIntendedPuzzle(puzzleDto?.puzzleGame?.game?.pgn)

        attemptButton.setOnClickListener {
            startActivity(MainActivity.buildIntentMain(this, puzzleDto!!))
        }
    }
}