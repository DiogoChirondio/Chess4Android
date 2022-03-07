package pt.isel.pdm.chess4android.daily

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import pt.isel.pdm.chess4android.PuzzleGameDto
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.databinding.ActivityMainBinding
import pt.isel.pdm.chess4android.game.model.Army
import pt.isel.pdm.chess4android.game.model.Position
import pt.isel.pdm.chess4android.game.model.Role
import pt.isel.pdm.chess4android.game.model.pieces.Pawn
import pt.isel.pdm.chess4android.game.views.Tile

private const val PUZZLE_EXTRA = "MainActivityLegacy.Extra.Puzzle"

class MainActivity : BaseActivity() {

    companion object {
        fun buildIntentMain(origin: Activity, puzzleDto: PuzzleGameDto): Intent {
            val msg = Intent(origin, MainActivity::class.java)
            msg.putExtra(PUZZLE_EXTRA, puzzleDto)
            return msg
        }
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainActivityViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.v("APP_TAG", Thread.currentThread().id.toString())

        val puzzleDto = intent.getParcelableExtra<PuzzleGameDto>(PUZZLE_EXTRA)

        binding.startButton.setOnClickListener{
            binding.dailyPuzzle.text = getString(R.string.fetching_message)
            if(viewModel.winCondition.value == true){
                viewModel.clean = true
                viewModel.cleanBoard()
                viewModel.winCondition.value = false
            }
            viewModel.getDailyPuzzle(puzzleDto)
        }

        viewModel.boardView.observe(this) { board ->
            if(!viewModel.clean){
                Log.v("APP_TAG", "observer notified")
                Log.v("APP_TAG", "NUMBER OF PLAYS: "+viewModel.numberOfPlays)

                board.forEach{ piece ->
                    binding.boardView.setPiece(piece.key, piece.value)
                }
                if(viewModel.isPieceSelected()){
                    if(viewModel.isToRemove){
                        binding.boardView.removePiece(viewModel.pieceSelected?.first!!)
                        viewModel.isToRemove = false
                    }
                    if(viewModel.moved){
                        viewModel.pieceSelected = null
                        viewModel.numberOfPlays = viewModel.numberOfPlays?.dec()
                        viewModel.moved = false
                    }
                }
                if(viewModel.possibleEnPassantRemoval){
                    // se antes do peão existir um outro peão da equipa adversária,
                    // significa que obrigatóriamente ocorreu o en-passant
                    if(checkIfEnPassantOccurred()){
                        val pos = viewModel.enPassantPiece?.first!!
                        binding.boardView.removePiece(pos)
                        viewModel.getCurrentBoard()?.remove(pos)
                    } else {
                        viewModel.enPassantPiece?.second?.setEnPassant(false)
                    }
                    viewModel.enPassantPiece = null
                    viewModel.possibleEnPassantRemoval = false
                }
                if(binding.startButton.isEnabled){
                    binding.startButton.isEnabled = false
                }

                binding.dailyPuzzle.text = getString(R.string.avaiable_plays)+viewModel.numberOfPlays
            } else {
                board.forEach{ piece ->
                    binding.boardView.removePiece(piece.key)
                }
                viewModel.clean = false
            }
        }

        viewModel.availableMoves.observe(this) { positions ->
            Log.v("APP_TAG", "id: "+Thread.currentThread().id.toString())
            binding.boardView.setAvailablePositions(positions)
        }

        viewModel.winCondition.observe(this) { winCondition ->
            if(winCondition){
                binding.dailyPuzzle.text = getString(R.string.victory_message)
                if(!binding.startButton.isEnabled){
                    binding.startButton.isEnabled = true
                }
            }
        }

        binding.boardView.onTileClickedListener = { tile: Tile, row: Int, column: Int ->
            if(viewModel.winCondition.value == false){
                if(!viewModel.isPieceSelected() && tile.piece != null && tile.piece?.army == viewModel.playerArmy){
                    val possibleMoves = tile.piece?.possibleMoves(Position(row, column), viewModel.getCurrentBoard(), false)
                    viewModel.currentPlayMoves(possibleMoves)
                    viewModel.pieceSelected = Pair(Position(row, column), tile.piece)
                    if(viewModel.enPassantPiece != null){
                        viewModel.possibleEnPassantRemoval = true
                    }
                } else if(viewModel.isPieceSelected() && viewModel.possibleMove(row, column)){
                    if(viewModel.pieceSelected?.first != Position(row, column)){
                        if(viewModel.checkSolution(row, column)){
                            if(viewModel.pieceSelected?.second?.role == Role.PAWN
                                && movedTwoTiles(row, viewModel.pieceSelected?.first?.x!!)
                            ){
                                if(viewModel.enPassantPiece != null){
                                    viewModel.possibleEnPassantRemoval = false
                                    viewModel.enPassantPiece?.second?.setEnPassant(false)
                                }
                                val pawn: Pawn = viewModel.pieceSelected?.second as Pawn
                                pawn.setEnPassant(true)
                                viewModel.enPassantPiece = Pair(Position(row, column), pawn)
                            }
                            viewModel.isToRemove = true
                            viewModel.moved = true
                            viewModel.movePiece(row, column)
                            viewModel.moveSolutionPiece()
                        } else {
                            Toast.makeText(applicationContext,R.string.wrong_move, Toast.LENGTH_SHORT ).show()
                            Log.v("APP_TAG", "Wrong move!")
                        }
                    }
                    viewModel.pieceSelected = null
                    viewModel.currentPlayMoves(listOf())
                }
            }
        }
    }

    private fun checkIfEnPassantOccurred(): Boolean {
        val pos = viewModel.enPassantPiece?.first
        val army = viewModel.enPassantPiece?.second?.army
        val coordinate = if(army == Army.WHITE){
            1
        } else {
            -1
        }
        val position = Position(pos?.x!! + coordinate, pos.y)
        if(viewModel.getCurrentBoard()?.get(position)?.role == Role.PAWN){
            return true
        }
        return false
    }

    private fun movedTwoTiles(row: Int, x: Int): Boolean {
        val compareX = x + 2
        val compareRow = row + 2
        if(row == compareX || x == compareRow){
            return true
        }
        return false
    }
}