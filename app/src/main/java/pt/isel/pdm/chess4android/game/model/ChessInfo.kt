package pt.isel.pdm.chess4android.game.model

import pt.isel.pdm.chess4android.game.model.pieces.*

class ChessInfo {

    companion object {
        var white = Army.WHITE
        var win = false
        var positionTo: Position? = null

        fun getDailyPattern(pgn: String?): HashMap<Position, Piece> {
            val board = initialBoard()

            var whiteThenBlack = true
            val moves: List<String>? = pgn?.trim()?.split(" ")
            moves?.forEach { move ->
                moveConverter(move, board, whiteThenBlack)
                whiteThenBlack = !whiteThenBlack
            }

            setArmyColor(whiteThenBlack)
            return board
        }

        private fun setArmyColor(whiteThenBlack: Boolean) {
            white = if(whiteThenBlack){
                Army.WHITE
            } else {
                Army.BLACK
            }
        }

        fun getArmyColor(): Army {
            return white
        }

        fun getWinCondition(): Boolean {
            return win
        }

        fun moveConverter(moveReceived: String, board: HashMap<Position, Piece>, white: Boolean): Boolean {
            var move = moveReceived
            var check = false
            if(moveReceived.contains("+")){
                check = true
                move = moveReceived.replace("+", "")
            } else if(moveReceived.contains("#")){
                check = true
                move = moveReceived.replace("#", "")
                win = true
            }

            val characters = move.count()
            when(characters){
                2 -> {
                    val y = translateHorizontal(move[0])
                    val x = translateVertical(move[1].toString().toInt())
                    movePiece(board, selectPawn(white, board, y, x), x, y)
                }
                3 -> {
                    if(checkCastling(move, board, white, "O-O", 6, 5, 7)){
                        return check
                    } else {
                        val piece = translatePiece(move[0], white)
                        val y = translateHorizontal(move[1])
                        val x = translateVertical(move[2].toString().toInt())
                        movePiece(board, selectPiece(piece, board, y, x), x, y)
                    }
                }
                4 -> {
                    if(move.contains("x")){
                        val split = move.split("x")
                        val pieceRepresentative = split[0]
                        val pieceMovement = split[1]

                        val piece = translatePiece(pieceRepresentative[0], white)
                        val y = translateHorizontal(pieceMovement[0])
                        val x = translateVertical(pieceMovement[1].toString().toInt())
                        val selectedPiece = if(piece.role == Role.PAWN){
                            selectPieceFromMultiple(piece, board, y, x, pieceRepresentative[0])
                        } else {
                            selectPiece(piece, board, y, x)
                        }
                        movePiece(board, selectedPiece, x, y)
                    } else if(move.contains("=")){
                        val split = move.split("=")
                        val pieceMovement = split[0]
                        val transformInto = split[1]

                        val y = translateHorizontal(pieceMovement[0])
                        val x = translateVertical(pieceMovement[1].toString().toInt())
                        movePiece(board, selectPawn(white, board, y, x), x, y)
                        transformInto(board, translatePiece(transformInto[0], white), x, y)
                    } else {
                        val piece = translatePiece(move[0], white)
                        val y = translateHorizontal(move[2])
                        val x = translateVertical(move[3].toString().toInt())
                        movePiece(board, selectPieceFromMultiple(piece, board, y, x, move[1]), x, y)
                    }
                }
                5 -> {
                    if(move.contains("x")){
                        val split = move.split("x")
                        val pieceRepresentative = split[0]
                        val pieceMovement = split[1]

                        val piece = translatePiece(pieceRepresentative[0], white)
                        val y = translateHorizontal(pieceMovement[0])
                        val x = translateVertical(pieceMovement[1].toString().toInt())
                        movePiece(board, selectPieceFromMultiple(piece, board, y, x, pieceRepresentative[1]), x, y)
                    } else {
                        checkCastling(move, board, white, "O-O-O", 2, 3, 0)
                    }
                }
                6 -> {
                    val firstSplit = move.split("=")
                    val transformTo = firstSplit[1]

                    val secondSplit = firstSplit[0].split("x")
                    val pieceRepresentative = secondSplit[0]
                    val pieceMovement = secondSplit[1]

                    val piece = translatePiece(pieceRepresentative[0], white)
                    val y = translateHorizontal(pieceMovement[0])
                    val x = translateVertical(pieceMovement[1].toString().toInt())
                    movePiece(board, selectPieceFromMultiple(piece, board, y, x, pieceRepresentative[0]), x, y)
                    transformInto(board, translatePiece(transformTo[0], white), x, y)
                }
            }
            return check
        }

        fun checkPieceWhoMoved(
            moveReceived: String,
            board: HashMap<Position, Piece>,
            white: Boolean
        ): Pair<Position, Piece?>? {
            val move = moveReceived.replace("+", "").replace("#", "")

            val characters = move.count()
            when(characters){
                2 -> {
                    val y = translateHorizontal(move[0])
                    val x = translateVertical(move[1].toString().toInt())
                    if(positionTo == null){
                        positionTo = Position(x, y)
                    }
                    return selectPawn(white, board, y, x)
                }
                3 -> {
                    /*if(checkCastling(move, board, white, "O-O", 6, 5, 7)){
                        return
                    }*/
                    val piece = translatePiece(move[0], white)
                    val y = translateHorizontal(move[1])
                    val x = translateVertical(move[2].toString().toInt())
                    if(positionTo == null){
                        positionTo = Position(x, y)
                    }
                    return selectPiece(piece, board, y, x)
                }
                4 -> {
                    if(move.contains("x")) {
                        val split = move.split("x")
                        val pieceRepresentative = split[0]
                        val pieceMovement = split[1]

                        val piece = translatePiece(pieceRepresentative[0], white)
                        val y = translateHorizontal(pieceMovement[0])
                        val x = translateVertical(pieceMovement[1].toString().toInt())
                        if(positionTo == null){
                            positionTo = Position(x, y)
                        }
                        return if (piece.role == Role.PAWN) {
                            selectPieceFromMultiple(piece, board, y, x, pieceRepresentative[0])
                        } else {
                            selectPiece(piece, board, y, x)
                        }
                    } else if(move.contains("=")){
                        val split = move.split("=")
                        val pieceMovement = split[0]

                        val y = translateHorizontal(pieceMovement[0])
                        val x = translateVertical(pieceMovement[1].toString().toInt())
                        if(positionTo == null){
                            positionTo = Position(x, y)
                        }
                        return selectPawn(white, board, y, x)
                    } else {
                        val piece = translatePiece(move[0], white)
                        val y = translateHorizontal(move[2])
                        val x = translateVertical(move[3].toString().toInt())
                        if(positionTo == null){
                            positionTo = Position(x, y)
                        }
                        return selectPieceFromMultiple(piece, board, y, x, move[1])
                    }
                }
                5 -> {
                    if(move.contains("x")){
                        val split = move.split("x")
                        val pieceRepresentative = split[0]
                        val pieceMovement = split[1]

                        val piece = translatePiece(pieceRepresentative[0], white)
                        val y = translateHorizontal(pieceMovement[0])
                        val x = translateVertical(pieceMovement[1].toString().toInt())
                        if(positionTo == null){
                            positionTo = Position(x, y)
                        }
                        return selectPieceFromMultiple(piece, board, y, x, pieceRepresentative[1])
                    } /*else {
                        checkCastling(move, board, white, "O-O-O", 2, 3, 0)
                    }*/
                }
                6 -> {
                    val firstSplit = move.split("=")
                    val transformTo = firstSplit[1]

                    val secondSplit = firstSplit[0].split("x")
                    val pieceRepresentative = secondSplit[0]
                    val pieceMovement = secondSplit[1]

                    val piece = translatePiece(pieceRepresentative[0], white)
                    val y = translateHorizontal(pieceMovement[0])
                    val x = translateVertical(pieceMovement[1].toString().toInt())
                    if(positionTo == null){
                        positionTo = Position(x, y)
                    }
                    return selectPieceFromMultiple(piece, board, y, x, pieceRepresentative[0])
                }
            }
            return null
        }

        fun checkMateKingSurvivalMoves(
            pieceWhoChecked: Piece?,
            pieceWhoCheckedPosition: Position?,
            updatedBoard: HashMap<Position, Piece>?
        ): List<Position> {
            val moves = mutableListOf<Position>()

            val checkForCheckMate = pieceWhoChecked?.possibleMoves(pieceWhoCheckedPosition!!, updatedBoard, false)
            checkForCheckMate?.forEach { position ->
                if(updatedBoard?.get(position)?.role == Role.KING){
                    val king = if(pieceWhoChecked.army == Army.WHITE){
                        King(Army.BLACK)
                    } else {
                        King(Army.WHITE)
                    }

                    //We need to remove the king temporarily so that he doesn't block the way of a possible check
                    val tempBoard = updatedBoard.toMutableMap() as HashMap
                    tempBoard.remove(position)

                    //King can run away
                    val possibleEscapes = king.possibleMoves(position, tempBoard, false)
                    possibleEscapes.forEach {
                        if(king.canBeEatenBy(it, tempBoard, pieceWhoChecked.army).count() == 0){
                            moves.add(it)
                        }
                    }

                    moves.add(position)
                }
            }

            return moves
        }

        fun checkMateSecurityMoves(
            pieceWhoChecked: Piece?,
            pieceWhoCheckedPosition: Position?,
            updatedBoard: HashMap<Position, Piece>?
        ): MutableMap<Position, List<Position>> {
            val moves = mutableMapOf<Position, List<Position>>()

            val checkForCheckMate = pieceWhoChecked?.possibleMoves(pieceWhoCheckedPosition!!, updatedBoard, false)
            checkForCheckMate?.forEach { position ->
                if(updatedBoard?.get(position)?.role == Role.KING){
                    val king = if(pieceWhoChecked.army == Army.WHITE){
                        King(Army.BLACK)
                    } else {
                        King(Army.WHITE)
                    }

                    //Piece who checked can be eaten
                    pieceWhoChecked.canBeEatenBy(pieceWhoCheckedPosition!!, updatedBoard, king.army).forEach {
                        if(it.first != pieceWhoCheckedPosition){
                            val pieceFoundMoves = mutableListOf<Position>()
                            pieceFoundMoves.add(it.first)
                            pieceFoundMoves.add(pieceWhoCheckedPosition)

                            moves.put(it.first, pieceFoundMoves)
                        }
                    }

                    //Piece who checked can be blocked
                    val pieceWhoCheckedMoves = pieceWhoChecked.possibleMoves(pieceWhoCheckedPosition, updatedBoard, false)
                    pieceWhoCheckedMoves.forEach { move ->
                        if(correctPathBlocked(updatedBoard, move, king, pieceWhoChecked, pieceWhoCheckedPosition)){
                            pieceWhoChecked.canBeEatenBy(move, updatedBoard, king.army, Role.KING, true).forEach {
                                val pieceFoundMoves = mutableListOf<Position>()
                                pieceFoundMoves.add(it.first)
                                pieceFoundMoves.add(move)

                                if(moves.containsKey(it.first)){
                                    moves.get(it.first)?.forEach{
                                        pieceFoundMoves.add(it)
                                    }
                                    moves.replace(it.first, pieceFoundMoves)
                                } else {
                                    moves.put(it.first, pieceFoundMoves)
                                }
                            }
                        }
                    }
                }
            }

            return moves
        }

        fun moveMaker(
            pgn: String?,
            board: HashMap<Position, Piece>?,
            pieceSelected: Pair<Position, Piece?>?,
            row: Int,
            column: Int
        ): String {
            var pgnMove = ""
            val piece = pieceSelected?.second

            if(piece!!.role != Role.PAWN){
                pgnMove += transformPiece(pieceSelected.second!!)
            }

            val possibleMoves = piece.possibleMoves(Position(row, column), board, true)
            if(possibleMoves.count() == 2){
                if(possibleMoves[0].y == possibleMoves[1].y){
                    pgnMove += transformVertical(pieceSelected.first.x)
                } else {
                    pgnMove += transformHorizontal(pieceSelected.first.y)
                }
            } else if(possibleMoves.count() > 2){
                pgnMove += transformHorizontal(pieceSelected.first.y)
                pgnMove += transformVertical(pieceSelected.first.x)
            }

            if(board?.get(Position(row, column)) != null){
                pgnMove += 'x'
            }

            pgnMove += transformHorizontal(column)
            pgnMove += transformVertical(row)

            val position = Position(row, column)

            //update board for doOrDie because we use possibleMoves() with onlyIncludePiece=true
            val tempBoard: HashMap<Position, Piece> = board?.toMutableMap() as HashMap<Position, Piece>
            tempBoard.put(position, piece)
            tempBoard.remove(pieceSelected.first)

            val checkForCheckMate = piece.possibleMoves(position, tempBoard, false)
            checkForCheckMate.forEach {
                val pieceFound = tempBoard.get(it)
                if(pieceFound?.role == Role.KING && pieceFound.army != piece.army){
                    if(doOrDie(it, tempBoard, position, piece, checkForCheckMate)){
                        pgnMove += '+'
                    } else {
                        pgnMove += '#'
                    }
                }
            }

            return "$pgn $pgnMove"
        }

        private fun doOrDie(
            kingPosition: Position,
            board: HashMap<Position, Piece>,
            pieceWhoCheckedPosition: Position,
            pieceWhoChecked: Piece,
            pieceWhoCheckedMoves: List<Position>
        ): Boolean {
            val king = if(pieceWhoChecked.army == Army.WHITE){
                King(Army.BLACK)
            } else {
                King(Army.WHITE)
            }

            //King can run away
            val possibleEscapes = king.possibleMoves(kingPosition, board, false)
            possibleEscapes.forEach {
                if(king.canBeEatenBy(it, board, pieceWhoChecked.army).count() == 0){
                    return true
                }
            }

            //Piece who checked can be eaten
            if(pieceWhoChecked.canBeEatenBy(pieceWhoCheckedPosition, board, king.army).count() > 0){
                return true
            }

            //Piece who checked can be blocked
            pieceWhoCheckedMoves.forEach { position ->
                if(pieceWhoChecked.canBeEatenBy(position, board, king.army, Role.KING, true).count() > 0){
                    //Check if blocked the correct path to the king
                    if(correctPathBlocked(board, position, king, pieceWhoChecked, pieceWhoCheckedPosition)){
                        return true
                    }
                }
            }

            return false
        }

        private fun correctPathBlocked(
            board: HashMap<Position, Piece>,
            position: Position,
            king: King,
            pieceWhoChecked: Piece,
            pieceWhoCheckedPosition: Position
        ): Boolean{
            if(board.get(position)?.role == Role.KING){
                return false
            }
            val tempBoard: HashMap<Position, Piece> = board.toMutableMap() as HashMap<Position, Piece>
            tempBoard.put(position, Pawn(king.army)) //Imaginary Pawn to block the way

            val checkForCheckMate = pieceWhoChecked.possibleMoves(pieceWhoCheckedPosition, tempBoard, false)
            checkForCheckMate.forEach {
                if(tempBoard.get(it)?.role == Role.KING){
                    return false
                }
            }
            return true
        }

        private fun checkCastling(
            move: String,
            board: HashMap<Position, Piece>,
            white: Boolean,
            type: String,
            kingColumn: Int,
            rookColumn: Int,
            y: Int
        ): Boolean {
            if(move.compareTo(type) == 0){
                val army: Army
                val kingPos: Position
                val rookPos: Position
                val row: Int

                if(white){
                    army = Army.WHITE
                    kingPos = Position(7,4)
                    row = 7
                    rookPos = Position(7,y)
                } else {
                    army = Army.BLACK
                    kingPos = Position(0,4)
                    row = 0
                    rookPos = Position(0,y)
                }
                movePiece(board, Pair(kingPos, King(army)), row, kingColumn)
                movePiece(board, Pair(rookPos, Rook(army)), row, rookColumn)
                return true
            }
            return false
        }

        private fun selectPieceFromMultiple(
            piece: Piece,
            board: HashMap<Position, Piece>,
            y: Int,
            x: Int,
            rowOrColumn: Char,
        ): Pair<Position, Piece?>? {
            val possibleMoves = piece.possibleMoves(Position(x, y), board, true)
            var moveChosen = Position(0,0)
            if(rowOrColumn.isDigit()){
                val vertical = translateVertical(rowOrColumn.toString().toInt())
                possibleMoves.forEach{ position ->
                    if(position.x == vertical){
                        moveChosen = position
                    }
                }
            } else {
                val horizontal = translateHorizontal(rowOrColumn)
                possibleMoves.forEach{ position ->
                    if(position.y == horizontal){
                        moveChosen = position
                    }
                }
            }

            val pieceFound = board.get(moveChosen)
            return Pair(moveChosen, pieceFound)
        }

        private fun selectPiece(
            piece: Piece,
            board: HashMap<Position, Piece>,
            y: Int,
            x: Int,
        ): Pair<Position, Piece?>? {
            val possibleMoves = piece.possibleMoves(Position(x, y), board, true)
            val pieceFound = board.get(possibleMoves[0])
            return Pair(possibleMoves[0], pieceFound)
        }

        private fun selectPawn(white: Boolean, board: HashMap<Position, Piece>, y: Int, x: Int): Pair<Position, Piece?>? {
            if(white) {
                var position = Position(x + 1, y)
                var pawn = board.get(position)
                if(pawn == null){
                    position = Position(x + 2, y)
                    pawn = board.get(position)
                }
                return Pair(position, pawn)
            } else {
                var position = Position(x - 1, y)
                var pawn = board.get(position)
                if(pawn == null){
                    position = Position(x - 2, y)
                    pawn = board.get(position)
                }
                return Pair(position, pawn)
            }
        }

        private fun transformInto(
            board: HashMap<Position, Piece>?,
            translatePiece: Piece,
            row: Int,
            column: Int
        ) {
            val position = Position(row, column)
            board?.remove(position)
            board?.put(position, translatePiece)
        }

        fun movePiece(
            board: HashMap<Position, Piece>?,
            pieceSelected: Pair<Position, Piece?>?,
            row: Int,
            column: Int
        ): HashMap<Position, Piece>? {
            board?.remove(pieceSelected?.first)
            board?.put(Position(row, column), pieceSelected?.second!!)
            return board
        }

        fun translateHorizontal(c: Char): Int {
            when(c){
                'a' -> return 0
                'b' -> return 1
                'c' -> return 2
                'd' -> return 3
                'e' -> return 4
                'f' -> return 5
                'g' -> return 6
                'h' -> return 7
            }
            return -1
        }

        fun transformHorizontal(i: Int): Char {
            when(i){
                0 -> return 'a'
                1 -> return 'b'
                2 -> return 'c'
                3 -> return 'd'
                4 -> return 'e'
                5 -> return 'f'
                6 -> return 'g'
                7 -> return 'h'
            }
            return ' '
        }

        fun translateVertical(i: Int): Int {
            when(i){
                1 -> return 7
                2 -> return 6
                3 -> return 5
                4 -> return 4
                5 -> return 3
                6 -> return 2
                7 -> return 1
                8 -> return 0
            }
            return -1
        }

        fun transformVertical(i: Int): Int {
            when(i){
                7 -> return 1
                6 -> return 2
                5 -> return 3
                4 -> return 4
                3 -> return 5
                2 -> return 6
                1 -> return 7
                0 -> return 8
            }
            return -1
        }

        private fun translatePiece(c: Char, white: Boolean): Piece {
            val army = if(white){
                Army.WHITE
            } else {
                Army.BLACK
            }
            when(c){
                'N' -> return Knight(army)
                'B' -> return Bishop(army)
                'R' -> return Rook(army)
                'Q' -> return Queen(army)
                'K' -> return King(army)
            }
            return Pawn(army)
        }

        private fun transformPiece(piece: Piece): Char {
            when(piece.role){
                Role.KNIGHT -> return 'N'
                Role.BISHOP -> return 'B'
                Role.ROOK   -> return 'R'
                Role.QUEEN  -> return 'Q'
                Role.KING   -> return 'K'
            }
            return ' '
        }

        fun initialBoard(): HashMap<Position, Piece> {
            return hashMapOf(Pair(Position(0,0), Rook(Army.BLACK)),
                Pair(Position(0,1), Knight(Army.BLACK)),
                Pair(Position(0,2), Bishop(Army.BLACK)),
                Pair(Position(0,3), Queen(Army.BLACK)),
                Pair(Position(0,4), King(Army.BLACK)),
                Pair(Position(0,5), Bishop(Army.BLACK)),
                Pair(Position(0,6), Knight(Army.BLACK)),
                Pair(Position(0,7), Rook(Army.BLACK)),
                Pair(Position(1,0), Pawn(Army.BLACK)),
                Pair(Position(1,1), Pawn(Army.BLACK)),
                Pair(Position(1,2), Pawn(Army.BLACK)),
                Pair(Position(1,3), Pawn(Army.BLACK)),
                Pair(Position(1,4), Pawn(Army.BLACK)),
                Pair(Position(1,5), Pawn(Army.BLACK)),
                Pair(Position(1,6), Pawn(Army.BLACK)),
                Pair(Position(1,7), Pawn(Army.BLACK)),
                Pair(Position(7,0), Rook(Army.WHITE)),
                Pair(Position(7,1), Knight(Army.WHITE)),
                Pair(Position(7,2), Bishop(Army.WHITE)),
                Pair(Position(7,3), Queen(Army.WHITE)),
                Pair(Position(7,4), King(Army.WHITE)),
                Pair(Position(7,5), Bishop(Army.WHITE)),
                Pair(Position(7,6), Knight(Army.WHITE)),
                Pair(Position(7,7), Rook(Army.WHITE)),
                Pair(Position(6,0), Pawn(Army.WHITE)),
                Pair(Position(6,1), Pawn(Army.WHITE)),
                Pair(Position(6,2), Pawn(Army.WHITE)),
                Pair(Position(6,3), Pawn(Army.WHITE)),
                Pair(Position(6,4), Pawn(Army.WHITE)),
                Pair(Position(6,5), Pawn(Army.WHITE)),
                Pair(Position(6,6), Pawn(Army.WHITE)),
                Pair(Position(6,7), Pawn(Army.WHITE)))
        }
    }
}
