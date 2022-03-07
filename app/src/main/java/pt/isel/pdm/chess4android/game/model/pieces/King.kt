package pt.isel.pdm.chess4android.game.model.pieces

import pt.isel.pdm.chess4android.game.model.Army
import pt.isel.pdm.chess4android.game.model.Piece
import pt.isel.pdm.chess4android.game.model.Position
import pt.isel.pdm.chess4android.game.model.Role

class King(
    army: Army
) : Piece(Role.KING, army) {

    override fun possibleMoves(
        currentPosition: Position,
        currentBoard: HashMap<Position, Piece>?,
        onlyIncludePiece: Boolean
    ): List<Position> {
        val moves = mutableListOf<Position>()
        for(x in -1..1){
            for(y in -1..1){
                if(x == 0 && y == 0) continue
                val pos = Position(currentPosition.x + x, currentPosition.y + y)
                if(!onlyIncludePiece && checkPosition(pos) && currentBoard?.get(pos)?.checkArmy() != army ||
                        onlyIncludePiece && currentBoard?.get(pos)?.checkArmy() == army && currentBoard?.get(pos)?.checkRole() == Role.KING){
                    moves.add(pos)
                }
            }
        }
        if(!onlyIncludePiece){
            moves.add(currentPosition)
        }
        return moves
    }
}