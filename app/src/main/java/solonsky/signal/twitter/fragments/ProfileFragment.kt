package solonsky.signal.twitter.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.stfalcon.frescoimageviewer.ImageViewer

import java.util.ArrayList

import solonsky.signal.twitter.R
import solonsky.signal.twitter.adapters.SimplePagerAdapter
import solonsky.signal.twitter.api.ProfileDataApi
import solonsky.signal.twitter.data.ProfileRefreshData
import solonsky.signal.twitter.databinding.ActivityProfileBinding
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.interfaces.ProfileRefreshHandler
import solonsky.signal.twitter.libs.DownloadFiles
import solonsky.signal.twitter.viewmodels.ProfileViewModel
import solonsky.signal.twitter.views.ProfileView

import android.view.View.inflate
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.arellomobile.mvp.presenter.InjectPresenter
import solonsky.signal.twitter.activities.*
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.models.ImageModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.presenters.ProfilePresenter

/**
 * Created by neura on 24.06.17.
 */

class ProfileFragment : Fragment(), SmartTabLayout.TabProvider, ProfileView {
    override fun performBack(startAnim: Int, endAnim: Int) {

    }

    override fun showError(errorMessage: Int) {

    }

    override fun setupUser(user: User, homeUser: Boolean) {

    }

    override fun setupTweets(tweetsArray: ArrayList<StatusModel>) {

    }

    override fun setupMedia(mediaArray: ArrayList<ImageModel>) {

    }

    override fun setupLikes(likesArray: ArrayList<StatusModel>) {

    }

    override fun updateAvatar(verified: Int, avatar: Int, progress: Float) {

    }

    override fun updateTab(diff: Int) {

    }

    override fun updateTitle(scrollY: Int) {

    }

    override fun showImage(urls: ArrayList<String>, startPosition: Int) {

    }

    private val TAG: String = ProfileFragment::class.java.simpleName

    @InjectPresenter
    lateinit var mProfilePresenter: ProfilePresenter

    /**
     * Avatar size in DP
     *
     * @link Utilities.convertDptoPixel
     */
    private val AVATAR_SIZE = 76

    /**
     * Verified icon size in DP
     *
     * @link Utilities.convertDptoPixel
     */
    private val VERIFIED_SIZE = 20
    private val TAIL_Y_OFFSET = 208
    private val TABS_Y_OFFSET = 176
    private val FADER_Y_OFFSET = 138
    private val ESTIMATED_PAGER_SIZE = 80

    private var CURRENT_POSITION = 0
    private var mActivity: LoggedActivity? = null

    private var viewModel: ProfileViewModel? = null
    private var difference = 0

    private val profileTweetsFragment = ProfileTweetsFragment()
    private val profileLikesFragment = ProfileLikesFragment()
    private val profileMediaFragment = ProfileMediaFragment()

    private lateinit var headerInfoFragment: HeaderInfoFragment
    private lateinit var headerStatsFragment: HeaderStatsFragment

    private lateinit var binding: ActivityProfileBinding

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater!!, R.layout.activity_profile, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity = activity as LoggedActivity
        mActivity!!.viewModel.toolbarState = AppData.TOOLBAR_LOGGED_PROFILE
        mActivity!!.binding.rlLoggedContainer.fitsSystemWindows = false
        mActivity!!.setStatusBarColor(android.R.color.transparent)

        binding.scrollProfile.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
        //            mProfilePresenter.updateUI(dY = scrollY,
        //                    avatarSize = Utilities.convertDpToPixel(avatarSize, applicationContext).toInt(),
        //                    verifiedSize = Utilities.convertDpToPixel(verifiedSize, applicationContext).toInt(),
        //                    tailOffset = Utilities.convertDpToPixel(tailOffset + 16, applicationContext).toInt())
        })

        viewModel = ProfileViewModel(AppData.ME.profileBannerImageUrl,
                AppData.ME.originalProfileImageURL, AppData.ME.name, "@" + AppData.ME.screenName,
                "Followers " + AppData.ME.followersCount, false)
        viewModel!!.isHomeUser = true

        binding.model = viewModel
        binding.click = object : ProfileViewModel.ProfileClickHandler {
            override fun onBackClick(v: View) {
                mActivity!!.onBackPressed()
            }

            override fun onSettingsClick(v: View) {
                mActivity!!.startActivity(Intent(context, ProfileSettingsActivity::class.java))
                mActivity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onMoreClick(v: View) {
                when (CURRENT_POSITION) {
                    0 -> {
                        AppData.CURRENT_USER = AppData.ME
                        mActivity!!.startActivity(Intent(context, StatsTweetsActivity::class.java))
                        mActivity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }

                    1 -> {
                        AppData.CURRENT_USER = AppData.ME
                        mActivity!!.startActivity(Intent(context, StatsLikesActivity::class.java))
                        mActivity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }

                    2 -> {
                    }
                }
            }

            override fun onLinkClick(v: View) {

            }

            override fun onMenuClick(v: View) {
                // Do nothing
            }

            override fun onAvatarClick(v: View) {
                val urls = ArrayList<String>()
                urls.add(viewModel!!.avatar)

                val overlay = inflate(context, R.layout.overlay_profile, null)

                val imageViewer = ImageViewer.Builder(mActivity, urls)
                        .setStartPosition(0)
                        .setOverlayView(overlay)
                        .setStyleRes(R.style.statusMediaStyle).build()

                imageViewer.show()
                overlay.findViewById<View>(R.id.img_profile_content_back).setOnClickListener { imageViewer.onDismiss() }
                overlay.findViewById<View>(R.id.img_profile_content_save).setOnClickListener {
                    val downloadFiles = DownloadFiles(mActivity)
                    downloadFiles.saveFile(urls[0], mActivity!!.getString(R.string.download_url))
                }
            }

            override fun onBackDropClick(v: View) {
                val urls = ArrayList<String>()
                urls.add(viewModel!!.backdrop)

                val overlay = inflate(context, R.layout.overlay_profile, null)

                val imageViewer = ImageViewer.Builder(mActivity, urls)
                        .setStartPosition(0)
                        .setOverlayView(overlay)
                        .setStyleRes(R.style.statusMediaStyle).build()

                imageViewer.show()
                overlay.findViewById<View>(R.id.img_profile_content_back).setOnClickListener { imageViewer.onDismiss() }
                overlay.findViewById<View>(R.id.img_profile_content_save).setOnClickListener {
                    val downloadFiles = DownloadFiles(mActivity)
                    downloadFiles.saveFile(urls[0], mActivity!!.getString(R.string.download_url))
                }
            }
        }

        Log.e(TAG, "old banner " + viewModel!!.backdrop)
        Picasso.with(context).load(viewModel!!.backdrop)
                .resize(Utilities.getScreenWidth(mActivity), Utilities.convertDpToPixel(186f, context).toInt())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .centerCrop()
                .into(binding.imgProfileTestHeader)

        Picasso.with(context).load(viewModel!!.avatar)
                .resize(Utilities.convertDpToPixel(72f, context).toInt(),
                        Utilities.convertDpToPixel(72f, context).toInt())
                .centerCrop()
                .into(binding.imgProfileAvatarImage)

        ProfileDataApi.getInstance().screenName = AppData.ME.screenName
        ProfileDataApi.getInstance().loadData()
        ProfileRefreshData.getInstance().updateHandler = object : ProfileRefreshHandler {
            override fun onAvatarUpdate() {
                viewModel!!.avatar = AppData.ME.originalProfileImageURL
                Picasso.with(context)
                        .load(AppData.ME.originalProfileImageURL)
                        .into(binding.imgProfileAvatarImage)
            }

            override fun onBannerUpdate() {
                viewModel!!.backdrop = AppData.ME.profileBannerImageUrl
                binding.imgProfileTestHeader.setImageResource(android.R.color.transparent)
                Picasso.with(context).load(AppData.ME.profileBannerImageUrl)
                        .resize(Utilities.getScreenWidth(mActivity), Utilities.convertDpToPixel(186f, context).toInt())
                        .centerCrop()
                        .into(binding.imgProfileTestHeader, object : Callback {
                            override fun onSuccess() {
                                binding.imgProfileTestHeader.postDelayed({
                                    binding.imgProfileTestHeader.invalidate()
                                    binding.imgProfileTestHeader.requestLayout()
                                }, 50)
                            }

                            override fun onError() {
                                Log.e(TAG, "Error loading new image")
                            }
                        })
            }

            override fun onInfoUpdate() {
                viewModel!!.username = AppData.ME.name
                viewModel!!.twitterName = AppData.ME.screenName

                var realDescription = ""

                if (AppData.ME.description != null) {
                    realDescription = AppData.ME.description
                    if (AppData.ME.descriptionUrlEntities != null) {
                        for (jsonElement in AppData.ME.descriptionUrlEntities) {
                            realDescription = realDescription.replace(
                                    jsonElement.asJsonObject.get("url").asString,
                                    jsonElement.asJsonObject.get("expandedURL").asString)
                        }
                    }
                }

                headerInfoFragment.setViewModel(
                        realDescription, if (AppData.ME.urlEntity == null)
                    ""
                else
                    AppData.ME.urlEntity.get("displayURL").asString, AppData.ME.location)
            }
        }

        setupHeaderPager()
        setupContentPager()

        binding.imgProfileAvatar.postDelayed({ updateHeader(0, false) }, 100)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            mActivity!!.endSearching(false)
            mActivity!!.viewModel.toolbarState = AppData.TOOLBAR_LOGGED_PROFILE
            mActivity!!.binding.rlLoggedContainer.fitsSystemWindows = false
            mActivity!!.setStatusBarColor(android.R.color.transparent)
        }
    }

    private fun selectFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_profile, fragment).commit()
    }

    /**
     * Setup title floating position
     *
     * @param scrollY - scroll y position
     */
    private fun setupTitle(scrollY: Int) {
        val diff = (Utilities.convertDpToPixel(51.25f, context) - scrollY).toInt()
        val params = binding.llToolbarTitle.layoutParams as FrameLayout.LayoutParams

        if (diff > 0) {
            params.topMargin = Utilities.convertDpToPixel(80f, context).toInt()
        } else {
            params.topMargin = if (-diff >= Utilities.convertDpToPixel(44.75f, context))
                Utilities.convertDpToPixel(35.25f, context).toInt()
            else
                Utilities.convertDpToPixel(80f, context).toInt() + diff
        }

        binding.llToolbarTitle.layoutParams = params
    }

    /**
     * Setup tab fixed position when scroll
     *
     * @param scrollY - scroll y position
     */
    private fun setupTab(scrollY: Int) {
        val diff = (Utilities.convertDpToPixel((TAIL_Y_OFFSET + 16).toFloat(), context) + difference - scrollY).toInt()
        if (diff < 0) {
            binding.stbProfileFragment.animate().translationY((-diff).toFloat()).setDuration(0).start()
        } else {
            binding.stbProfileFragment.animate().translationY(0f).setDuration(0).start()
        }
    }

    /**
     * Setup content view pager
     */
    private fun setupContentPager() {
        val adapter = FragmentPagerItemAdapter(
                childFragmentManager, FragmentPagerItems.with(mActivity)
                .add(getString(R.string.profile_tweets).toUpperCase(), DummyFragment::class.java)
                .add(getString(R.string.profile_likes).toUpperCase(), DummyFragment::class.java)
                .add(getString(R.string.profile_media).toUpperCase(), DummyFragment::class.java)
                .create())

        binding.vpProfileFragment.adapter = adapter
        binding.stbProfileFragment.setCustomTabView(this)
        binding.stbProfileFragment.setViewPager(binding.vpProfileFragment)
        binding.stbProfileFragment.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        selectFragment(profileTweetsFragment)
                        viewModel!!.selectorName = getString(R.string.profile_tweets)
                    }

                    1 -> {
                        selectFragment(profileLikesFragment)
                        viewModel!!.selectorName = getString(R.string.profile_likes)
                    }

                    2 -> {
                        selectFragment(profileMediaFragment)
                        viewModel!!.selectorName = getString(R.string.profile_media)
                    }
                }

                val oldTab = binding.stbProfileFragment.getTabAt(CURRENT_POSITION)
                val currentTab = binding.stbProfileFragment.getTabAt(position)

                val oldView = oldTab.findViewById<View>(R.id.tab_layout) as RelativeLayout
                val currentView = currentTab.findViewById<View>(R.id.tab_layout) as RelativeLayout

                (currentView.findViewById<View>(R.id.tab_txt) as TextView).setTextColor(resources
                        .getColor(if (App.getInstance().isNightEnabled)
                            R.color.dark_primary_text_color
                        else
                            R.color.light_primary_text_color))
                (currentView.findViewById<View>(R.id.tab_txt) as TextView).typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)

                (oldView.findViewById<View>(R.id.tab_txt) as TextView).setTextColor(resources
                        .getColor(if (App.getInstance().isNightEnabled)
                            R.color.dark_hint_text_color
                        else
                            R.color.light_hint_text_color))
                (oldView.findViewById<View>(R.id.tab_txt) as TextView).typeface = Typeface.DEFAULT

                oldView.background = resources.getDrawable(R.drawable.tab_shape_transparent)
                currentView.background = resources.getDrawable(if (App.getInstance().isNightEnabled)
                    R.drawable.tab_shape_dark
                else
                    R.drawable.tab_shape_light)

                CURRENT_POSITION = position
            }
        })

        binding.vpProfileFragment.postDelayed({ selectFragment(profileTweetsFragment) }, 1000)
    }

    /**
     * Setup view pager placed in header
     */
    private fun setupHeaderPager() {
        val fragments = ArrayList<Fragment>()

        headerStatsFragment.setProfileListener(this)
        headerInfoFragment.setProfileListener(this)

        fragments.add(headerStatsFragment)
        fragments.add(headerInfoFragment)

        val adapter = SimplePagerAdapter(fragments, childFragmentManager)
        binding.vpProfileHeader.adapter = adapter
        binding.stbProfileHeader.setViewPager(binding.vpProfileHeader)
        binding.stbProfileHeader.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    1 -> headerInfoFragment.changeProfileHeight(true)

                    0 -> headerStatsFragment.changeProfileHeight(true)
                }
            }
        })
    }

    override fun createTabView(container: ViewGroup, position: Int, adapter: PagerAdapter): View {
        val inflater = LayoutInflater.from(container.context)
        val res = container.context.resources
        val tab = inflater.inflate(R.layout.tab_item_only_text, container, false)
        val imageView = tab.findViewById<View>(R.id.tab_iv) as ImageView
        val textView = tab.findViewById<View>(R.id.tab_txt) as TextView
        val layoutView = tab.findViewById<View>(R.id.tab_layout) as RelativeLayout

        textView.typeface = if (CURRENT_POSITION == position)
            Typeface.create("sans-serif-medium", Typeface.NORMAL)
        else
            Typeface.create("sans-serif", Typeface.NORMAL)
        textView.setTextColor(res.getColor(if (App.getInstance().isNightEnabled)
            if (CURRENT_POSITION == position) R.color.dark_primary_text_color else R.color.dark_hint_text_color
        else if (CURRENT_POSITION == position) R.color.light_primary_text_color else R.color.light_hint_text_color))

        imageView.visibility = View.GONE
        layoutView.background = if (CURRENT_POSITION == position)
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

    override fun updateHeader(diff: Int, isAnimated: Boolean) {
        difference = if (diff == 0) difference else diff

        val paramsFader = binding.rlHeaderFader.layoutParams as RelativeLayout.LayoutParams
        val paramsStbContent = binding.stbProfileFragment.layoutParams as RelativeLayout.LayoutParams
        val paramsFlContent = binding.flProfile.layoutParams as RelativeLayout.LayoutParams
        val paramsVpHeader = binding.vpProfileHeader.layoutParams as LinearLayout.LayoutParams

        if (isAnimated) {
            val duration = 300

            val animVpHeader = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (diff == 0) {
                        //                        paramsVpHeader.height = ((int) (Utilities.convertDpToPixel(ESTIMATED_PAGER_SIZE, getApplicationContext()) + difference - (difference * interpolatedTime)));
                    } else if (diff > 0) {
                        //                        paramsVpHeader.height = ((int) (Utilities.convertDpToPixel(ESTIMATED_PAGER_SIZE, getApplicationContext()) + (diff * interpolatedTime)));
                    }
                    binding.vpProfileHeader.layoutParams = paramsVpHeader
                }
            }

            val animHeader = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (diff == 0) {
                        paramsFader.topMargin = (Utilities.convertDpToPixel(FADER_Y_OFFSET.toFloat(), context) + difference - difference * interpolatedTime).toInt()
                    } else {
                        paramsFader.topMargin = (Utilities.convertDpToPixel(FADER_Y_OFFSET.toFloat(), context) + diff * interpolatedTime).toInt()
                    }
                    binding.rlHeaderFader.layoutParams = paramsFader
                }
            }

            val animStb = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (diff == 0) {
                        paramsStbContent.topMargin = (Utilities.convertDpToPixel(TABS_Y_OFFSET.toFloat(), context) + difference - difference * interpolatedTime).toInt()
                    } else {
                        paramsStbContent.topMargin = (Utilities.convertDpToPixel(TABS_Y_OFFSET.toFloat(), context) + diff * interpolatedTime).toInt()
                    }
                    binding.stbProfileFragment.layoutParams = paramsStbContent
                }
            }

            val animFragment = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (diff == 0) {
                        paramsFlContent.topMargin = (Utilities.convertDpToPixel(TAIL_Y_OFFSET.toFloat(), context) + difference - difference * interpolatedTime).toInt()
                    } else {
                        paramsFlContent.topMargin = (Utilities.convertDpToPixel(TAIL_Y_OFFSET.toFloat(), context) + diff * interpolatedTime).toInt()
                    }
                    binding.flProfile.layoutParams = paramsFlContent
                }
            }

            animHeader.duration = duration.toLong()
            animStb.duration = duration.toLong()
            animFragment.duration = duration.toLong()
            animVpHeader.duration = duration.toLong()

            animHeader.interpolator = DecelerateInterpolator()
            animStb.interpolator = DecelerateInterpolator()
            animFragment.interpolator = DecelerateInterpolator()
            animVpHeader.interpolator = DecelerateInterpolator()

            binding.rlHeaderFader.startAnimation(animHeader)
            binding.stbProfileFragment.startAnimation(animStb)
            binding.flProfile.startAnimation(animFragment)
            binding.vpProfileHeader.startAnimation(animVpHeader)

            Handler().postDelayed({ difference = diff }, duration.toLong())
        } else {
            Log.e(TAG, "no animation - " + diff)
            if (diff >= 0) {
                paramsVpHeader.height = (Utilities.convertDpToPixel(ESTIMATED_PAGER_SIZE.toFloat(), context) + diff).toInt()
                binding.vpProfileHeader.layoutParams = paramsVpHeader
            }

            paramsFader.topMargin = (Utilities.convertDpToPixel(FADER_Y_OFFSET.toFloat(), context) + diff).toInt()
            binding.rlHeaderFader.layoutParams = paramsFader

            paramsStbContent.topMargin = (Utilities.convertDpToPixel(TABS_Y_OFFSET.toFloat(), context) + diff).toInt()
            binding.stbProfileFragment.layoutParams = paramsStbContent

            paramsFlContent.topMargin = (Utilities.convertDpToPixel(TAIL_Y_OFFSET.toFloat(), context) + diff).toInt()
            binding.flProfile.layoutParams = paramsFlContent

            difference = diff
        }
    }


}
