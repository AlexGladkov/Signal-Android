package solonsky.signal.twitter.presenters

import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import moxy.InjectViewState
import moxy.MvpPresenter
import solonsky.signal.twitter.views.MediaView

/**
 * Created by neura on 30.10.17.
 */
@InjectViewState
class MediaPresenter : MvpPresenter<MediaView>() {
    val TAG: String = MediaPresenter::class.java.simpleName
    var startCount = 0
    var isClicking = false
    var isFlushing = true
    var isTrackTouched = false
    var isOverlay = true
    var duration: Int = 0

    /**
     * Performs play functional
     */
    fun play() {
        viewState.onPlay()
        isClicking = true

        click()
    }

    /**
     * Performs pause
     */
    fun pause() {
        viewState.onSuspend()
        isClicking = false
    }

    fun stop() {
        isClicking = false
        startCount = duration / 1000

        viewState.onUpdateBar(getProgress())
        viewState.onSuspend()
        viewState.onUpdateProgress(getTimeStamp())
    }

    /**
     * Performs forward
     */
    fun forward() {
        if ((startCount + 5) > (duration / 1000)) {
            startCount = (duration / 1000)
        } else {
            startCount += 5
        }

        viewState.onUpdateProgress(getTimeStamp())
        viewState.onSeekTo(startCount * 1000)
        viewState.onUpdateBar(getProgress())
    }

    /**
     * Performs rewind
     */
    fun rewind() {
        if ((startCount - 5) > 0) {
            startCount -= 5
        } else {
            startCount = 0
        }

        viewState.onUpdateProgress(getTimeStamp())
        viewState.onSeekTo(startCount * 1000)
        viewState.onUpdateBar(getProgress())
    }

    fun contentClick() {
        this.isOverlay = !isOverlay
        if (isOverlay) {
            viewState.showOverlay()
        } else {
            viewState.hideOverlay()
        }
    }

    fun onProgressChanged(progress: Int) {
        if (isTrackTouched) {
            startCount = getPosition(progress)

            viewState.onUpdateProgress(getTimeStamp())
            viewState.onSeekTo(startCount * 1000)
            viewState.onUpdateBar(getProgress())
        }
    }

    fun onStartTrackingTouch() {
        isTrackTouched = true
    }

    fun onStopTrackingTouch() {
        isTrackTouched = false
    }

    fun prepareVideo(duration: Int) {
        val millis = duration / 1000
        val seconds = millis.mod(60)
        val minutes = millis / 60
        val end = (if (minutes < 10) "0" + minutes.toString() else minutes.toString()) + ":" + (if (seconds < 10) "0" + seconds.toString() else seconds.toString())

        this.duration = duration
        viewState.onUpdateProgress("00:00")
        viewState.onUpdateEnd(end)
    }

    private fun click() {
        val handler = Handler()
        if (isFlushing) {
            isFlushing = false
            Thread(Runnable {
                Thread.sleep(1000)

                if ((startCount + 1) < (duration / 1000)) {
                    startCount += 1
                }

                handler.post {
                    if (!isTrackTouched) {
                        viewState.onUpdateProgress(getTimeStamp())
                        viewState.onUpdateBar(getProgress())
                    }

                    isFlushing = true
                    if (isClicking) click()
                }
            }).start()
        }
    }

    private fun getPosition(progress: Int): Int =
            ((progress.toFloat() / 100f) * (duration / 1000)).toInt()

    private fun getProgress(): Int =
            ((startCount.toFloat() / (duration / 1000).toFloat()) * 100f).toInt()

    private fun getTimeStamp(): String {
        val minutes = startCount / 60
        val seconds = startCount.mod(60)
        val minutesString = if (minutes < 10) ("0" + minutes.toString()) else minutes.toString()
        val secondsString = if (seconds < 10) ("0" + seconds.toString()) else seconds.toString()

        return minutesString + ":" + secondsString
    }
}