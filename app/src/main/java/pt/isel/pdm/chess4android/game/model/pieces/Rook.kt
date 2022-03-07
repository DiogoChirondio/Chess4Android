package pt.isel.pdm.chess4android.game.model.pieces

import pt.isel.pdm.chess4android.game.model.Army
import pt.isel.pdm.chess4android.game.model.Piece
import pt.isel.pdm.chess4android.game.model.Role
import pt.isel.pdm.chess4android.game.model.Position

class Rook(
    army: Army
) : Piece(Role.ROOK, army) {

    override fun possibleMoves(
        currentPosition: Position,
        currentBoard: HashMap<Position, Piece>?,
        onlyIncludePiece: Boolean
    ): List<Position> {
        val moves = mutableListOf<Position>()
        val limit = 7

        for(x in currentPosition.x+1..limit){
            val check = Position(x, currentPosition.y)
            if(!onlyIncludePiece && avaluateLine(check, currentBoard, moves) ||
                onlyIncludePiece && foundRook(check, currentBoard, moves) ){
                break
            }
        }

        for(x in currentPosition.x-1 downTo 0){
            val check = Position(x, currentPosition.y)
            if(!onlyIncludePiece && avaluateLine(check, currentBoard, moves) ||
                onlyIncludePiece && foundRook(check, currentBoard, moves) ){
                break
            }
        }

        for(y in currentPosition.y+1..limit){
            val check = Position(currentPosition.x, y)
            if(!onlyIncludePiece && avaluateLine(check, currentBoard, moves) ||
                onlyIncludePiece && foundRook(check, currentBoard, moves) ){
                break
            }
        }

        for(y in currentPosition.y-1 downTo 0){
            val check = Position(currentPosition.x, y)
            if(!onlyIncludePiece && avaluateLine(check, currentBoard, moves) ||
                onlyIncludePiece && foundRook(check, currentBoard, moves) ){
                break
            }
        }

        if(!onlyIncludePiece){
            moves.add(currentPosition)
        }
        return moves
    }

    private fun foundRook(
        check: Position,
        currentBoard: HashMap<Position, Piece>?,
        moves: MutableList<Position>
    ): Boolean {
        if(checkPosition(check) && currentBoard?.get(check) != null){
            if(currentBoard?.get(check)?.checkArmy() == army && currentBoard?.get(check)?.checkRole() == Role.ROOK){
                moves.add(check)
            }
            return true
        }
        return false
    }

    private fun avaluateLine(
        check: Position,
        currentBoard: HashMap<Position, Piece>?,
        moves: MutableList<Position>
    ): Boolean {
        if(checkPosition(check)){
            if(currentBoard?.get(check) != null){
                if(currentBoard?.get(check)?.checkArmy() != army){
                    moves.add(check)
                }
                return true
            }
            moves.add(check)
        }
        return false
    }
}