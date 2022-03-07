package pt.isel.pdm.chess4android.game.model

import pt.isel.pdm.chess4android.game.model.pieces.*

enum class Army {
    WHITE, BLACK
}

enum class Role {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING
}

abstract class Piece(
    val role: Role,
    val army: Army,
) {
    fun getPair(): Pair<Army, Role> {
        return Pair(army, role)
    }

    abstract fun possibleMoves(
        currentPosition: Position,
        currentBoard: HashMap<Position, Piece>?,
        onlyIncludePiece: Boolean
    ): List<Position>

    protected fun checkPosition(pos: Position): Boolean{
        return pos.x >= 0 && pos.y >= 0
    }

    fun checkArmy(): Army {
        return army
    }

    fun checkRole(): Role {
        return role
    }

    fun canBeEatenBy(
        position: Position,
        board: HashMap<Position, Piece>,
        armyWhoEats: Army,
        roleException: Role? = null,
        pawnCanBlock: Boolean = false
    ): List<Pair<Position, Piece?>> {
        val possiblePieces = mutableListOf<Pair<Position, Piece?>>()
        val tempPossiblePieces = mutableListOf<Pair<Position, Piece?>>()

        //Knight
        Knight(armyWhoEats).possibleMoves(position, board, true).forEach {
            tempPossiblePieces.add(Pair(it, board.get(it)))
        }
        possiblePieces.addAll(applyRoleException(tempPossiblePieces, Role.KNIGHT, roleException))
        tempPossiblePieces.clear()

        //Queen
        Queen(armyWhoEats).possibleMoves(position, board, true).forEach {
            tempPossiblePieces.add(Pair(it, board.get(it)))
        }
        possiblePieces.addAll(applyRoleException(tempPossiblePieces, Role.QUEEN, roleException))
        tempPossiblePieces.clear()

        //Bishop
        Bishop(armyWhoEats).possibleMoves(position, board, true).forEach {
            tempPossiblePieces.add(Pair(it, board.get(it)))
        }
        possiblePieces.addAll(applyRoleException(tempPossiblePieces, Role.BISHOP, roleException))
        tempPossiblePieces.clear()

        //Rook
        Rook(armyWhoEats).possibleMoves(position, board, true).forEach {
            tempPossiblePieces.add(Pair(it, board.get(it)))
        }
        possiblePieces.addAll(applyRoleException(tempPossiblePieces, Role.ROOK, roleException))
        tempPossiblePieces.clear()

        //King
        King(armyWhoEats).possibleMoves(position, board, true).forEach {
            tempPossiblePieces.add(Pair(it, board.get(it)))
        }
        possiblePieces.addAll(applyRoleException(tempPossiblePieces, Role.KING, roleException))
        tempPossiblePieces.clear()

        //Pawn
        Pawn(armyWhoEats).possibleMoves(position, board, true).forEach {
            tempPossiblePieces.add(Pair(it, board.get(it)))
        }
        //filter front movements for pawns
        if(!pawnCanBlock){
            tempPossiblePieces.forEach{
                if(it.first.y == position.y){
                    tempPossiblePieces.remove(it)
                }
            }
        }

        possiblePieces.addAll(applyRoleException(tempPossiblePieces, Role.PAWN, roleException))
        tempPossiblePieces.clear()

        return possiblePieces
    }

    private fun applyRoleException(
        possibleMoves: MutableList<Pair<Position, Piece?>>,
        role: Role,
        roleException: Role?
    ): Collection<Pair<Position, Piece?>> {
        if(roleException != null && role == roleException){
            possibleMoves.clear()
        }
        return possibleMoves
    }
}
