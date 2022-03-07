package pt.isel.pdm.chess4android.game.model

enum class Type {
    WHITE,
    BLACK;

    companion object {
        val firstToMove: Type = WHITE
    }

    val other: Type
        get() = if (this == WHITE) BLACK else WHITE

    fun toArmy(): Army {
        return if(this == WHITE) Army.WHITE
        else Army.BLACK
    }
}