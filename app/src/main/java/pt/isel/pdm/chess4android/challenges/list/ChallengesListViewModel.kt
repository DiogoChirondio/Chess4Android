package pt.isel.pdm.chess4android.challenges.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.chess4android.DailyPuzzleApplication
import pt.isel.pdm.chess4android.challenges.ChallengeInfo
import pt.isel.pdm.chess4android.game.GameState

class ChallengesListViewModel(app: Application) : AndroidViewModel(app) {

    private val app = getApplication<DailyPuzzleApplication>()

    /**
     * Contains the result of the last attempt to fetch the challenges list
     */
    private val _challenges: MutableLiveData<Result<List<ChallengeInfo>>> = MutableLiveData()
    val challenges: LiveData<Result<List<ChallengeInfo>>> = _challenges

    /**
     * Gets the challenges list by fetching them from the server. The operation's result is exposed
     * through [challenges]
     */
    fun fetchChallenges() =
        app.challengesRepository.fetchChallenges(onComplete = {
            _challenges.value = it
        })

    /**
     * Contains information about the enrolment in a game.
     */
    private val _enrolmentResult: MutableLiveData<Result<Pair<ChallengeInfo, GameState>>?> = MutableLiveData()
    val enrolmentResult: LiveData<Result<Pair<ChallengeInfo, GameState>>?> = _enrolmentResult

    /**
     * Tries to accept the given challenge. The result of the asynchronous operation is exposed
     * through [enrolmentResult] LiveData instance.
     */
    fun tryAcceptChallenge(challengeInfo: ChallengeInfo) {
        val app = getApplication<DailyPuzzleApplication>()
        Log.v("TAG", "Challenge accepted. Signalling by removing challenge from list")
        app.challengesRepository.withdrawChallenge(
            challengeId = challengeInfo.id,
            onComplete = {
                it.onSuccess {
                    Log.v("TAG", "We successfully unpublished the challenge. Let's start the game")
                    app.gamesRepository.createGame(challengeInfo, onComplete = { game ->
                        _enrolmentResult.value = game
                    })
                }
            }
        )
    }
}