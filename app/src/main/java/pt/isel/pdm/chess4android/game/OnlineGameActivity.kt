package pt.isel.pdm.chess4android.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.isel.pdm.chess4android.R
import pt.isel.pdm.chess4android.challenges.ChallengeInfo
import pt.isel.pdm.chess4android.databinding.ActivityGameBinding
import pt.isel.pdm.chess4android.game.model.Board
import pt.isel.pdm.chess4android.game.model.Position
import pt.isel.pdm.chess4android.game.model.Role
import pt.isel.pdm.chess4android.game.model.Type
import pt.isel.pdm.chess4android.game.views.Tile

private const val GAME_EXTRA = "GameActivity.GameInfoExtra"
private const val LOCAL_PLAYER_EXTRA = "GameActivity.LocalPlayerExtra"

/**
 * The activity that displays the board.
 */
class OnlineGameActivity : AppCompatActivity() {

    companion object {
        fun buildIntent(origin: Context, local: Type, turn: Type, challengeInfo: ChallengeInfo) =
            Intent(origin, OnlineGameActivity::class.java)
                .putExtra(GAME_EXTRA, Board(turn = turn).toGameState(challengeInfo.id))
                .putExtra(LOCAL_PLAYER_EXTRA, local.name)
    }

    private val binding: ActivityGameBinding by lazy { ActivityGameBinding.inflate(layoutInflater) }

    private val localPlayer: Type by lazy {
        val local = intent.getStringExtra(LOCAL_PLAYER_EXTRA)
        if (local != null) Type.valueOf(local)
        else throw IllegalArgumentException("Mandatory extra $LOCAL_PLAYER_EXTRA not present")
    }

    private val initialState: GameState by lazy {
        intent.getParcelableExtra<GameState>(GAME_EXTRA) ?:
        throw IllegalArgumentException("Mandatory extra $GAME_EXTRA not present")
    }

    private val viewModel: OnlineGameViewModel by viewModels {
        @Suppress("UNCHECKED_CAST")
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OnlineGameViewModel(application, initialState, localPlayer) as T
            }
        }
    }

    /**
     * Callback method that handles the activity initiation
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.v("TAG", "GameActivity.onCreate()")
        Log.v("TAG", "Local player is $localPlayer")
        Log.v("TAG", "Turn player is ${initialState.turn}")
        viewModel.game.observe(this) {
            updateBoard()
        }

        viewModel.availableMoves.observe(this) { positions ->
            binding.boardView.setAvailablePositions(positions)
        }

        binding.boardView.onTileClickedListener = { tile: Tile, row: Int, column: Int ->
            if(viewModel.winCondition.value == null){
                if(viewModel.isPlayerTurn()){
                    if(!viewModel.isPieceSelected() && tile.piece != null && tile.piece?.army == viewModel.getPlayerArmy()){
                        var possibleMoves: List<Position>?
                        val position = Position(row, column)

                        if(viewModel.doOrDieMoves.count() > 0 && tile.piece?.role == Role.KING){
                            possibleMoves = viewModel.doOrDieMoves
                        } else if(viewModel.kingSecurityMoves.count() > 0 && viewModel.kingSecurityMoves.containsKey(position)){
                            possibleMoves = viewModel.kingSecurityMoves.get(position)
                        } else {
                            if(viewModel.kingSecurityMoves.count() > 0){
                                possibleMoves = mutableListOf(position)
                            } else {
                                possibleMoves = tile.piece?.possibleMoves(position, viewModel.getCurrentBoard(), false)
                            }
                        }
                        if(possibleMoves?.count()!! > 1){
                            val preventAutoCheckMoves = viewModel.checkOccurs(
                                possibleMoves,
                                Pair(Position(row, column), tile.piece)
                            )
                            if(preventAutoCheckMoves.count() >= 1){
                                possibleMoves = preventAutoCheckMoves
                            }
                        }
                        viewModel.currentPlayMoves(possibleMoves)
                        viewModel.pieceSelected = Pair(Position(row, column), tile.piece)
                    } else if(viewModel.isPieceSelected() && viewModel.possibleMove(row, column)){
                        if(viewModel.pieceSelected?.first != Position(row, column)){
                            if(viewModel.doOrDieMoves.count() > 0){
                                viewModel.doOrDieMoves = listOf()
                            }
                            if(viewModel.kingSecurityMoves.count() > 0){
                                viewModel.kingSecurityMoves = mutableMapOf()
                            }
                            viewModel.moved = true
                            viewModel.movePiece(row, column)
                        }
                        viewModel.pieceSelected = null
                        viewModel.currentPlayMoves(listOf())
                    }
                }
            }
        }

        viewModel.winCondition.observe(this) { winCondition ->
            if(winCondition){
                binding.endingText.text = getString(R.string.victory_message)
                if(binding.forfeitButton.isEnabled){
                    binding.forfeitButton.isEnabled = false
                }
            } else {
                binding.endingText.text = getString(R.string.loser_message)
                if(binding.forfeitButton.isEnabled){
                    binding.forfeitButton.isEnabled = false
                }
            }
        }

        binding.forfeitButton.setOnClickListener {
            viewModel.endingGame(warnOtherPlayer = true)
        }
    }

    /**
     * Used to update de board view according to the current state of the game
     */
    private fun updateBoard() {
        binding.forfeitButton.isClickable =
            if (viewModel.game.value?.isSuccess == true)
                viewModel.localPlayer == viewModel.game.value?.getOrThrow()?.turn
            else false

        viewModel.game.value?.onSuccess { board ->
            board.getBoardDisplay()?.forEach { piece ->
                if(piece.value.role == Role.KING){
                    if(viewModel.army != null && viewModel.army == piece.value.army){
                        if(viewModel.check){
                            binding.boardView.setChecked(piece.key)
                        } else {
                            binding.boardView.setChecked(null)
                        }
                    }
                    if(piece.value.army == localPlayer.toArmy()){
                        viewModel.tempKing = piece.key
                    }
                }
                binding.boardView.setPiece(piece.key, piece.value)
            }
        }

        if(viewModel.isPieceSelected()){
            if(viewModel.moved){
                binding.boardView.removePiece(viewModel.pieceSelected?.first!!)
                viewModel.moved = false
            }
        }
    }
}