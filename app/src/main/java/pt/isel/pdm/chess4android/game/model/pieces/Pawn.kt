package pt.isel.pdm.chess4android.game.model.pieces

import pt.isel.pdm.chess4android.game.model.Army
import pt.isel.pdm.chess4android.game.model.Piece
import pt.isel.pdm.chess4android.game.model.Position
import pt.isel.pdm.chess4android.game.model.Role

class Pawn(
    army: Army
) : Piece(Role.PAWN, army) {

    private var enPassant: Boolean = false

    fun isEnPassantVictim(): Boolean {
        return enPassant
    }

    fun setEnPassant(flag: Boolean){
        enPassant = flag
    }

    override fun possibleMoves(
        currentPosition: Position,
        currentBoard: HashMap<Position, Piece>?,
        onlyIncludePiece: Boolean
    ): List<Position> {
        val moves = mutableListOf<Position>()
        val coordinatesOnArmy = if(army == Army.WHITE){
            if(onlyIncludePiece) {
                1
            } else {
                -1
            }
        } else {
            if(onlyIncludePiece) {
                -1
            } else {
                1
            }
        }

        for(y in -1..1 step 2){
            val check = Position(currentPosition.x + coordinatesOnArmy, currentPosition.y + y)
            if(!onlyIncludePiece && checkPosition(check) && currentBoard?.get(check) != null && currentBoard.get(check)?.checkArmy() != army ||
                onlyIncludePiece && currentBoard?.get(check)?.checkArmy() == army && currentBoard?.get(check)?.checkRole() == Role.PAWN && currentBoard.get(currentPosition) != null && currentBoard?.get(currentPosition)?.checkArmy() != army){
                moves.add(check)
            }
        }

        var mightMoveTwo = false
        var pos = Position(currentPosition.x + coordinatesOnArmy, currentPosition.y)
        if(checkPosition(pos) && currentBoard?.get(pos) == null){
            if(!onlyIncludePiece){
                moves.add(pos)
            }
            mightMoveTwo = true
        } else if(onlyIncludePiece && checkPosition(pos) && currentBoard?.get(pos) != null && currentBoard.get(pos)?.role == Role.PAWN && currentBoard.get(pos)?.checkArmy() == army && currentBoard.get(currentPosition) == null){
            moves.add(pos)
        }
        if(mightMoveTwo && (army == Army.BLACK && currentPosition.x == 1 || army == Army.WHITE && currentPosition.x == 6)){
            pos = Position(currentPosition.x + coordinatesOnArmy + coordinatesOnArmy, currentPosition.y)
            if(!onlyIncludePiece && checkPosition(pos) && currentBoard?.get(pos) == null ||
                onlyIncludePiece && checkPosition(pos) && currentBoard?.get(pos) != null && currentBoard.get(pos)?.role == Role.PAWN && currentBoard.get(pos)?.checkArmy() == army && currentBoard.get(currentPosition) == null){
                moves.add(pos)
            }
        }

        // en-passant
        val leftSide = Position(currentPosition.x, currentPosition.y - 1)
        val leftSidePiece = currentBoard?.get(leftSide)
        val rightSide = Position(currentPosition.x, currentPosition.y + 1)
        val rightSidePiece = currentBoard?.get(rightSide)

        if(leftSidePiece?.army != army && leftSidePiece?.role == role){
            val pawnFound = leftSidePiece as Pawn
            if(pawnFound.isEnPassantVictim()){
                moves.add(Position(leftSide.x + coordinatesOnArmy, leftSide.y))
            }
        }
        if(rightSidePiece?.army != army && rightSidePiece?.role == role){
            val pawnFound = rightSidePiece as Pawn
            if(pawnFound.isEnPassantVictim()){
                moves.add(Position(rightSide.x + coordinatesOnArmy, rightSide.y))
            }
        }

        if(!onlyIncludePiece){
            moves.add(currentPosition)
        }

        return moves
    }
}