package solonsky.signal.twitter.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

import com.twitter.Autolink;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ProfileActivity;
import solonsky.signal.twitter.activities.SearchActivity;
import solonsky.signal.twitter.databinding.FragmentProfileInfoBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.HeaderListener;
import solonsky.signal.twitter.interfaces.ProfileListener;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkMode;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkOnClickListener;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.viewmodels.ProfileInfoViewModel;

/**
 * Created by neura (Alex Gladkov) on 27.05.17.
 * <p>
 * Sub fragment for view pager in Profiles
 * Describes user bio, location, url
 *
 * @link res/layout/fragment_profile_info.xml
 * @see ProfileActivity
 * @see ProfileFragment
 */
public class HeaderInfoFragment extends Fragment {
    private final String TAG = HeaderInfoFragment.class.getSimpleName();
    private ProfileInfoViewModel viewModel;
    private int height = 0;
    private ProfileListener profileListener;
    private int additionalHeight;
    private int estimatedHeight;
    private FragmentProfileInfoBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_info, container, false);
        viewModel = new ProfileInfoViewModel("", "", "");

        binding.setModel(viewModel);
        binding.txtProfileInfoBio.addAutoLinkMode(
                AutoLinkMode.MODE_MENTION,
                AutoLinkMode.MODE_URL,
                AutoLinkMode.MODE_HASHTAG
        );

        boolean isNight = App.getInstance().isNightEnabled();

        binding.txtProfileInfoBio.setHashtagModeColor(ContextCompat.getColor(getContext(), isNight ?
                R.color.dark_tag_color : R.color.light_tag_color));
        binding.txtProfileInfoBio.setMentionModeColor(ContextCompat.getColor(getContext(), isNight ?
                R.color.dark_highlight_color : R.color.light_highlight_color));
        binding.txtProfileInfoBio.setUrlModeColor(ContextCompat.getColor(getContext(), isNight ?
                R.color.dark_highlight_color : R.color.light_highlight_color));
        binding.txtProfileInfoBio.setSelectedStateColor(ContextCompat.getColor(getContext(), isNight ?
                R.color.dark_secondary_text_color : R.color.light_secondary_text_color));

        binding.txtProfileInfoBio.setAutoLinkText(viewModel.getBio() == null ? "" : viewModel.getBio());
        binding.txtProfileInfoBio.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
            @Override
            public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                if (profileListener != null) {
                    if (autoLinkMode.equals(AutoLinkMode.MODE_MENTION)) {
                        Flags.userSource = Flags.UserSource.screenName;
                        AppData.CURRENT_SCREEN_NAME = matchedText;
                        profileListener.openActivity(new Intent(getContext(), ProfileActivity.class));
                    } else if (autoLinkMode.equals(AutoLinkMode.MODE_URL)) {
                        profileListener.openLink(matchedText.trim());
                    } else if (autoLinkMode.equals(AutoLinkMode.MODE_HASHTAG)) {
                        AppData.searchQuery = matchedText;
                        getActivity().startActivity(new Intent(getContext(), SearchActivity.class));
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }
            }

            @Override
            public void onAutoLinkLongTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                Log.e(TAG, "long click - " + matchedText);
            }
        });

        binding.txtProfileInfoLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileListener != null)
                    profileListener.openLink(binding.txtProfileInfoLink.getText().toString());
            }
        });

        binding.txtProfileInfoLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "geo:0,0?q=" + viewModel.getLocation();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });

        measureHeight();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (profileListener != null)
            profileListener.updateInfo();
    }

    private void measureHeight() {
        binding.txtProfileInfoBio.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.txtProfileInfoBio.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                height = (binding.txtProfileInfoBio.getLineHeight() * binding.txtProfileInfoBio.getLineCount());
                if ((viewModel.getLocation() != null && !viewModel.getLocation().trim().equals(""))
                        && (viewModel.getLink() != null && !viewModel.getLink().trim().equals(""))) {
                    additionalHeight = (int) Utilities.convertDpToPixel(44, getContext());
                } else if ((viewModel.getLocation() == null || viewModel.getLocation().trim().equals(""))
                        && (viewModel.getLink() == null || viewModel.getLink().trim().equals(""))) {
                    additionalHeight = 0;
                } else {
                    additionalHeight = (int) Utilities.convertDpToPixel(22, getContext());
                }

                estimatedHeight = (int) Utilities.convertDpToPixel(80, getContext());
                changeProfileHeight(false);
            }
        });
    }

    /**
     * Setup users info
     *
     * @param bio      - user's bio
     * @param link     - user's link
     * @param location - user's location
     */
    public void setViewModel(String bio, String link, String location) {
        viewModel = new ProfileInfoViewModel(bio, link, location);
        if (bio != null) {
            if (TextUtils.isEmpty(bio)) {
                binding.txtProfileInfoBio.setAutoLinkText("No Bio");
                binding.txtProfileInfoBio.setTypeface(binding.txtProfileInfoBio.getTypeface(), Typeface.ITALIC);
                binding.txtProfileInfoBio.setTextColor(getResources().getColor(R.color.dark_hint_text_color));

            } else {
                binding.txtProfileInfoBio.setAutoLinkText(bio);
            }
        }
        binding.setModel(viewModel);

        measureHeight();
        changeProfileHeight(false);
    }

    /**
     * Change height for fader in activity
     *
     * @param isAnimated - @true for animation
     */
    public void changeProfileHeight(boolean isAnimated) {
        try {
            int diff = height + additionalHeight - estimatedHeight;
            if (profileListener != null)
                profileListener.updateHeader(diff, isAnimated);
        } catch (Exception e) {
            Log.e(TAG, "exception - " + e.getLocalizedMessage());
        }
    }

    public int getHeight() {
        return height;
    }

    public void setProfileListener(ProfileListener profileListener) {
        this.profileListener = profileListener;
    }
}
