package pt.isel.pdm.chess4android.daily

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    private val appTag = "CHESS_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(appTag, "onCreate() on $localClassName")
    }

    override fun onStart() {
        super.onStart()
        Log.v(appTag, "onStart() on $localClassName")
    }

    override fun onResume() {
        super.onResume()
        Log.v(appTag, "onResume() on $localClassName")
    }

    override fun onPause() {
        super.onPause()
        Log.v(appTag, "onPause() on $localClassName")
    }

    override fun onStop() {
        super.onStop()
        Log.v(appTag, "onStop() on $localClassName")
    }

    override fun onRestart() {
        super.onRestart()
        Log.v(appTag, "onRestart() on $localClassName")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(appTag, "onDestroy() on $localClassName")
    }
}