package uk.jamiern.perfhintmandemo

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.PerformanceHintManager
import android.os.Process
import android.util.Log
import android.view.Choreographer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
class GeckoThread(name: String, context: Context) : HandlerThread(name),
    Choreographer.FrameCallback {

    companion object {
        private const val LOGTAG = "GeckoThread"
    }

    private var workload: Int = 0
    private var phmEnabled = false
    private var budget: Duration = Duration.ZERO

    private val perfHintManager = context.getSystemService(PerformanceHintManager::class.java)
    private var perfHintSession: PerformanceHintManager.Session? = null
    private var frameId = 0
    private var totalDuration: Duration = Duration.ZERO

    fun updateSettings(workload: Int, phmEnabled: Boolean, budget: Duration) {
        Log.d(
            LOGTAG,
            "updateSettings() workload: $workload, PHM enabled: $phmEnabled, budget: $budget"
        )

        this.workload = workload

        if (phmEnabled && !this.phmEnabled) {
            perfHintSession = perfHintManager.createHintSession(
                intArrayOf(Process.myTid()), budget.inWholeNanoseconds
            )!!
        } else if (!phmEnabled && this.phmEnabled) {
            perfHintSession?.close()
            perfHintSession = null
        }
        this.phmEnabled = phmEnabled

        this.budget = budget
        perfHintSession?.updateTargetWorkDuration(budget.inWholeNanoseconds)
    }

    @Override
    override fun onLooperPrepared() {
        Log.d(LOGTAG, "GeckoThread tid: ${Process.myTid()}")
        Choreographer.getInstance().postFrameCallback(this)
    }

    @Override
    override fun doFrame(frameTimeNanos: Long) {
        frameId++

        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE") val primes: List<Int>
        val duration = TimeSource.Monotonic.measureTime {
            primes = calculatePrimes(workload)
        }

        perfHintSession?.reportActualWorkDuration(duration.inWholeNanoseconds)
        totalDuration += duration

        if (frameId == 10) {
            val averageDuration = totalDuration / frameId
            Log.d(LOGTAG, "Average duration for $frameId frames: $averageDuration")
            frameId = 0
            totalDuration = Duration.ZERO

            val handler = Handler(Looper.getMainLooper())
            handler.post {
                MainActivity.result = averageDuration.toString()
            }
        }
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun calculatePrimes(workload: Int): List<Int> {
        val primes = mutableListOf<Int>()
        for (n in 2..workload) {
            if ((2 until n).none { n % it == 0 }) {
                primes.add(n)
            }
        }
        return primes
    }
}