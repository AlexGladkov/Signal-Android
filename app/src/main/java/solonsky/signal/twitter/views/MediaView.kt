package solonsky.signal.twitter.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType


/**
 * Created by neura on 30.10.17.
 */

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface MediaView : MvpView {
    fun onUpdateProgress(start: String)
    fun onUpdateEnd(end: String)
    fun onUpdateBar(value: Int)
    fun onSeekTo(mSec: Int)
    fun onPlay()
    fun onSuspend()
    fun showOverlay()
    fun hideOverlay()
}