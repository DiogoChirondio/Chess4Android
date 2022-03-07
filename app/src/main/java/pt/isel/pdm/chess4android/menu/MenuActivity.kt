package pt.isel.pdm.chess4android.menu

import android.content.Intent
import android.os.Bundle
import pt.isel.pdm.chess4android.daily.BaseActivity
import pt.isel.pdm.chess4android.about.AboutActivity
import pt.isel.pdm.chess4android.challenges.list.ChallengesListActivity
import pt.isel.pdm.chess4android.daily.MainActivity
import pt.isel.pdm.chess4android.databinding.ActivityMenuBinding
import pt.isel.pdm.chess4android.history.HistoryActivity


class MenuActivity : BaseActivity() {
    private val binding by lazy {
        ActivityMenuBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.dailyPuzzle.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.history.setOnClickListener{
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.aboutText.setOnClickListener{
            startActivity(Intent(this, AboutActivity::class.java))
        }
        binding.online.setOnClickListener {
            startActivity(Intent(this, ChallengesListActivity::class.java))
        }
    }
}