package solonsky.signal.twitter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import moxy.MvpDelegate
import org.jetbrains.annotations.Nullable


/**
 * Created by neura on 05.11.17.
 */
abstract class MvpViewHolder(private val mParentDelegate: MvpDelegate<*>, itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var mMvpDelegate: MvpDelegate<*>? = null
    private val mvpDelegate: MvpDelegate<*>?
        @Nullable
        get() {
            if (mvpChildId == null) {
                return null
            }
            if (mMvpDelegate == null) {
                mMvpDelegate = MvpDelegate(this)
                mMvpDelegate!!.setParentDelegate(mParentDelegate, mvpChildId)
            }
            return mMvpDelegate
        }

    protected abstract val mvpChildId: String?

    init {

    }

    protected fun destroyMvpDelegate() {
        if (mvpDelegate != null) {
            mvpDelegate!!.onSaveInstanceState()
            mvpDelegate!!.onDetach()
            mMvpDelegate = null
        }
    }

    protected fun createMvpDelegate() {
        if (mvpDelegate != null) {
            mvpDelegate!!.onCreate()
            mvpDelegate!!.onAttach()
        }
    }
}