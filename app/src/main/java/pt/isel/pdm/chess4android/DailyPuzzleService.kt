package pt.isel.pdm.chess4android

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import retrofit2.Call
import retrofit2.http.GET
import java.lang.Exception
import java.util.*

@Parcelize
data class UserID(val userId: String, val name: String, val color: String) : Parcelable

@Parcelize
data class Perf(val icon: String, val name: String) : Parcelable

@Parcelize
data class Puzzle(val id: String, val rating: Int, val plays: Int, val initialPly: Int, val solution: MutableList<String>, val themes: List<String>) : Parcelable

@Parcelize
data class Game(val id: String, val perf: Perf, val rated: Boolean, val players: List<UserID>, val pgn: String, val clock: String) : Parcelable

@Parcelize
data class PuzzleGame(val game: Game, val puzzle: Puzzle) : Parcelable

@Parcelize
data class PuzzleGameDto(val date: Long, var solved: Boolean, val puzzleGame: PuzzleGame) : Parcelable

interface DailyPuzzleService {
    @GET("puzzle/daily")
    fun getPuzzle(): Call<PuzzleGame>
}

class ServiceUnavailable(message: String = "", cause: Throwable? = null): Exception(message)