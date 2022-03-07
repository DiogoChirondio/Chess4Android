package pt.isel.pdm.chess4android.history

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import pt.isel.pdm.chess4android.*
import pt.isel.pdm.chess4android.daily.MainActivity.Companion.buildIntentMain
import pt.isel.pdm.chess4android.databinding.ActivityHistoryBinding
import pt.isel.pdm.chess4android.history.PuzzleActivity.Companion.buildIntentPuzzle

class HistoryActivity : AppCompatActivity() {

    private val binding by lazy { ActivityHistoryBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<HistoryActivityViewModel>()

    override fun onStart() {
        super.onStart()

        setContentView(binding.root)
        binding.puzzleList.layoutManager = LinearLayoutManager(this)

        viewModel.loadHistory().observe(this){
            binding.puzzleList.adapter = HistoryAdapter(it) { puzzleDto, solved ->
                if(solved) startActivity(buildIntentPuzzle(this, puzzleDto))
                else startActivity(buildIntentMain(this, puzzleDto))
            }
        }
    }
}