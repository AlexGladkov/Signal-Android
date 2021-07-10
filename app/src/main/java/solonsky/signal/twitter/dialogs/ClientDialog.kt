package solonsky.signal.twitter.dialogs

import android.app.Activity
import android.content.DialogInterface
import android.os.Handler
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import solonsky.signal.twitter.R
import solonsky.signal.twitter.data.MuteData
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.models.RemoveModel

/**
 * Created by neura on 28.01.18.
 */
class ClientDialog(val mActivity: Activity, val clientName: String) {
    private final val TAG: String = ClientDialog::class.java.simpleName
    private var mDialog: MaterialDialog? = null

    init {
        val customView = if (App.getInstance().isNightEnabled)
            R.layout.dialog_dark_client
        else
            R.layout.dialog_light_client
        mDialog = MaterialDialog.Builder(mActivity)
                .customView(customView, false)
                .dismissListener(DialogInterface.OnDismissListener { }).build()

        mDialog?.let {
            it.window?.attributes?.windowAnimations = R.style.actionSheetAnimation
            val lp = it.window!!.attributes
            lp.alpha = 1.0f
            it.window?.attributes = lp

            val view = it.view
            val title = view.findViewById<View>(R.id.dialog_client_title) as TextView
            title.text = clientName

            view.findViewById<View>(R.id.dialog_client_mute).setOnClickListener {
                val handler = Handler()
                Thread(Runnable {
                    if (!MuteData.getInstance().isCacheLoaded) {
                        MuteData.getInstance().loadCache()
                    }

                    val removeModel = RemoveModel(0, clientName)
                    if (!MuteData.getInstance().getmClientsList().contains(removeModel))
                        MuteData.getInstance().getmClientsList().add(0, removeModel)

                    MuteData.getInstance().saveCache()
                    handler.post {
                        Toast.makeText(mActivity, mActivity.getString(R.string.success_mute_client)
                                .replace("[clientname]", clientName), Toast.LENGTH_SHORT).show()
                    }
                }).start()
                mDialog?.dismiss()
            }
        }
    }

    fun show() {
        mDialog?.show()
    }
}