package solonsky.signal.twitter.activities

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

import java.util.ArrayList

import solonsky.signal.twitter.R
import solonsky.signal.twitter.adapters.UserDetailAdapter
import solonsky.signal.twitter.api.DirectApi
import solonsky.signal.twitter.data.UsersData
import solonsky.signal.twitter.databinding.ActivityChatSelectBinding
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.models.UserModel
import solonsky.signal.twitter.room.converters.UsersConverterImpl
import solonsky.signal.twitter.viewmodels.ChatSelectViewModel

/**
 * Created by neura on 23.05.17.
 */

class ChatSelectActivity : AppCompatActivity() {
    private val TAG = ChatSelectActivity::class.java.simpleName
    private var viewModel: ChatSelectViewModel? = null
    private var mUsersList: MutableList<UserModel> = ArrayList()
    private var mAdapter: UserDetailAdapter? = null
    private val converter = UsersConverterImpl()

    private val searchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            filterData(s.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (App.getInstance().isNightEnabled) {
            setTheme(R.style.ActivityThemeDarkNoAnimation)
        }

        val binding = DataBindingUtil.setContentView<ActivityChatSelectBinding>(this, R.layout.activity_chat_select)
        setSupportActionBar(binding.tbChatSelect)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor =  resources.getColor(if (App.getInstance().isNightEnabled)
            R.color.dark_status_bar_timeline_color
        else
            R.color.light_status_bar_timeline_color)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            val back = resources.getDrawable(R.drawable.ic_icons_toolbar_back)
            back.setColorFilter(resources.getColor(if (App.getInstance().isNightEnabled)
                R.color.dark_tint_color
            else
                R.color.light_tint_color), PorterDuff.Mode.SRC_ATOP)
            supportActionBar!!.setHomeAsUpIndicator(back)
        }

        //        for (solonsky.signal.twitter.models.User user : UsersData.getInstance().getUsersList()) {
        //            UserModel userModel = new UserModel(user.getId(), user.getBiggerProfileImageURL(),
        //                    user.getName(), "@" + user.getScreenName(), true, false, false);
        //            mUsersList.add(userModel);
        //        }



        binding.txtChatSelect.addTextChangedListener(searchTextWatcher)
        mAdapter = UserDetailAdapter(mUsersList, applicationContext, this, UserDetailAdapter.UserClickHandler { model, v ->
            AppData.DM_SELECTED_USER = model.username
            AppData.DM_OTHER_ID = model.id
            DirectApi.getInstance().clear()
            DirectApi.getInstance().userId = model.id
            DirectApi.getInstance().screenName = model.twitterName.replace("@", "")
            startActivity(Intent(applicationContext, ChatActivity::class.java))
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation)
            finish()
        })
        viewModel = ChatSelectViewModel(mAdapter, applicationContext)
        binding.model = viewModel
        filterData(query = "")
    }

    private fun filterData(query: String) {
        // FIX THIS!!!! NEED TO SEND IT TO PRESENTER AND CHANGE AFTER DATA WILL BE INSERT INSIDE ADAPTER
        // THIS IS BAD DECISION AND NEED TO TEMPORARY FIX
        val handler = Handler()
        Thread({
            mUsersList.clear()
            mUsersList.addAll(App.db.usersDao().all
                    .filter { it.name.toLowerCase().contains(query.toLowerCase()) }
                    .map { converter.dbToModel(it) })
            handler.post {
                mAdapter?.notifyDataSetChanged()
            }
        }).start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()

            else -> {
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        Utilities.hideKeyboard(this)
        finish()
        overridePendingTransition(R.anim.slide_out_no_animation, R.anim.fade_out)
    }
}
