package pt.isel.pdm.chess4android.game.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.GridLayout
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import pt.isel.pdm.chess4android.*
import pt.isel.pdm.chess4android.game.model.*
import kotlin.collections.HashMap

typealias TileTouchListener = (tile: Tile, row: Int, column: Int) -> Unit

/**
 * Custom view that implements a chess board.
 */
@SuppressLint("ClickableViewAccessibility")
class BoardView(private val ctx: Context, attrs: AttributeSet?) : GridLayout(ctx, attrs) {

    private val tilesList: HashMap<Position, Tile> = HashMap()
    private val possibleMovesTiles = mutableListOf<Position>()
    private var checkedTile: Position? = null

    private val side = 8

    private val brush = Paint().apply {
        ctx.resources.getColor(R.color.chess_board_black, null)
        style = Paint.Style.STROKE
        strokeWidth = 10F
    }

    private fun createImageEntry(army: Army, role: Role, imageId: Int) =
        Pair(Pair(army, role), VectorDrawableCompat.create(ctx.resources, imageId, null))

    private val piecesImages = mapOf(
        createImageEntry(Army.WHITE, Role.PAWN, R.drawable.ic_white_pawn),
        createImageEntry(Army.WHITE, Role.KNIGHT, R.drawable.ic_white_knight),
        createImageEntry(Army.WHITE, Role.BISHOP, R.drawable.ic_white_bishop),
        createImageEntry(Army.WHITE, Role.ROOK, R.drawable.ic_white_rook),
        createImageEntry(Army.WHITE, Role.QUEEN, R.drawable.ic_white_queen),
        createImageEntry(Army.WHITE, Role.KING, R.drawable.ic_white_king),
        createImageEntry(Army.BLACK, Role.PAWN, R.drawable.ic_black_pawn),
        createImageEntry(Army.BLACK, Role.KNIGHT, R.drawable.ic_black_knight),
        createImageEntry(Army.BLACK, Role.BISHOP, R.drawable.ic_black_bishop),
        createImageEntry(Army.BLACK, Role.ROOK, R.drawable.ic_black_rook),
        createImageEntry(Army.BLACK, Role.QUEEN, R.drawable.ic_black_queen),
        createImageEntry(Army.BLACK, Role.KING, R.drawable.ic_black_king),
    )

    init {
        rowCount = side
        columnCount = side
        repeat(side * side) {
            val row = it / side
            val column = it % side
            val tile = Tile(
                ctx,
                if((row + column) % 2 == 0) Type.WHITE else Type.BLACK,
                side,
                piecesImages
            )
            tile.setOnClickListener { onTileClickedListener?.invoke(tile, row, column) }
            tilesList[Position(row, column)] = tile
            addView(tile)
        }
    }

    fun setPiece(position: Position, piece: Piece){
        tilesList[Position(position.x, position.y)]?.piece = piece
    }

    fun removePiece(position: Position){
        tilesList[Position(position.x, position.y)]?.piece = null
    }

    fun setAvailablePositions(positions: List<Position>?) {
        if(positions.isNullOrEmpty()){
            possibleMovesTiles.forEach{ position ->
                tilesList[Position(position.x, position.y)]?.available = false
            }
        } else {
            positions.forEach { position ->
                tilesList[Position(position.x, position.y)]?.available = true
                possibleMovesTiles.add(position)
            }
        }
    }

    fun setChecked(position: Position?) {
        if(position != null){
            tilesList[Position(position.x, position.y)]?.checked = true
            checkedTile = position
        } else {
            tilesList[Position(checkedTile!!.x, checkedTile!!.y)]?.checked = false
            checkedTile = null
        }
    }

    var onTileClickedListener: TileTouchListener? = null

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, brush)
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), brush)
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), brush)
        canvas.drawLine(width.toFloat(), 0f, width.toFloat(), height.toFloat(), brush)
    }
}