package com.example.assign1_climbingscoreapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScoreViewModel : ViewModel() {
    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _hold = MutableLiveData(0)
    val hold: LiveData<Int> = _hold

    private val _hasFallen = MutableLiveData(false)
    val hasFallen: LiveData<Boolean> = _hasFallen

    fun climb() {
        if (_hasFallen.value == true) return
        if (_hold.value == 9) return

        _hold.value = _hold.value?.plus(1)
        _score.value = _score.value?.plus(getPointsForHold(_hold.value ?: 0))
    }

    fun fall() {
        if ((_hold.value ?: 0) > 0 && (_hold.value ?: 0) < 9) {
            // Decrease the score by 3 points, ensuring it doesn't go below 0
            _score.value = ((_score.value ?: 0) - 3).coerceAtLeast(0)
            _hasFallen.value = true
        }
    }

    fun reset() {
        _score.value = 0
        _hold.value = 0
        _hasFallen.value = false
    }

    fun restoreState(score: Int, hold: Int, hasFallen: Boolean) {
        _score.value = score
        _hold.value = hold
        _hasFallen.value = hasFallen
    }

    private fun getPointsForHold(hold: Int): Int {
        return when (hold) {
            in 1..3 -> 1
            in 4..6 -> 2
            in 7..9 -> 3
            else -> 0
        }
    }
}