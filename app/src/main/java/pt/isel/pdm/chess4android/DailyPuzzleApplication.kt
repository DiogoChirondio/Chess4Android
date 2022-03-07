package pt.isel.pdm.chess4android

import android.app.Application
import androidx.room.Room
import androidx.work.*
import com.google.gson.Gson
import pt.isel.pdm.chess4android.challenges.ChallengesRepository
import pt.isel.pdm.chess4android.game.GamesRepository
import pt.isel.pdm.chess4android.history.HistoryDatabase
import pt.isel.pdm.chess4android.history.PuzzleEntity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class DailyPuzzleApplication : Application() {

    val dailyPuzzleService: DailyPuzzleService by lazy{
        Retrofit.Builder()
            .baseUrl("https://lichess.org/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DailyPuzzleService::class.java)
    }

    val historyDB: HistoryDatabase by lazy {
        Room
            .databaseBuilder(this, HistoryDatabase::class.java, "history_db")
            .build()
    }

    private val mapper: Gson by lazy { Gson() }

    /**
     * The challenges' repository
     */
    val challengesRepository: ChallengesRepository by lazy { ChallengesRepository() }

    /**
     * The games' repository
     */
    val gamesRepository: GamesRepository by lazy { GamesRepository(mapper) }

    override fun onCreate() {
        super.onCreate()
        val workRequest = PeriodicWorkRequestBuilder<DownloadDailyPuzzleWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            .build()

        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                "DownloadDailyPuzzle",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
}