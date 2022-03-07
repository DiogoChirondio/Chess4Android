package pt.isel.pdm.chess4android.game.model.pieces

import pt.isel.pdm.chess4android.game.model.Army
import pt.isel.pdm.chess4android.game.model.Piece
import pt.isel.pdm.chess4android.game.model.Role
import pt.isel.pdm.chess4android.game.model.Position

class Queen(
    army: Army
) : Piece(Role.QUEEN, army) {

    override fun possibleMoves(
        currentPosition: Position,
        currentBoard: HashMap<Position, Piece>?,
        onlyIncludePiece: Boolean
    ): List<Position> {
        val moves = mutableListOf<Position>()
        val limit = 7
        var moveSide = 1

        for(x in currentPosition.x+1..limit){
            val check = Position(x, currentPosition.y)
            if(!onlyIncludePiece && avaluate(check, currentBoard, moves) ||
                onlyIncludePiece && foundQueen(check, currentBoard, moves)){
                break
            }
        }

        for(x in currentPosition.x+1..limit){
            val check = Position(x, currentPosition.y + moveSide)
            if(!onlyIncludePiece && avaluate(check, currentBoard, moves) ||
                onlyIncludePiece && foundQueen(check, currentBoard, moves)){
                break
            }
            moveSide++
        }
        moveSide = 1

        for(x in currentPosition.x-1 downTo 0){
            val check = Position(x, currentPosition.y)
            if(!onlyIncludePiece && avaluate(check, currentBoard, moves) ||
                onlyIncludePiece && foundQueen(check, currentBoard, moves)){
                break
            }
        }

        for(x in currentPosition.x-1 downTo 0){
            val check = Position(x, currentPosition.y - moveSide)
            if(!onlyIncludePiece && avaluate(check, currentBoard, moves) ||
                onlyIncludePiece && foundQueen(check, currentBoard, moves)){
                break
            }
            moveSide++
        }
        moveSide = 1

        for(y in currentPosition.y+1..limit){
            val check = Position(currentPosition.x, y)
            if(!onlyIncludePiece && avaluate(check, currentBoard, moves) ||
                onlyIncludePiece && foundQueen(check, currentBoard, moves)){
                break
            }
        }

        for(y in currentPosition.y+1..limit){
            val check = Position(currentPosition.x - moveSide, y)
            if(!onlyIncludePiece && avaluate(check, currentBoard, moves) ||
                onlyIncludePiece && foundQueen(check, currentBoard, moves)){
                break
            }
            moveSide++
        }
        moveSide = 1

        for(y in currentPosition.y-1 downTo 0){
            val check = Position(currentPosition.x, y)
            if(!onlyIncludePiece && avaluate(check, currentBoard, moves) ||
                onlyIncludePiece && foundQueen(check, currentBoard, moves)){
                break
            }
        }

        for(y in currentPosition.y-1 downTo 0){
            val check = Position(currentPosition.x + moveSide, y)
            if(!onlyIncludePiece && avaluate(check, currentBoard, moves) ||
                onlyIncludePiece && foundQueen(check, currentBoard, moves)){
                break
            }
            moveSide++
        }
        moveSide = 1

        if(!onlyIncludePiece){
            moves.add(currentPosition)
        }
        return moves
    }

    private fun foundQueen(
        check: Position,
        currentBoard: HashMap<Position, Piece>?,
        moves: MutableList<Position>
    ): Boolean {
        if(checkPosition(check) && currentBoard?.get(check) != null){
            if(currentBoard?.get(check)?.checkArmy() == army && currentBoard?.get(check)?.checkRole() == Role.QUEEN){
                moves.add(check)
            }
            return true
        }
        return false
    }

    private fun avaluate(
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