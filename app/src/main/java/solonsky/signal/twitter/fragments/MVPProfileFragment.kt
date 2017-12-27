package solonsky.signal.twitter.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import android.widget.*
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import solonsky.signal.twitter.R
import solonsky.signal.twitter.activities.*
import solonsky.signal.twitter.adapters.SimplePagerAdapter
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.libs.DownloadFiles
import solonsky.signal.twitter.models.ImageModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.overlays.ImageOverlay
import solonsky.signal.twitter.presenters.ProfilePresenter
import solonsky.signal.twitter.views.ProfileView
import java.util.*

/**
 * Created by sunwi on 28.11.2017.
 */
class MVPProfileFragment: MvpAppCompatFragment(), SmartTabLayout.TabProvider, ProfileView {
    private val TAG: String = MVPProfileFragment::class.java.simpleName

    @InjectPresenter
    lateinit var mProfilePresenter: ProfilePresenter

    private val avatarSize = 76f
    private val verifiedSize = 20f
    private val tailOffset = 208f
    private val tabsOffset = 176
    private val faderOffset = 138
    private val estimatedPagerSize = 80
    private var currentPosition = 0
    private var difference: Int = 0

    private val profileTweetsFragment = ProfileTweetsFragment()
    private val profileLikesFragment = ProfileLikesFragment()
    private val profileMediaFragment = ProfileMediaFragment()

    private lateinit var headerInfoFragment: HeaderInfoFragment
    private lateinit var headerStatsFragment: HeaderStatsFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.activity_profile, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as LoggedActivity).viewModel.toolbarState = AppData.TOOLBAR_LOGGED_PROFILE
        (activity as LoggedActivity).binding.rlLoggedContainer.fitsSystemWindows = false
        (activity as LoggedActivity).setStatusBarColor(android.R.color.transparent)

        scroll_profile.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            mProfilePresenter.updateUI(dY = scrollY,
                    avatarSize = Utilities.convertDpToPixel(avatarSize, context).toInt(),
                    verifiedSize = Utilities.convertDpToPixel(verifiedSize, context).toInt(),
                    tailOffset = Utilities.convertDpToPixel(tailOffset + 16, context).toInt())
        })

        setupContentPager()

        img_profile_avatar.setOnClickListener { mProfilePresenter.showAvatar() }
        img_profile_test_header.setOnClickListener { mProfilePresenter.showBackdrop() }
        tb_profile_header.setOnClickListener { mProfilePresenter.showBackdrop() }
        btn_profile_back.setOnClickListener { mProfilePresenter.backClick() }

        mProfilePresenter.getUser(user = AppData.ME)
        mProfilePresenter.selectFragment(fragment = profileTweetsFragment,
                fragmentManager = childFragmentManager)
    }

    private fun setupHeaderPager() {
        val fragments = ArrayList<Fragment>()
        headerStatsFragment.setProfileListener(this)
//        headerInfoFragment.setProfileListener(this)

        fragments.add(headerStatsFragment)
        fragments.add(headerInfoFragment)

        val adapter = SimplePagerAdapter(fragments, childFragmentManager)
        vp_profile_header.adapter = adapter
        stb_profile_header.setViewPager(vp_profile_header)
        stb_profile_header.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                headerStatsFragment.setProfileListener(this@MVPProfileFragment)
                headerInfoFragment.setProfileListener(this@MVPProfileFragment)

                when (position) {
                    1 -> headerInfoFragment.changeProfileHeight(true)
                    0 -> headerStatsFragment.changeProfileHeight(true)
                }
            }
        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (activity as LoggedActivity).viewModel.toolbarState = AppData.TOOLBAR_LOGGED_PROFILE
            (activity as LoggedActivity).binding.rlLoggedContainer.fitsSystemWindows = false
            (activity as LoggedActivity).setStatusBarColor(android.R.color.transparent)
        }
    }

    private fun setupContentPager() {
        val adapter = FragmentPagerItemAdapter(
                childFragmentManager, FragmentPagerItems.with(context)
                .add(getString(R.string.profile_tweets).toUpperCase(), DummyFragment::class.java)
                .add(getString(R.string.profile_likes).toUpperCase(), DummyFragment::class.java)
                .add(getString(R.string.profile_media).toUpperCase(), DummyFragment::class.java)
                .create())

        vp_profile_fragment.adapter = adapter
        stb_profile_fragment.setCustomTabView(this)
        stb_profile_fragment.setViewPager(vp_profile_fragment)
        stb_profile_fragment.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> mProfilePresenter.selectFragment(fragment = profileTweetsFragment,
                            fragmentManager = childFragmentManager)
                    1 -> mProfilePresenter.selectFragment(fragment = profileLikesFragment,
                            fragmentManager = childFragmentManager)
                    2 -> mProfilePresenter.selectFragment(fragment = profileMediaFragment,
                            fragmentManager = childFragmentManager)
                }

                val oldTab = stb_profile_fragment.getTabAt(currentPosition)
                val currentTab = stb_profile_fragment.getTabAt(position)

                val oldView = oldTab.findViewById<View>(R.id.tab_layout) as RelativeLayout
                val currentView = currentTab.findViewById<View>(R.id.tab_layout) as RelativeLayout
                val tabTxt = currentView.findViewById<View>(R.id.tab_txt) as TextView
                val oldTabTxt = oldView.findViewById<View>(R.id.tab_txt) as TextView

                tabTxt.setTextColor(resources.getColor(if (App.getInstance().isNightEnabled)
                    R.color.dark_primary_text_color
                else
                    R.color.light_primary_text_color))
                tabTxt.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)

                oldTabTxt.setTextColor(resources.getColor(if (App.getInstance().isNightEnabled)
                    R.color.dark_hint_text_color
                else
                    R.color.light_hint_text_color))
                oldTabTxt.typeface = Typeface.DEFAULT

                oldView.background = resources.getDrawable(R.drawable.tab_shape_transparent)
                currentView.background = resources.getDrawable(if (App.getInstance().isNightEnabled)
                    R.drawable.tab_shape_dark
                else
                    R.drawable.tab_shape_light)

                currentPosition = position
            }
        })
    }

    // View part
    override fun performBack(startAnim: Int, endAnim: Int) {
        // Do nothing
    }

    override fun showError(errorMessage: Int) {
        Toast.makeText(context, getString(errorMessage), Toast.LENGTH_SHORT).show()
    }

    override fun setupUser(user: User, homeUser: Boolean) {
        Picasso.with(context)
                .load(user.originalProfileImageURL)
                .into(img_profile_avatar_image)

        val screenWidth = Utilities.getScreenWidth(activity)
        val bannerHeight = Utilities.convertDpToPixel(186f, activity)

        Picasso.with(context)
                .load(user.profileBannerImageUrl)
                .resize(screenWidth, bannerHeight.toInt())
                .centerCrop()
                .into(img_profile_test_header)

        txt_profile_username.text = user.name
        txt_profile_tb_username.text = user.name
        txt_profile_screen_name.text = "@${user.screenName}"
        txt_profile_tb_followers.text = Utilities.parseFollowers(user.followersCount, "followers")
        img_profile_verified.visibility = if (user.isVerified) View.VISIBLE else View.GONE

        if (homeUser) {
            btn_profile_back.visibility = View.GONE
            btn_profile_more.visibility = View.GONE
            btn_profile_settings.visibility = View.VISIBLE
        } else {
            btn_profile_back.visibility = View.VISIBLE
            btn_profile_more.visibility = View.VISIBLE
            btn_profile_settings.visibility = View.GONE
        }

        /**
         * FIX IT: Need to store user in Intent not in global value
         * will fix after bug fixing
         */

        fl_profile_more.setOnClickListener {
            when (currentPosition) {
                0 -> {
                    AppData.CURRENT_USER = user
                    activity.startActivity(Intent(context, StatsTweetsActivity::class.java))
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                1 -> {
                    AppData.CURRENT_USER = user
                    activity.startActivity(Intent(context, StatsLikesActivity::class.java))
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                2 -> {
                    AppData.CURRENT_USER = user
                    activity.startActivity(Intent(context, StatsImagesActivity::class.java))
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        }

        btn_profile_settings.setOnClickListener {
            AppData.CURRENT_USER = user
            activity.startActivity(Intent(context, ProfileSettingsActivity::class.java))
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        /**
         * FIX IT Stop
         */

        headerInfoFragment = HeaderInfoFragment.newInstance(user)
        headerStatsFragment = HeaderStatsFragment.newInstance(user)
        setupHeaderPager()
    }

    override fun setupTweets(tweetsArray: ArrayList<StatusModel>) {
        profileTweetsFragment.setupTweets(tweetsArray)
    }

    override fun setupMedia(mediaArray: ArrayList<ImageModel>) {
        profileMediaFragment.setupMedia(mediaArray = mediaArray)
    }

    override fun setupLikes(likesArray: ArrayList<StatusModel>) {
        profileLikesFragment.setupLikes(likesArray = likesArray)
    }

    override fun updateAvatar(verified: Int, avatar: Int, progress: Float) {
        val params = img_profile_avatar.layoutParams as RelativeLayout.LayoutParams
        val verifiedParams = img_profile_verified.layoutParams as RelativeLayout.LayoutParams

        verifiedParams.width = verified
        verifiedParams.height = verified

        params.width = avatar
        params.height = avatar
        params.topMargin = -params.height / 2

        verifiedParams.bottomMargin = ((Utilities.convertDpToPixel(verifiedSize, context) - verifiedParams.height) / 3).toInt()
        verifiedParams.rightMargin = ((Utilities.convertDpToPixel(verifiedSize, context) - verifiedParams.height) / 3).toInt()

        img_profile_avatar.alpha = progress
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        img_profile_avatar.layoutParams = params
        img_profile_verified.layoutParams = verifiedParams
    }

    override fun updateTab(diff: Int) {
        val summary = diff + difference
        if (summary < 0) {
            stb_profile_fragment.animate().translationY(-summary.toFloat()).setDuration(0).start()
        } else {
            stb_profile_fragment.animate().translationY(0f).setDuration(0).start()
        }
    }

    override fun updateTitle(scrollY: Int) {
        val diff = (Utilities.convertDpToPixel(51.25f, context) - scrollY).toInt()
        val params = ll_toolbar_title.getLayoutParams() as FrameLayout.LayoutParams

        if (diff > 0) {
            params.topMargin = Utilities.convertDpToPixel(80f, context).toInt()
        } else {
            params.topMargin = if (-diff >= Utilities.convertDpToPixel(44.75f, context))
                Utilities.convertDpToPixel(35.25f, context).toInt()
            else
                Utilities.convertDpToPixel(80f, context).toInt() + diff
        }

        ll_toolbar_title.layoutParams = params
    }

    override fun updateHeader(diff: Int, isAnimated: Boolean) {
        Log.e(TAG, "update header ${diff}")
        difference = if (diff == 0) difference else diff

        val paramsFader = rl_header_fader.layoutParams as RelativeLayout.LayoutParams
        val paramsStbContent = stb_profile_fragment.layoutParams as RelativeLayout.LayoutParams
        val paramsFlContent = fl_profile.layoutParams as RelativeLayout.LayoutParams
        val paramsVpHeader = vp_profile_header.layoutParams as LinearLayout.LayoutParams

        if (isAnimated) {
            val duration = 300

            val animHeader = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (diff == 0) {
                        paramsFader.topMargin = (Utilities.convertDpToPixel(faderOffset.toFloat(), context) + difference - difference * interpolatedTime).toInt()
                    } else {
                        paramsFader.topMargin = (Utilities.convertDpToPixel(faderOffset.toFloat(), context) + diff * interpolatedTime).toInt()
                    }
                    rl_header_fader.layoutParams = paramsFader
                }
            }

            val animStb = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (diff == 0) {
                        paramsStbContent.topMargin = (Utilities.convertDpToPixel(tabsOffset.toFloat(), context) + difference - difference * interpolatedTime).toInt()
                    } else {
                        paramsStbContent.topMargin = (Utilities.convertDpToPixel(tabsOffset.toFloat(), context) + diff * interpolatedTime).toInt()
                    }
                    stb_profile_fragment.layoutParams = paramsStbContent
                }
            }

            val animFragment = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (diff == 0) {
                        paramsFlContent.topMargin = (Utilities.convertDpToPixel(tailOffset, context) + difference - difference * interpolatedTime).toInt()
                    } else {
                        paramsFlContent.topMargin = (Utilities.convertDpToPixel(tailOffset, context) + diff * interpolatedTime).toInt()
                    }

                    fl_profile.layoutParams = paramsFlContent
                }
            }

            animHeader.duration = duration.toLong()
            animStb.duration = duration.toLong()
            animFragment.duration = duration.toLong()

            animHeader.interpolator = DecelerateInterpolator()
            animStb.interpolator = DecelerateInterpolator()
            animFragment.interpolator = DecelerateInterpolator()

            rl_header_fader.startAnimation(animHeader)
            stb_profile_fragment.startAnimation(animStb)
            fl_profile.startAnimation(animFragment)

            Handler().postDelayed({ difference = diff }, duration.toLong())
        } else {
            if (diff > 0) {
                paramsVpHeader.height = (Utilities.convertDpToPixel(estimatedPagerSize.toFloat(), context) + diff).toInt()
                vp_profile_header.layoutParams = paramsVpHeader
            }

            paramsFader.topMargin = (Utilities.convertDpToPixel(faderOffset.toFloat(), context) + diff).toInt()
            rl_header_fader.layoutParams = paramsFader

            paramsStbContent.topMargin = (Utilities.convertDpToPixel(tabsOffset.toFloat(), context) + diff).toInt()
            stb_profile_fragment.layoutParams = paramsStbContent

            paramsFlContent.topMargin = (Utilities.convertDpToPixel(tailOffset, context) + diff).toInt()
            fl_profile.layoutParams = paramsFlContent

            difference = diff
        }
    }

    override fun showImage(urls: ArrayList<String>, startPosition: Int) {
        val imageOverlay = ImageOverlay(urls, activity as AppCompatActivity?, startPosition)
        imageOverlay.setImageOverlayClickHandler(object : ImageOverlay.ImageOverlayClickHandler {
            override fun onBackClick(v: View) {
                imageOverlay.imageViewer.onDismiss()
            }

            override fun onSaveClick(v: View, url: String) {
                val downloadFiles = DownloadFiles(activity as AppCompatActivity?)
                downloadFiles.saveFile(url, getString(R.string.download_url))
            }
        })
    }

    override fun createTabView(container: ViewGroup, position: Int, adapter: PagerAdapter?): View {
        val inflater = LayoutInflater.from(container.context)
        val res = container.context.resources
        val tab = inflater.inflate(R.layout.tab_item_only_text, container, false)

        val imageView = tab.findViewById<View>(R.id.tab_iv) as ImageView
        val textView = tab.findViewById<View>(R.id.tab_txt) as TextView
        val layoutView = tab.findViewById<View>(R.id.tab_layout) as RelativeLayout

        textView.typeface = if (currentPosition == position)
            Typeface.create("sans-serif-medium", Typeface.NORMAL)
        else
            Typeface.create("sans-serif", Typeface.NORMAL)
        textView.setTextColor(res.getColor(if (App.getInstance().isNightEnabled)
            if (currentPosition == position) R.color.dark_primary_text_color else R.color.dark_hint_text_color
        else if (currentPosition == position) R.color.light_primary_text_color else R.color.light_hint_text_color))

        imageView.visibility = View.GONE
        layoutView.background = if (currentPosition == position)
            res.getDrawable(if (App.getInstance().isNightEnabled)
                R.drawable.tab_shape_dark
            else
                R.drawable.tab_shape_light)
        else
            res.getDrawable(R.drawable.tab_shape_transparent)

        when (position) {
            0 -> textView.text = getString(R.string.profile_tweets).toUpperCase()
            1 -> textView.text = getString(R.string.profile_likes).toUpperCase()
            2 -> textView.text = getString(R.string.profile_media).toUpperCase()
        }

        return tab
    }
}