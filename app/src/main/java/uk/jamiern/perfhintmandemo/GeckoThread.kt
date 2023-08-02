package uk.jamiern.perfhintmandemo

import android.content.Context
import android.os.HandlerThread
import android.os.PerformanceHintManager
import android.os.Process
import android.util.Log
import android.view.Choreographer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
class GeckoThread(name: String, context: Context) : HandlerThread(name),
    Choreographer.FrameCallback {

    private val perfHintManager = context.getSystemService(PerformanceHintManager::class.java)
    private lateinit var perfHintSession: PerformanceHintManager.Session
    private var frameId = 0
    private var totalDuration: Duration = Duration.ZERO

    @Override
    override fun onLooperPrepared() {
        val tid = Process.myTid()
        Log.d(LOGTAG, "GeckoThread tid: $tid")
        perfHintSession =
            perfHintManager.createHintSession(intArrayOf(tid), target.inWholeNanoseconds)!!

        Choreographer.getInstance().postFrameCallback(this)
    }

    @Override
    override fun doFrame(frameTimeNanos: Long) {
        frameId++

        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE") val primes: List<Int>
        val duration = TimeSource.Monotonic.measureTime {
            primes = calculatePrimes()
        }

        perfHintSession.reportActualWorkDuration(duration.inWholeNanoseconds)
        totalDuration += duration

        if (frameId == 10) {
            Log.d(LOGTAG, "Average duration for $frameId frames: ${totalDuration / frameId}")
            frameId = 0
            totalDuration = Duration.ZERO
        }
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun calculatePrimes(): List<Int> {
        val primes = mutableListOf<Int>()
        for (n in 2..workload) {
            if ((2 until n).none { n % it == 0 }) {
                primes.add(n)
            }
        }
        return primes
    }

    companion object {
        private const val LOGTAG = "GeckoThread"
        private val target = 5.milliseconds
        private const val workload = 3000
    }
}