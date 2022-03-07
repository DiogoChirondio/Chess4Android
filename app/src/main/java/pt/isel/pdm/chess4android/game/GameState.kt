package pt.isel.pdm.chess4android.game

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import pt.isel.pdm.chess4android.game.model.*

/**
 * Data type used to represent the game state externally, that is, when the game state crosses
 * process boundaries and device boundaries.
 */
@Parcelize
data class GameState(
    val id: String,
    val turn: String?,
    val pgn: String
): Parcelable

/**
 * Extension to create a [GameState] instance from this [Board].
 */
fun Board.toGameState(gameId: String): GameState {
    return GameState(id = gameId, turn = turn?.name, pgn = getPGN()!!)
}

/**
 * Extension to create a [Board] instance from this [GameState].
 */
fun GameState.toBoard() = Board(
    turn = if (turn != null) Type.valueOf(turn) else null,
    pgn = pgn,
    board = pgn.toBoardContents()
)

/**
 * Extension to create a list of moves from this string
 */
private fun String.toBoardContents(): HashMap<Position, Piece> {
    val board = ChessInfo.initialBoard()
    var whiteThenBlack = true

    val moves = this.trim().split(" ")

    moves.dropLast(1).forEach { move ->
        ChessInfo.moveConverter(move, board, whiteThenBlack)
        whiteThenBlack = !whiteThenBlack
    }

    return board
}


