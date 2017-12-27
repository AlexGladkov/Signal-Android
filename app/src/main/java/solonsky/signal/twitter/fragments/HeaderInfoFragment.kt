package solonsky.signal.twitter.fragments

import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver

import solonsky.signal.twitter.R
import solonsky.signal.twitter.activities.MVPProfileActivity
import solonsky.signal.twitter.activities.SearchActivity
import solonsky.signal.twitter.databinding.FragmentProfileInfoBinding
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Flags
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkMode
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkOnClickListener
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.viewmodels.ProfileInfoViewModel
import solonsky.signal.twitter.views.ProfileView

/**
 * Created by neura (Alex Gladkov) on 27.05.17.
 *
 *
 * Sub fragment for view pager in Profiles
 * Describes user bio, location, url
 *
 * @link res/layout/fragment_profile_info.xml
 * @see ProfileActivity
 *
 * @see ProfileFragment
 */
class HeaderInfoFragment : Fragment() {
    private val TAG = HeaderInfoFragment::class.java.simpleName
    private lateinit var viewModel: ProfileInfoViewModel
    var height = 0
        private set
    private var profileListener: ProfileView? = null
    private var additionalHeight: Int = 0
    private var estimatedHeight: Int = 0
    private lateinit var binding: FragmentProfileInfoBinding

    companion object {
        fun newInstance(user: User): HeaderInfoFragment {
            val fragment = HeaderInfoFragment()
            val args = Bundle()
            args.putParcelable(User.TAG, user)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_profile_info, container, false)

        if (arguments != null && arguments.get(User.TAG) != null) {
            val user = arguments.get(User.TAG) as User

            var realDescription = user.description
            if (user.descriptionUrlEntities != null) {
                user.descriptionUrlEntities.forEach({
                    realDescription = realDescription.replace(
                            it.asJsonObject.get("url").asString,
                            it.asJsonObject.get("expandedURL").asString)
                })
            }

            if (TextUtils.isEmpty(realDescription)) {
                realDescription = getString(R.string.no_bio)
                binding.txtProfileInfoBio.setTypeface(binding.txtProfileInfoBio.typeface, Typeface.ITALIC)
                binding.txtProfileInfoBio.setTextColor(resources.getColor(R.color.dark_hint_text_color))
            }

            val link = user.urlEntity?.get("displayURL")?.asString

            viewModel = ProfileInfoViewModel(realDescription, link, user.location)

            binding.model = viewModel
            binding.txtProfileInfoBio.addAutoLinkMode(
                    AutoLinkMode.MODE_MENTION,
                    AutoLinkMode.MODE_URL,
                    AutoLinkMode.MODE_HASHTAG
            )

            val isNight = App.getInstance().isNightEnabled

            binding.txtProfileInfoBio.setHashtagModeColor(ContextCompat.getColor(context, if (isNight)
                R.color.dark_tag_color
            else
                R.color.light_tag_color))
            binding.txtProfileInfoBio.setMentionModeColor(ContextCompat.getColor(context, if (isNight)
                R.color.dark_highlight_color
            else
                R.color.light_highlight_color))
            binding.txtProfileInfoBio.setUrlModeColor(ContextCompat.getColor(context, if (isNight)
                R.color.dark_highlight_color
            else
                R.color.light_highlight_color))
            binding.txtProfileInfoBio.setSelectedStateColor(ContextCompat.getColor(context, if (isNight)
                R.color.dark_secondary_text_color
            else
                R.color.light_secondary_text_color))

            binding.txtProfileInfoBio.setAutoLinkText(if (viewModel.bio == null) "" else viewModel.bio)
            binding.txtProfileInfoBio.setAutoLinkOnClickListener(object : AutoLinkOnClickListener {
                override fun onAutoLinkTextClick(autoLinkMode: AutoLinkMode, matchedText: String) {
                    if (profileListener != null) {
                        when (autoLinkMode) {
                            AutoLinkMode.MODE_MENTION -> {
                                val intent = Intent(context, MVPProfileActivity::class.java)
                                intent.putExtra(Flags.PROFILE_SCREEN_NAME, matchedText)
                                activity.startActivity(intent)
                                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            }
                            AutoLinkMode.MODE_URL -> Utilities.openLink(matchedText.trim { it <= ' ' }, activity)
                            AutoLinkMode.MODE_HASHTAG -> {
                                AppData.searchQuery = matchedText
                                activity.startActivity(Intent(context, SearchActivity::class.java))
                                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            }
                            else -> Log.e(TAG, "unhandled touch")
                        }
                    }
                }

                override fun onAutoLinkLongTextClick(autoLinkMode: AutoLinkMode, matchedText: String) {
                    Log.e(TAG, "long click - " + matchedText)
                }
            })

            binding.txtProfileInfoLink.setOnClickListener {
                Utilities.openLink(binding.txtProfileInfoLink.text.toString(), activity)
            }

            binding.txtProfileInfoLocation.setOnClickListener {
                val uri = "geo:0,0?q=" + viewModel.location
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(intent)
            }

        }

        measureHeight()
        return binding.root
    }

    private fun measureHeight() {
        binding.txtProfileInfoBio.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.txtProfileInfoBio.viewTreeObserver.removeOnGlobalLayoutListener(this)
                height = binding.txtProfileInfoBio.lineHeight * binding.txtProfileInfoBio.lineCount
                additionalHeight = if (viewModel.location != null && viewModel.location.trim { it <= ' ' } != "" && viewModel.link != null && viewModel.link.trim { it <= ' ' } != "") {
                    Utilities.convertDpToPixel(44f, context).toInt()
                } else if ((viewModel.location == null || viewModel.location.trim { it <= ' ' } == "") && (viewModel.link == null || viewModel.link.trim { it <= ' ' } == "")) {
                    0
                } else {
                    Utilities.convertDpToPixel(22f, context).toInt()
                }

                estimatedHeight = Utilities.convertDpToPixel(80f, context).toInt()
                changeProfileHeight(false)
            }
        })
    }

    /**
     * Setup users info
     *
     * @param bio      - user's bio
     * @param link     - user's link
     * @param location - user's location
     */
    fun setViewModel(bio: String?, link: String, location: String) {
//        viewModel = ProfileInfoViewModel(bio, link, location)
//        if (bio != null) {
//            if (TextUtils.isEmpty(bio)) {
//                binding.txtProfileInfoBio.setAutoLinkText("No Bio")
//                binding.txtProfileInfoBio.setTypeface(binding.txtProfileInfoBio.typeface, Typeface.ITALIC)
//                binding.txtProfileInfoBio.setTextColor(resources.getColor(R.color.dark_hint_text_color))
//
//            } else {
//                binding.txtProfileInfoBio.setAutoLinkText(bio)
//            }
//        }
//        binding.model = viewModel
//
//        measureHeight()
//        changeProfileHeight(false)
    }

    /**
     * Change height for fader in activity
     *
     * @param isAnimated - @true for animation
     */
    fun changeProfileHeight(isAnimated: Boolean) {
        try {
            val diff = height + additionalHeight - estimatedHeight
            if (profileListener != null)
                profileListener!!.updateHeader(diff, isAnimated)
        } catch (e: Exception) {
            Log.e(TAG, "exception - " + e.localizedMessage)
        }
    }

    fun setProfileListener(profileListener: ProfileView) {
        this.profileListener = profileListener
    }
}
