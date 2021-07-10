package solonsky.signal.twitter.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.MvpDelegate
import solonsky.signal.twitter.R
import solonsky.signal.twitter.activities.LoggedActivity
import solonsky.signal.twitter.api.ActionsApiFactory
import solonsky.signal.twitter.data.ShareData
import solonsky.signal.twitter.databinding.CellStatusBinding
import solonsky.signal.twitter.dialogs.HashtagDialog
import solonsky.signal.twitter.dialogs.MediaDialog
import solonsky.signal.twitter.dialogs.ProfileDialog
import solonsky.signal.twitter.dialogs.UrlDialog
import solonsky.signal.twitter.helpers.Flags
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.holder.LoaderViewHolder
import solonsky.signal.twitter.libs.ShareContent
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.viewholders.StatusViewHolder
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by neura on 03.11.17.
 */

class MVPStatusAdapter(parentDelegate: MvpDelegate<*>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TAG: String = MVPStatusAdapter::class.simpleName.toString()
    var mContext: WeakReference<Context>? = null
    var mActivity: WeakReference<Activity>? = null
    var mStatuses: ArrayList<StatusModel> = ArrayList()
    var mSources: ArrayList<Any> = ArrayList()
    var mParentDelegate: MvpDelegate<*> = parentDelegate
    var isLoading = false

    val STATUS_TYPE = 0
    val LOADER_TYPE = 1

    interface StatusClickListener {
        fun openActivity(intent: Intent, startAnim: Int, endAnim: Int)
        fun showDialog(type: Flags.Dialogs, title: String, isVideo: Boolean)
        fun shareText(text: String)
        fun shareTextWithApp(text: String, packageName: String, packageActivity: String)
        fun hidePopup()
        fun translate(text: String?)
        fun closeOther(currentStatus: StatusModel?)
        fun closePosition(currentStatus: StatusModel?)
    }

    fun attachContext(context: Context) {
        mContext = WeakReference(context)
    }

    fun attachActivity(activity: Activity) {
        mActivity = WeakReference(activity)
    }

    fun detachContext() {
        mContext = null
    }

    fun detachActivity() {
        mActivity = null
    }

    fun setStatuses(statuses: ArrayList<StatusModel>) {
        mStatuses = statuses
        mSources.clear()
        mSources.addAll(statuses)
        notifyDataSetChanged()
    }

    fun addNewStatuses(newList: ArrayList<StatusModel>) {
        newList.forEach {
            mSources.add(0, it)
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (position == mSources.size - 1 && isLoading) {
            return LOADER_TYPE
        }

        return STATUS_TYPE
    }

    override fun getItemCount(): Int = mSources.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == LOADER_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_footer, parent, false)
            LoaderViewHolder(itemView = view)
        } else {
            val view = inflater.inflate(R.layout.mvp_cell_status, parent, false)
            StatusViewHolder(itemView = view, screenWidth = Utilities.getScreenWidth(mActivity?.get()))
        }
    }

    private var mRecycler: RecyclerView? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecycler = recyclerView
    }

    private var lastPosition = -1
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is StatusViewHolder) {
            mActivity?.let { holder.provideActivity(it) }
            holder.setIsRecyclable(false)
            holder.bind(mSources[position] as StatusModel)
            holder.setClickListener(object : StatusClickListener {
                override fun closePosition(currentStatus: StatusModel?) {
                    lastPosition = -1
                }

                override fun closeOther(currentStatus: StatusModel?) {
                    currentStatus?.let { status ->
                        if (lastPosition != -1) {
                            mRecycler?.let { recyclerView ->
                                val viewHolder: StatusViewHolder = recyclerView.findViewHolderForAdapterPosition(lastPosition)
                                        as StatusViewHolder
                                val status = mSources[lastPosition] as StatusModel
                                viewHolder.changeActions(isExpand = false, isHighlighted = status.isHighlighted)
                            }
                        }

                        lastPosition = if (status.isExpand) {
                            mSources.indexOf(status)
                        } else {
                            -1
                        }
                    }
                }

                override fun translate(text: String?) {
                    ActionsApiFactory.translate(text, mActivity?.get())
                }

                override fun hidePopup() {
                    if (mActivity?.get() is LoggedActivity) {
                        (mActivity?.get() as LoggedActivity).hidePopup()
                    }
                }

                override fun shareTextWithApp(text: String, packageName: String, packageActivity: String) {
                    val shareContent = ShareContent(mActivity?.get())
                    shareContent.shareTextWithApp(text, packageName, packageActivity)
                    hidePopup()
                }

                override fun shareText(text: String) {
                    val shareContent = ShareContent(mActivity?.get())
                    shareContent.shareText(text, "") { componentName ->
                        ShareData.getInstance().addShare(componentName)
                        ShareData.getInstance().saveCache()
                    }
                    hidePopup()
                }

                override fun openActivity(intent: Intent, startAnim: Int, endAnim: Int) {
                    if (mActivity != null) {
                        val strongActivity = mActivity?.get()
                        strongActivity?.startActivity(intent)
                        strongActivity?.overridePendingTransition(startAnim, endAnim)
                    }
                    hidePopup()
                }

                override fun showDialog(type: Flags.Dialogs, title: String, isVideo: Boolean) {
                    if (mActivity != null) {
                        when (type) {
                            Flags.Dialogs.HASH -> HashtagDialog(title.replace("\\s+".toRegex(), ""), mActivity?.get()).show()
                            Flags.Dialogs.USER -> ProfileDialog(title, mActivity?.get()).show()
                            Flags.Dialogs.MEDIA -> MediaDialog(title, mActivity?.get(), isVideo).show()
                            Flags.Dialogs.LINK -> (mSources[position] as StatusModel).urlEntities.forEach {
                                if (it.asJsonObject.get("displayURL").asString == title) {
                                    UrlDialog(it.asJsonObject.get("expandedURL").asString, mActivity?.get()).show()
                                }
                            }
                        }

                        hidePopup()
                    }
                }
            })
        }
    }

    fun addLoader() {
//        isLoading = true
//        mSources.add("")
//        notifyDataSetChanged()
    }

    fun removeLoader() {
//        isLoading = false
//        mSources.remove(mSources.size - 1)
//        notifyItemRemoved(mSources.size - 1)
    }

    fun changeHighlight(isOn: Boolean) {
        mSources.forEach {
            if (it is StatusModel) {
                it.isHighlighted = isOn
            }
        }
        notifyDataSetChanged()
    }

    fun updateStatus(isHighlight: Boolean, position: Int) {
        (mSources[position] as StatusModel).isHighlighted = isHighlight
        notifyItemChanged(position)
    }

}