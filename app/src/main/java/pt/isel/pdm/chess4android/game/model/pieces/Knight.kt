package pt.isel.pdm.chess4android.game.model.pieces

import pt.isel.pdm.chess4android.game.model.Army
import pt.isel.pdm.chess4android.game.model.Piece
import pt.isel.pdm.chess4android.game.model.Position
import pt.isel.pdm.chess4android.game.model.Role

class Knight(
    army: Army
) : Piece(Role.KNIGHT, army) {

    override fun possibleMoves(
        currentPosition: Position,
        currentBoard: HashMap<Position, Piece>?,
        onlyIncludePiece: Boolean
    ): List<Position> {
        val moves = mutableListOf<Position>()
        val arr = arrayOf(-1, 1)
        for(x in arr){
            for(y in arr){
                val pos1 = Position(currentPosition.x + x + x, currentPosition.y + y)
                val pos2 = Position(currentPosition.x + x, currentPosition.y + y + y)
                if(!onlyIncludePiece && checkPosition(pos1) && currentBoard?.get(pos1)?.checkArmy() != army ||
                    onlyIncludePiece && currentBoard?.get(pos1)?.checkArmy() == army && currentBoard?.get(pos1)?.checkRole() == Role.KNIGHT
                ){
                    moves.add(pos1)
                }
                if(!onlyIncludePiece && checkPosition(pos2) && currentBoard?.get(pos2)?.checkArmy() != army ||
                    onlyIncludePiece && currentBoard?.get(pos2)?.checkArmy() == army && currentBoard?.get(pos2)?.checkRole() == Role.KNIGHT
                ){
                    moves.add(pos2)
                }
            }
        }
        if(!onlyIncludePiece){
            moves.add(currentPosition)
        }
        return moves
    }
}