package pt.isel.pdm.chess4android.game.model

/**
 * Represents an Online Chess Board. Instances are immutable.
 *
 * @property turn   The next player to move, or null if the game has already ended
 * @property board  The board tiles
 */
data class Board(
    val turn: Type? = Type.firstToMove,
    val pgn: String? = "",
    private val board: HashMap<Position, Piece>? = ChessInfo.initialBoard()
) {

    fun getPGN(): String? = pgn

    fun getBoardDisplay(): HashMap<Position, Piece>? = board

    fun makeMove(row: Int, column: Int, pieceSelected: Pair<Position, Piece?>?): Board {
        return Board(
            turn = turn?.other,
            pgn = ChessInfo.moveMaker(pgn, board, pieceSelected, row, column),
            board = ChessInfo.movePiece(board, pieceSelected, row, column)
        )
    }
}