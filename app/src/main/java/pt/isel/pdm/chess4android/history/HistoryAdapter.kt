package pt.isel.pdm.chess4android.history

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.provider.Settings.System.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import pt.isel.pdm.chess4android.PuzzleGame
import pt.isel.pdm.chess4android.PuzzleGameDto
import pt.isel.pdm.chess4android.R
import java.util.*

class HistoryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val puzzleIdView = itemView.findViewById<TextView>(R.id.puzzle_id)
    private val dateView = itemView.findViewById<TextView>(R.id.date)

    fun bindTo(puzzleGame: PuzzleGameDto, onItemClick: () -> Unit) {
        val context = itemView.context
        val solvedMessage = if(puzzleGame.solved){
            context.getString(R.string.solved_message)
        } else {
            context.getString(R.string.not_solved_message)
        }
        puzzleIdView.text = solvedMessage + " " + puzzleGame.puzzleGame.game.id

        dateView.text = Date(puzzleGame.date).toString()

        itemView.setOnClickListener {
            itemView.isClickable = false
            startAnimation {
                onItemClick()
                itemView.isClickable = true
            }
        }
    }

    private fun startAnimation(onAnimationEnd: () -> Unit) {

        val animation = ValueAnimator.ofArgb(
            ContextCompat.getColor(itemView.context, R.color.list_item_background),
            ContextCompat.getColor(itemView.context, R.color.list_item_background_selected),
            ContextCompat.getColor(itemView.context, R.color.list_item_background)
        )

        animation.addUpdateListener { animator ->
            val background = itemView.background as GradientDrawable
            background.setColor(animator.animatedValue as Int)
        }

        animation.duration = 400
        animation.doOnEnd { onAnimationEnd() }

        animation.start()
    }
}

class HistoryAdapter(
    private val dataSource: List<PuzzleGameDto>,
    private val onItemClick: (PuzzleGameDto, Boolean) -> Unit
): RecyclerView.Adapter<HistoryItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_view, parent, false)
        return HistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bindTo(dataSource[position]) {
            onItemClick(dataSource[position], dataSource[position].solved)
        }
    }

    override fun getItemCount() = dataSource.size
}