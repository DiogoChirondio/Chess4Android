package pt.isel.pdm.chess4android

import pt.isel.pdm.chess4android.history.HistoryPuzzleDao
import pt.isel.pdm.chess4android.history.PuzzleEntity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private fun PuzzleGame.toPuzzleEntity() = PuzzleEntity(
    id = this.game.id,
    png = this.game.pgn,
    sol = this.puzzle.solution.toString().substring(1, this.puzzle.solution.toString().length - 1).replace(" ", ""),
    solved = false
)

private fun PuzzleEntity.toPuzzleGame() = PuzzleGame(
    game = Game(this.id, Perf("", ""), false, mutableListOf(), this.png, ""),
    puzzle = Puzzle("", 0, 0, 0, this.sol.split(',').toMutableList(), mutableListOf())
)

class PuzzleRepository(
    private val puzzleOfDayService: DailyPuzzleService,
    private val historyPuzzleDao: HistoryPuzzleDao
) {

    /**
     * Asynchronously gets the daily puzzle from the local DB, if available.
     * @param callback the function to be called to signal the completion of the
     * asynchronous operation, which is called in the MAIN THREAD.
     */
    private fun asyncMaybeGetTodayPuzzleFromDB(callback: (Result<PuzzleEntity?>) -> Unit) {
        callbackAfterAsync(callback) {
            historyPuzzleDao.getLast(1).firstOrNull()
        }
    }

    /**
     * Asynchronously gets the daily puzzle from the remote API.
     * @param callback the function to be called to signal the completion of the
     * asynchronous operation, which is called in the MAIN THREAD.
     */
    private fun asyncGetTodayPuzzleFromAPI(callback: (Result<PuzzleGame>) -> Unit) {
        puzzleOfDayService.getPuzzle().enqueue(
            object: Callback<PuzzleGame> {
                override fun onResponse(call: Call<PuzzleGame>, response: Response<PuzzleGame>) {
                    val dailyPuzzle: PuzzleGame? = response.body()
                    val result =
                        if (dailyPuzzle != null && response.isSuccessful)
                            Result.success(dailyPuzzle)
                        else
                            Result.failure(ServiceUnavailable())
                    callback(result)
                }

                override fun onFailure(call: Call<PuzzleGame>, error: Throwable) {
                    callback(Result.failure(ServiceUnavailable(cause = error)))
                }
            })
    }

    /**
     * Asynchronously saves the daily puzzle to the local DB.
     * @param callback the function to be called to signal the completion of the
     * asynchronous operation, which is called in the MAIN THREAD.
     */
    private fun asyncSaveToDB(dto: PuzzleGame, callback: (Result<Unit>) -> Unit = { }) {
        callbackAfterAsync(callback) {
            historyPuzzleDao.insert(dto.toPuzzleEntity())
        }
    }

    /**
     * Asynchronously gets the puzzle of day, either from the local DB, if available, or from
     * the remote API.
     *
     * @param mustSaveToDB  indicates if the operation is only considered successful if all its
     * steps, including saving to the local DB, succeed. If false, the operation is considered
     * successful regardless of the success of saving the puzzle in the local DB (the last step).
     * @param callback the function to be called to signal the completion of the
     * asynchronous operation, which is called in the MAIN THREAD
     *
     * Using a boolean to distinguish between both options is a questionable design decision.
     */
    fun fetchPuzzleOfDay(mustSaveToDB: Boolean = false, callback: (Result<PuzzleGame>) -> Unit) {
        asyncMaybeGetTodayPuzzleFromDB { maybeEntity ->
            val maybePuzzle = maybeEntity.getOrNull()
            if (maybePuzzle?.isTodayPuzzle() == true) {
                callback(Result.success(maybePuzzle.toPuzzleGame()))
            }
            else {
                asyncGetTodayPuzzleFromAPI { apiResult ->
                    apiResult.onSuccess { puzzleDto ->
                        asyncSaveToDB(puzzleDto) { saveToDBResult ->
                            saveToDBResult.onSuccess {
                                callback(Result.success(puzzleDto))
                            }
                            .onFailure {
                                callback(if(mustSaveToDB) Result.failure(it) else Result.success(puzzleDto))
                            }
                        }
                    }
                    callback(apiResult)
                }
            }
        }
    }

    fun asyncUpdateDB(id: String, callback: (Result<Unit>) -> Unit = { }) {
        callbackAfterAsync(callback) {
            historyPuzzleDao.updateSolvedState(id)
        }
    }
}