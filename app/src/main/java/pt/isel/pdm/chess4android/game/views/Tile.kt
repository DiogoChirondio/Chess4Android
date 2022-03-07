package pt.isel.pdm.chess4android.game.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import pt.isel.pdm.chess4android.*
import pt.isel.pdm.chess4android.game.model.Army
import pt.isel.pdm.chess4android.game.model.Piece
import pt.isel.pdm.chess4android.game.model.Role
import pt.isel.pdm.chess4android.game.model.Type

/**
 * Custom view that implements a chess board tile.
 * Tiles are either black or white and can they can be empty or occupied by a chess piece.
 *
 * Implementation note: This view is not to be used with the designer tool.
 * You need to adapt this view to suit your needs. ;)
 *
 * @property type           The tile's type (i.e. black or white)
 * @property tilesPerSide   The number of tiles in each side of the chess board
 */
@SuppressLint("ViewConstructor")
class Tile(
    private val ctx: Context,
    private val type: Type,
    private val tilesPerSide: Int,
    private val images: Map<Pair<Army, Role>, VectorDrawableCompat?>,
    initialPiece: Piece? = null,
    availableMove: Boolean = false
) : View(ctx) {

    var piece: Piece? = initialPiece
        set(value) {
            field = value
            invalidate()
        }

    var available: Boolean = availableMove
        set(value) {
            field = value
            invalidate()
        }

    var checked: Boolean = availableMove
        set(value) {
            field = value
            invalidate()
        }

    private fun selectColor(): Int {
        if (type == Type.WHITE)
            return R.color.chess_board_white
        else {
            return R.color.chess_board_black
        }
    }

    private val brushForCheck = Paint().apply {
        color = ctx.resources.getColor(
            R.color.chess_board_red,
            null
        )
        style = Paint.Style.FILL_AND_STROKE
    }

    private val brushForPlays = Paint().apply {
        color = ctx.resources.getColor(
            R.color.chess_board_green,
            null
        )
        style = Paint.Style.FILL_AND_STROKE
    }

    private val brushForBoard = Paint().apply {
        color = ctx.resources.getColor(
            selectColor(),
            null
        )
        style = Paint.Style.FILL_AND_STROKE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val side = Integer.min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(side / tilesPerSide, side / tilesPerSide)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), selectBrush())
        if (piece != null) {
            images[piece?.getPair()]?.apply {
                val padding = 8
                setBounds(padding, padding, width-padding, height-padding)
                draw(canvas)
            }
        }
    }

    private fun selectBrush(): Paint {
        if(available){
            return brushForPlays
        } else if(checked){
            return brushForCheck
        } else {
            return brushForBoard
        }
    }
}