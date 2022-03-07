package pt.isel.pdm.chess4android.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.chess4android.game.model.Piece
import pt.isel.pdm.chess4android.game.model.ChessInfo
import pt.isel.pdm.chess4android.game.model.Position

class PuzzleActivityViewModel(
    application: Application
): AndroidViewModel(application) {

    private val _boardView: MutableLiveData<HashMap<Position, Piece>> = MutableLiveData()
    val boardView: LiveData<HashMap<Position, Piece>> = _boardView

    fun showIntendedPuzzle(pgn: String?) {
        _boardView.value = ChessInfo.getDailyPattern(pgn)
    }
}