package pt.isel.pdm.chess4android.daily

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.chess4android.DailyPuzzleApplication
import pt.isel.pdm.chess4android.PuzzleGame
import pt.isel.pdm.chess4android.PuzzleGameDto
import pt.isel.pdm.chess4android.PuzzleRepository
import pt.isel.pdm.chess4android.game.model.ChessInfo
import pt.isel.pdm.chess4android.game.model.Army
import pt.isel.pdm.chess4android.game.model.Piece
import pt.isel.pdm.chess4android.game.model.Position
import pt.isel.pdm.chess4android.game.model.Role
import pt.isel.pdm.chess4android.game.model.pieces.Pawn

class MainActivityViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _boardView: MutableLiveData<HashMap<Position, Piece>> = MutableLiveData()
    val boardView: LiveData<HashMap<Position, Piece>> = _boardView

    private val _availableMoves: MutableLiveData<List<Position>> = MutableLiveData()
    val availableMoves: LiveData<List<Position>> = _availableMoves

    val app = getApplication<DailyPuzzleApplication>()
    private val repo = PuzzleRepository(app.dailyPuzzleService, app.historyDB.getHistoryPuzzleDao())

    var winCondition: MutableLiveData<Boolean> = MutableLiveData()

    var solution: MutableList<String> = mutableListOf()
    var mutableSolution: MutableList<String> = solution

    var isPuzzleSolved: Boolean = false

    var clean: Boolean = false
    var pieceSelected: Pair<Position, Piece?>? = null
    var enPassantPiece: Pair<Position, Pawn?>? = null
    var isToRemove: Boolean = false
    var possibleEnPassantRemoval: Boolean = false
    var moved: Boolean = false
    var numberOfPlays: Int? = -1
    private var dailyPuzzleResponse: PuzzleGame? = null
    var playerArmy: Army? = null

    fun isPieceSelected(): Boolean {
        return pieceSelected != null
    }

    fun currentPlayMoves(possibleMoves: List<Position>?) {
        Log.v("APP_TAG", Thread.currentThread().id.toString())
        _availableMoves.value = possibleMoves
    }

    fun possibleMove(row: Int, column: Int): Boolean {
        val moves = availableMoves.value
        if (moves != null) {
            for(move in moves){
                if(move.x == row && move.y == column){
                    return true
                }
            }
        }
        return false
    }

    fun getCurrentBoard(): HashMap<Position, Piece>? {
        return boardView.value
    }

    fun getDailyPuzzle(puzzleDto: PuzzleGameDto?) {
        if(puzzleDto != null){
            winCondition.value = false
            isPuzzleSolved = puzzleDto.solved
            dailyPuzzleResponse = puzzleDto.puzzleGame
            mutableSolution = dailyPuzzleResponse?.puzzle?.solution!!.toMutableList()
            if(solution.isEmpty()) {
                solution = dailyPuzzleResponse?.puzzle?.solution!!.toMutableList()
                mutableSolution = solution.toMutableList()
            }
            numberOfPlays = mutableSolution.size.div(2).inc()
            _boardView.value = ChessInfo.getDailyPattern(puzzleDto.puzzleGame.game.pgn)
            setPlayerArmy()
        } else {
            repo.fetchPuzzleOfDay { result ->
                result
                    .onSuccess { response ->
                        dailyPuzzleResponse = response
                        winCondition.value = false
                        mutableSolution = dailyPuzzleResponse?.puzzle?.solution!!
                        numberOfPlays = mutableSolution.size.div(2).inc()
                        _boardView.value = ChessInfo.getDailyPattern(dailyPuzzleResponse?.game?.pgn)
                        setPlayerArmy()
                    }
                    .onFailure { Log.e("APP_TAG", "onFailure") }
            }
        }
    }

    fun movePiece(row: Int, column: Int) {
        Log.v("TAG", ""+pieceSelected?.first)
        _boardView.value = ChessInfo.movePiece(boardView.value, pieceSelected, row, column)
    }

    private fun setPlayerArmy() {
        playerArmy = ChessInfo.getArmyColor()
    }

    fun checkSolution(row: Int, column: Int): Boolean {
        val head = mutableSolution.first().toString()
        var position = Position(
            ChessInfo.translateVertical(head[1].toString().toInt()),
            ChessInfo.translateHorizontal(head[0])
        )
        if(pieceSelected?.first == position){
            position = Position(
                ChessInfo.translateVertical(head[3].toString().toInt()),
                ChessInfo.translateHorizontal(head[2])
            )
            if(Position(row, column) == position){
                mutableSolution.removeFirst()
                return true
            }
        }
        return false
    }

    fun moveSolutionPiece() {
        if(mutableSolution.size != 0){
            val move = mutableSolution.removeFirst().toString()
            val position = Position(
                ChessInfo.translateVertical(move[1].toString().toInt()),
                ChessInfo.translateHorizontal(move[0])
            )
            val destinyX = ChessInfo.translateVertical(move[3].toString().toInt())
            val destinyY = ChessInfo.translateHorizontal(move[2])
            val piece = boardView.value?.get(position)
            pieceSelected = Pair(position, piece)
            isToRemove = true
            if(pieceSelected?.second?.role == Role.PAWN
                && boardView.value?.get(Position(destinyX, destinyY)) == null
                && (position.x != destinyX && position.y != destinyY) //andou na diagonal
            ){
                possibleEnPassantRemoval = true
            }
            _boardView.value = ChessInfo.movePiece(boardView.value, pieceSelected, destinyX, destinyY)
        } else {
            winCondition.value = true
            if(!isPuzzleSolved){
                repo.asyncUpdateDB(dailyPuzzleResponse?.game?.id!!) { updateResult ->
                    updateResult.onSuccess {
                        isPuzzleSolved = true
                        Log.v("APP_TAG", "UPDATED BD WITH SUCCESS")
                    }
                    .onFailure {
                        Log.v("APP_TAG", "UPDATED BD FAILED")
                    }
                }
            }
        }
    }

    fun cleanBoard() {
        _boardView.value = boardView.value
    }
}