package pt.isel.pdm.chess4android.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.chess4android.*

private fun PuzzleEntity.toPuzzleGameDto() = PuzzleGameDto(
    date = this.timestamp.time,
    solved = this.solved,
    puzzleGame = PuzzleGame(
        game = Game(this.id, Perf("", ""), false, mutableListOf(), this.png, ""),
        puzzle = Puzzle("", 0, 0, 0, this.sol.split(',').toMutableList(), mutableListOf())
    )
)

class HistoryActivityViewModel(application: Application): AndroidViewModel(application){

    var history: LiveData<List<PuzzleGameDto>>? = null
        private set

    private val historyDao: HistoryPuzzleDao by lazy {
        getApplication<DailyPuzzleApplication>().historyDB.getHistoryPuzzleDao()
    }

    fun loadHistory(): LiveData<List<PuzzleGameDto>>{
        val publish = MutableLiveData<List<PuzzleGameDto>>()
        history = publish
        callbackAfterAsync(
            asyncAction = {
                historyDao.getAll().map {
                    it.toPuzzleGameDto()
                }
            },
            callback = { result ->
                result.onSuccess { publish.value = it }
                result.onFailure { publish.value = emptyList() }
            }
        )
        return publish
    }
}