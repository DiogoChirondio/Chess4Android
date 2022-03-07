package pt.isel.pdm.chess4android

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture

class DownloadDailyPuzzleWorker(appContext: Context, workerParams: WorkerParameters)
    : ListenableWorker(appContext, workerParams) {

        override fun startWork(): ListenableFuture<Result> {
            val app : DailyPuzzleApplication = applicationContext as DailyPuzzleApplication
            val repo = PuzzleRepository(app.dailyPuzzleService, app.historyDB.getHistoryPuzzleDao())

            Log.v("APP_TAG", "Thread ${Thread.currentThread().name}: Starting DownloadDailyPuzzleWorker")

            return CallbackToFutureAdapter.getFuture { completer ->
                repo.fetchPuzzleOfDay(mustSaveToDB = true) { result ->
                    result
                        .onSuccess {
                            Log.v("APP_TAG", "Thread ${Thread.currentThread().name}: DownloadDailyPuzzleWorker succeeded")
                            completer.set(Result.success())
                        }
                        .onFailure {
                            Log.v("APP_TAG", "Thread ${Thread.currentThread().name}: DownloadDailyPuzzleWorker failed")
                            completer.setException(it)
                        }
                }
            }
        }
}