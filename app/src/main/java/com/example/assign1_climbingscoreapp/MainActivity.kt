package com.example.assign1_climbingscoreapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import kotlin.text.*

class MainActivity : AppCompatActivity() {
    private val viewModel: ScoreViewModel by viewModels()
    private var startTime = 0L
    private var elapsedTime = 0L
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val scoreTextView = findViewById<TextView>(R.id.textScore)
        val holdTextView = findViewById<TextView>(R.id.textHold)
        val timeTextView = findViewById<TextView>(R.id.textTime)
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)

        viewModel.score.observe(this, Observer { score ->
            scoreTextView.text = "Score: $score"
            updateScoreColor(scoreTextView, viewModel.hold.value ?: 0)
            updateBackgroundColor(mainLayout, viewModel.hold.value ?: 0)
        })

        viewModel.hold.observe(this, Observer { hold ->
            holdTextView.text = "Hold: $hold"
            updateScoreColor(scoreTextView, hold) // Update Score color
            updateScoreColor(holdTextView, hold) // Update Hold color
            updateScoreColor(timeTextView, hold) // Update Time color
            updateBackgroundColor(mainLayout, hold)
        })

        findViewById<Button>(R.id.buttonClimb).setOnClickListener {
            viewModel.climb()
            startTimer(timeTextView)
        }

        findViewById<Button>(R.id.buttonFall).setOnClickListener {
            viewModel.fall()
            stopTimer()
        }

        findViewById<Button>(R.id.buttonReset).setOnClickListener {
            viewModel.reset()
            resetTimer(timeTextView)
        }

        if (savedInstanceState != null) {
            // Restore ViewModel state
            viewModel.restoreState(
                savedInstanceState.getInt("score", 0),
                savedInstanceState.getInt("hold", 0),
                savedInstanceState.getBoolean("hasFallen", false)
            )

            // Restore timer state
            elapsedTime = savedInstanceState.getLong("elapsedTime", 0L)
            isTimerRunning = savedInstanceState.getBoolean("isTimerRunning", false)
            if (isTimerRunning) {
                startTimer(timeTextView, elapsedTime)
            } else {
                updateTimerText(timeTextView, elapsedTime)
            }
        }
    }

    private fun startTimer(timeTextView: TextView, initialElapsedTime: Long = 0L) {
        startTime = System.currentTimeMillis() - initialElapsedTime
        isTimerRunning = true
        runnable = object : Runnable {
            override fun run() {
                elapsedTime = System.currentTimeMillis() - startTime
                updateTimerText(timeTextView, elapsedTime)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
        isTimerRunning = false
    }

    private fun resetTimer(timeTextView: TextView) {
        stopTimer() // Stop timer if running
        elapsedTime = 0L // Reset elapsed time to 0
        startTime = 0L // Reset start time to 0
        updateTimerText(timeTextView, elapsedTime) // Ensure time shows 00:00
    }

    private fun updateTimerText(timeTextView: TextView, elapsedTime: Long) {
        val totalSeconds = elapsedTime / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        timeTextView.text = String.format("Time: %02d:%02d", minutes, seconds)
    }



    private fun updateScoreColor(scoreTextView: TextView, hold: Int) {
        val color = when {
            hold in 1..3 -> ContextCompat.getColor(this, R.color.blue)
            hold in 4..6 -> ContextCompat.getColor(this, R.color.green)
            hold in 7..9 -> ContextCompat.getColor(this, R.color.red)
            else -> ContextCompat.getColor(this, R.color.black)
        }
        scoreTextView.setTextColor(color)
        Log.d("MainActivity", "Updated score: score=${viewModel.score.value}, hold=$hold, color=$color")
    }

    private fun updateBackgroundColor(mainLayout: ConstraintLayout, hold: Int) {
        val color = when {
            hold in 1..3 -> ContextCompat.getColor(this, R.color.blue_background)
            hold in 4..6 -> ContextCompat.getColor(this, R.color.green_background)
            hold in 7..9 -> ContextCompat.getColor(this, R.color.red_background)
            else -> ContextCompat.getDrawable(this, R.drawable.climbing)
        }

        Log.d("MainActivity", "Updated background color: hold=$hold, color=$color")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save timer state
        outState.putLong("elapsedTime", elapsedTime)
        outState.putBoolean("isTimerRunning", isTimerRunning)

        // Save ViewModel state
        outState.putInt("score", viewModel.score.value ?: 0)
        outState.putInt("hold", viewModel.hold.value ?: 0)
        outState.putBoolean("hasFallen", viewModel.hasFallen.value ?: false)
    }
}