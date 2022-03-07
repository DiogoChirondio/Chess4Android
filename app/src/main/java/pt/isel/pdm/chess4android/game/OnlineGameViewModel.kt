package pt.isel.pdm.chess4android.game

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.chess4android.DailyPuzzleApplication
import pt.isel.pdm.chess4android.game.model.*
import pt.isel.pdm.chess4android.game.model.pieces.King

/**
 * The Game screen view model
 */
class OnlineGameViewModel(
    app: Application,
    private val initialGameState: GameState,
    val localPlayer: Type
): AndroidViewModel(app) {

    private val _game: MutableLiveData<Result<Board>> = MutableLiveData(Result.success(initialGameState.toBoard()))
    val game: LiveData<Result<Board>> = _game

    private val _availableMoves: MutableLiveData<List<Position>> = MutableLiveData()
    val availableMoves: LiveData<List<Position>> = _availableMoves

    var tempKing: Position? = null
    var doOrDieMoves: List<Position> = mutableListOf()
    var kingSecurityMoves: MutableMap<Position, List<Position>> = hashMapOf()

    var winCondition: MutableLiveData<Boolean> = MutableLiveData()
    var moved: Boolean = false

    var clearAfterCheck: Boolean = false
    var army: Army? = null
    var check: Boolean = false

    var pieceSelected: Pair<Position, Piece?>? = null

    private val gameSubscription = getApplication<DailyPuzzleApplication>()
        .gamesRepository.subscribeToGameStateChanges(
            challengeId = initialGameState.id,
            onSubscriptionError = { _game.value = Result.failure(it) },
            onGameStateChange = { gameState ->
                if(gameState.turn.equals(localPlayer.name)){
                    checkStatusUpdate()
                    val board = gameState.toBoard()
                    moved = true

                    if(gameState.pgn.trim().split(" ").last() == "L"){
                        winCondition.value = true
                    } else {
                        pieceSelected = ChessInfo.checkPieceWhoMoved(
                            gameState.pgn.trim().split(" ").last(),
                            board.getBoardDisplay()!!,
                            gameState.turn.equals("BLACK"),
                        )

                        check = ChessInfo.moveConverter(
                            gameState.pgn.trim().split(" ").last(),
                            board.getBoardDisplay()!!,
                            gameState.turn.equals("BLACK")
                        )
                        if(check){
                            army = localPlayer.toArmy()
                            if(ChessInfo.getWinCondition()){
                                endingGame(warnOtherPlayer = false)
                                ChessInfo.win = false
                            } else {
                                doOrDieMoves = ChessInfo.checkMateKingSurvivalMoves(
                                    pieceSelected?.second,
                                    ChessInfo.positionTo,
                                    board.getBoardDisplay()
                                )
                                kingSecurityMoves = ChessInfo.checkMateSecurityMoves(
                                    pieceSelected?.second,
                                    ChessInfo.positionTo,
                                    board.getBoardDisplay()
                                )
                            }
                        }

                        _game.value = Result.success(board)
                        pieceSelected = null
                        ChessInfo.positionTo = null
                    }
                }
            }
        )

    fun movePiece(row: Int, column: Int) {
        game.value?.onSuccess { board ->
            checkStatusUpdate()
            val newBoard = board.makeMove(row, column, pieceSelected)
            if(newBoard.pgn?.trim()?.split(" ")?.last()?.contains("+")!!){
                check = true
                army = localPlayer.other.toArmy()
            }
            if(newBoard.pgn.trim().split(" ").last().contains("#")){
                check = true
                army = localPlayer.other.toArmy()
                winCondition.value = true
            }
            _game.value = Result.success(newBoard)
            updateGame(newBoard)
        }
    }

    private fun checkStatusUpdate(){
        if(clearAfterCheck){
            army = null
            clearAfterCheck = false
        }
        if(check){
            check = false
            clearAfterCheck = true
        }
    }

    fun getPlayerArmy(): Army {
        return if(localPlayer == Type.WHITE){
            Army.WHITE
        } else {
            Army.BLACK
        }
    }

    fun getCurrentBoard(): HashMap<Position, Piece>? {
        game.value?.onSuccess { board ->
            return board.getBoardDisplay()
        }
        return null
    }

    fun isPieceSelected(): Boolean {
        return pieceSelected != null
    }

    fun currentPlayMoves(possibleMoves: List<Position>?) {
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

    /**
     * Checks if the player who made the request can make a move
     */
    fun isPlayerTurn(): Boolean {
        game.value?.onSuccess { board ->
            return board.turn == localPlayer
        }
        return false
    }

    /**
     * View model is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        endingGame(warnOtherPlayer = true)
    }

    private fun deleteGame(){
        getApplication<DailyPuzzleApplication>().gamesRepository.deleteGame(
            challengeId = initialGameState.id,
            onComplete = { }
        )
        gameSubscription.remove()
    }

    private fun updateGame(board: Board){
        getApplication<DailyPuzzleApplication>().gamesRepository.updateGameState(
            gameState = board.toGameState(initialGameState.id),
            onComplete = { result ->
                result.onFailure { _game.value = Result.failure(it) }
            }
        )
    }

    fun endingGame(warnOtherPlayer: Boolean){
        if(warnOtherPlayer){
            game.value?.onSuccess { board ->
                val L = "L"
                updateGame(Board(
                    turn = localPlayer.other,
                    pgn = "${board.pgn} $L",
                    board = board.getBoardDisplay()
                ))
            }
        }
        winCondition.value = false
        deleteGame()
    }

    fun checkOccurs(pieceToMovePossibleMoves: List<Position>, pieceToMove: Pair<Position, Piece?>): List<Position> {
        var foundMoves = mutableListOf<Position>()
        game.value?.onSuccess { board ->
            if(pieceToMove.first == tempKing){
                val king = King(localPlayer.toArmy())
                pieceToMovePossibleMoves.forEach { move ->
                    val boardDisplay = board.getBoardDisplay()?.toMutableMap() as HashMap
                    ChessInfo.movePiece(boardDisplay, pieceToMove, move.x, move.y)
                    if(move != pieceToMove.first){
                        if(king.canBeEatenBy(
                                move,
                                boardDisplay,
                                localPlayer.other.toArmy()
                            ).count() == 0){
                            foundMoves.add(move)
                        }
                    }
                }
            } else {
                pieceToMovePossibleMoves.forEach { move ->
                    val boardDisplay = board.getBoardDisplay()?.toMutableMap() as HashMap
                    if(move != pieceToMove.first){
                        ChessInfo.movePiece(boardDisplay, pieceToMove, move.x, move.y)
                        if(boardDisplay.get(tempKing)?.canBeEatenBy(
                                tempKing!!,
                                boardDisplay,
                                localPlayer.other.toArmy()
                            )?.count()!! == 0){
                            foundMoves.add(move)
                        }
                    }
                }
            }
        }
        foundMoves.add(pieceToMove.first)
        return foundMoves
    }
}