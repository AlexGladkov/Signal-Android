package solonsky.signal.twitter.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Permission;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.helpers.drafts.DraftModel;
import solonsky.signal.twitter.helpers.drafts.Drafts;
import solonsky.signal.twitter.models.*;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.presenters.ComposePresenter;
import solonsky.signal.twitter.views.ComposeView;
import twitter4j.*;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonElement;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pl.aprilapps.easyphotopicker.EasyImage;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.DropComposeAdapter;
import solonsky.signal.twitter.adapters.ImageHorizontalAdapter;
import solonsky.signal.twitter.databinding.ActivityComposeBinding;
import solonsky.signal.twitter.fragments.DraftsFragment;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.EasyImageConfig;
import solonsky.signal.twitter.viewmodels.ComposeViewModel;
import twitter4j.Status;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by neura on 22.05.17.
 */
public class ComposeActivity extends MvpAppCompatActivity
        implements ComposeView {

    @InjectPresenter
    public ComposePresenter mPresenter;

    private static final int REQUEST_LOCATION_CODE = 2;
    private final String TAG = ComposeActivity.class.getSimpleName();
    private static final int REQUEST_LIBRARY_CODE = 0;
    private static final int REQUEST_CAMERA_CODE = 1;

    public ActivityComposeBinding binding;
    private ComposeViewModel viewModel;
    private PopupWindow popupWindow;
    private ComposeActivity mActivity;

    private int selectCount = 1;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation = null;

    private boolean isSelection = false;
    private ImageHorizontalAdapter imagesAdapter;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        mActivity = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_compose);
        mediaPlayer = MediaPlayer.create(this, R.raw.send);

        EasyImage.configuration(this)
                .setAllowMultiplePickInGallery(true)
                .setImagesFolderName(getString(R.string.app_name))
                .setCopyTakenPhotosToPublicGalleryAppFolder(true);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color));

        String title = getString(R.string.compose_default_title);

        if (Flags.CURRENT_COMPOSE == Flags.COMPOSE_QUOTE) {
            title = getString(R.string.compose_title_quote);
        } else if (Flags.CURRENT_COMPOSE == Flags.COMPOSE_REPLY) {
            title = getString(R.string.compose_title_reply);
        }

        /* Setup mentions panel */
        ArrayList<UserModel> mentionsSource = new ArrayList<>();
//        for (solonsky.signal.twitter.models.User user : UsersData.getInstance().getUsersList()) {
//            mentionsSource.add(new UserModel(user.getId(), user.getOriginalProfileImageURL(),
//                    user.getName(), "@" + user.getScreenName(), false, false, false));
//        }

        viewModel = new ComposeViewModel(title, AppData.ME.getOriginalProfileImageURL(),
                AppData.ME.getLocation(), ComposeActivity.this, mentionsSource);

        checkQuote();
        checkReply();
        checkLink();
        checkTag();
        checkDrafts();
        setupComposeListener();

        binding.setCompose(viewModel);
        binding.setClick(new ComposeViewModel.ComposeClickHandler() {
            @Override
            public void onUserClick(final View view) {
                ArrayList<DropDownComposeItems> mUsersList = new ArrayList<>();
                for (ConfigurationUserModel configurationUserModel : AppData.configurationUserModels) {
                    solonsky.signal.twitter.models.User user = configurationUserModel.getUser();
                    mUsersList.add(new DropDownComposeItems(user.getId(), user.getOriginalProfileImageURL(),
                            "@" + user.getScreenName(), configurationUserModel.getClientToken(),
                            configurationUserModel.getClientSecret()));
                }

                DropComposeAdapter adapter = new DropComposeAdapter(getApplicationContext(),
                        App.getInstance().isNightEnabled() ? R.layout.dark_menu_compose : R.layout.light_menu_compose,
                        mUsersList, new DropComposeAdapter.MenuClickListener() {
                    @Override
                    public void onMenuClick(long id, String imageUrl, View v,
                                            String clientSecret, String clientToken) {
                        viewModel.setAvatarUrl(imageUrl);
                        viewModel.setClientSecret(clientSecret);
                        viewModel.setClientToken(clientToken);

                        if (popupWindow != null && popupWindow.isShowing()) {
                            popupWindow.dismiss();
                            popupWindow = null;
                        }
                    }
                });

                ListView listViewSort = new ListView(mActivity);
                listViewSort.setDivider(null);
                listViewSort.setDividerHeight(0);
                listViewSort.setAdapter(adapter);

                popupWindow = new PopupWindow(mActivity);

                popupWindow.setFocusable(true);
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                popupWindow.setWidth((int) Utilities.convertDpToPixel(200, getApplicationContext()));
                popupWindow.setElevation(Utilities.convertDpToPixel(8, getApplicationContext()));
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setContentView(listViewSort);

                popupWindow.showAsDropDown(view,
                        (int) -Utilities.convertDpToPixel(4.25f, getApplicationContext()),
                        (int) -Utilities.convertDpToPixel(4, getApplicationContext()));
            }

            @Override
            public void onSendClick(View view) {
                if (!viewModel.isFragment())
                    sendTweet();
            }

            @Override
            public void onGeoClick(final View view) {
                if (!viewModel.isFragment()) {
                    if (viewModel.getLocationState() == ComposeViewModel.LOCATION_ON) {
                        viewModel.setLocationState(ComposeViewModel.LOCATION_OFF);
                    } else {
                        if (!Permission.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION))
                            ActivityCompat.requestPermissions(ComposeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
                        else {
                            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
                            mFusedLocationClient.getLastLocation().addOnCompleteListener(mActivity, new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    currentLocation = task.getResult();
                                    viewModel.addLocation(task.getResult());
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onMentionsClick(View view) {
                if (!viewModel.isFragment()) {
                    String text = binding.txtComposeText.getText().toString();
                    binding.txtComposeText.setText(Utilities.checkLastCharIsSpace(text) ?
                            text + "@" : text.length() == 0 ? text + "@" : text + " @");
                    binding.txtComposeText.setSelection(binding.txtComposeText.getText().toString().length());
                }
            }

            @Override
            public void onCameraClick(View view) {
                if (!viewModel.isFragment())
                    setupUpload(view);
            }

            @Override
            public void onHashClick(View view) {
                if (!viewModel.isFragment()) {
                    String tweetText = binding.txtComposeText.getText().toString();
                    char lastChar = tweetText.length() == 0 ? 'a' : tweetText.charAt(tweetText.length() - 1);
                    if (lastChar == ' ' || lastChar == 'a') {
                        binding.txtComposeText.setText(tweetText + "#");
                    } else {
                        binding.txtComposeText.setText(tweetText + " #");
                    }

                    binding.txtComposeText.setSelection(binding.txtComposeText.getText().toString().length());
                }
            }

            @Override
            public void onDraftsClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_compose_drafts, new DraftsFragment()).commit();
                viewModel.setFragment(!viewModel.isFragment());
                resetCamera();
            }

            @Override
            public void onBackClick(View view) {
                if (viewModel.isFragment()) {
                    viewModel.setFragment(false);
                } else {
                    saveDraft();
                    finish();
                    Utilities.hideKeyboard(mActivity);
                    overridePendingTransition(R.anim.slide_out_no_animation, R.anim.fade_out);
                }
            }

            @Override
            public boolean onCameraLongClick(View view) {
                Utilities.hideKeyboard(mActivity);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Permission.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            EasyImage.openCamera(mActivity, EasyImageConfig.REQ_TAKE_PICTURE);
                        } else {
                            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Permission.PHOTO_REQUEST);
                        }
                    }
                }, 200);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (viewModel.isFragment()) {
            viewModel.setFragment(false);
        } else if (viewModel.isShowPhoto()) {
            viewModel.setShowPhoto(false);
            viewModel.setLibrary(true);
        } else {
            saveDraft();
            finish();
            Utilities.hideKeyboard(mActivity);
            overridePendingTransition(R.anim.slide_out_no_animation, R.anim.fade_out);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        binding.txtComposeText.requestFocus();
        Utilities.showKeyboard(binding.txtComposeText);
        switch (requestCode) {
            case Permission.LOCATION_REQUEST: {
                if (grantResults.length > 0) {
                    boolean fineLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean coarseLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (fineLocation || coarseLocation) {
                        viewModel.setLocationState(ComposeViewModel.LOCATION_ON);

                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                        PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        mFusedLocationClient.getLastLocation().addOnCompleteListener(mActivity, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                currentLocation = task.getResult();
                                viewModel.addLocation(task.getResult());
                            }
                        });
                    } else {
                        viewModel.setLocationState(ComposeViewModel.LOCATION_DISABLED);
                    }
                } else {
                    viewModel.setLocationState(ComposeViewModel.LOCATION_DISABLED);
                }

                return;
            }

            case Permission.PHOTO_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.loadRecentMedia();
                    setupUpload(binding.getRoot());
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LIBRARY_CODE && resultCode == RESULT_OK) {
            List<String> mImages = new ArrayList<>();
            List<String> mPathes = Matisse.obtainPathResult(data);
            int hasGif = -1;
            int hasVideo = -1;

            for (String path : mPathes) {
                switch (path.substring(path.lastIndexOf("."))) {
                    case ".mp4":
                    case ".avi":
                        if (hasVideo == -1) hasVideo = mPathes.indexOf(path);
                        break;

                    case ".gif":
                        if (hasGif == -1) hasGif = mPathes.indexOf(path);
                        break;

                    default:
                        mImages.add(path);
                        break;
                }

//                if (hasGif > -1 || hasVideo > -1) break;
            }

            if (hasGif > -1 || hasVideo > -1) {
                if (viewModel.getImagesSource().size() > 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_multiple_image), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mImages.size() > 0)
                    Toast.makeText(getApplicationContext(), getString(R.string.error_multiple_image_add), Toast.LENGTH_SHORT).show();

                File file = new File(mPathes.get(0));
                if (hasGif == 0) {
                    viewModel.setHasGif(true);
                    binding.recyclerComposeImage.scrollToPosition(viewModel.addMedia(file, Flags.MEDIA_TYPE.GIF));
                    return;
                }

                if (hasVideo == 0) {
                    viewModel.setHasVideo(true);
                    binding.recyclerComposeImage.scrollToPosition(viewModel.addMedia(file, Flags.MEDIA_TYPE.VIDEO));
                    return;
                }

                if (mImages.size() > 0) {
                    for (String path : mImages) {
                        File imageFile = new File(path);
                        binding.recyclerComposeImage.scrollToPosition(viewModel.addMedia(imageFile, Flags.MEDIA_TYPE.IMAGE));
                    }
                }
            } else {
                for (String path : mPathes) {
                    File file = new File(path);
                    viewModel.setHasGif(hasGif > -1);
                    viewModel.setHasVideo(hasVideo > -1);
                    binding.recyclerComposeImage.scrollToPosition(viewModel.addMedia(file, Flags.MEDIA_TYPE.IMAGE));
                }
            }
        }

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {
                if (source.equals(EasyImage.ImageSource.CAMERA) && imageFiles.size() > 0) {
                    if (viewModel.isHasGif()) return;
                    if (viewModel.isHasVideo()) return;

                    binding.recyclerComposeImage.scrollToPosition(viewModel.addMedia(imageFiles.get(0), Flags.MEDIA_TYPE.IMAGE));
                }
            }

            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                super.onImagePickerError(e, source, type);
                Toast.makeText(getApplicationContext(), getString(R.string.photo_loading_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDraft() {
        if (viewModel.getCurrentTweetText().equals("")) return;
        if (viewModel.isFromDraft()) return;

        Drafts drafts = new Drafts(getApplicationContext(), AppData.ME.getId());
        ArrayList<String> urls = new ArrayList<>();

        for (File file : viewModel.getImagesSource()) {
            urls.add(file.getAbsolutePath());
        }

        DraftModel draft = new DraftModel("", binding.txtComposeText.getText().toString(), urls);
        drafts.Save(draft);
        Toast.makeText(getApplicationContext(), getString(R.string.success_draft_save), Toast.LENGTH_SHORT).show();
    }

    /**
     * Performs send tweet to Twitter
     */
    private void sendTweet() {
        if (viewModel.isEnabled()) {
            viewModel.setEnabled(false);
            final Handler handler = new Handler();
            final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
            configurationBuilder.setDebugEnabled(true)
                    .setOAuthConsumerKey(AppData.CONSUMER_KEY)
                    .setOAuthConsumerSecret(AppData.CONSUMER_SECRET)
                    .setOAuthAccessToken(viewModel.getClientToken())
                    .setOAuthAccessTokenSecret(viewModel.getClientSecret());
            final ConfigurationBuilder sendBuilder = new ConfigurationBuilder();
            sendBuilder.setDebugEnabled(true)
                    .setOAuthConsumerKey(AppData.CONSUMER_KEY)
                    .setOAuthConsumerSecret(AppData.CONSUMER_SECRET)
                    .setOAuthAccessToken(viewModel.getClientToken())
                    .setOAuthAccessTokenSecret(viewModel.getClientSecret());
            final AsyncTwitter asyncTwitter = new AsyncTwitterFactory(sendBuilder.build()).getInstance();
            asyncTwitter.addListener(new TwitterAdapter() {
                @Override
                public void updatedStatus(final Status status) {
                    super.updatedStatus(status);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewModel.setEnabled(true);
                            finish();
                        }
                    });
                }

                @Override
                public void onException(TwitterException te, TwitterMethod method) {
                    super.onException(te, method);
                    Log.e(TAG, "Here is a problem - " + te.getLocalizedMessage());
                    viewModel.setEnabled(true);
                }
            });

            final StatusUpdate statusUpdate = new StatusUpdate(
                    Flags.CURRENT_COMPOSE == Flags.COMPOSE_QUOTE ?
                            binding.txtComposeText.getText().toString() + " https://twitter.com/" +
                                    AppData.CURRENT_STATUS_MODEL.getUser().getScreenName() + "/status/" +
                                    AppData.CURRENT_STATUS_MODEL.getId()
                            : binding.txtComposeText.getText().toString());

            if (Flags.CURRENT_COMPOSE == Flags.COMPOSE_REPLY) {
                statusUpdate.setInReplyToStatusId(AppData.CURRENT_STATUS_MODEL.getId());
            }

            if (viewModel.getImagesSource() != null && !viewModel.getImagesSource().isEmpty()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Twitter twitter = new TwitterFactory(configurationBuilder.build()).getInstance();
                        long[] mediaIds = new long[viewModel.getImagesSource().size()];

                        finish();
                        for (int i = 0; i < viewModel.getImagesSource().size(); i++) {
                            UploadedMedia media = null;
                            try {
                                media = twitter.uploadMedia(viewModel.getImagesSource().get(i));
                            } catch (TwitterException e) {
                                Log.e(TAG, "Error while loading image - " + e.getLocalizedMessage());
                            }

                            if (media != null)
                                mediaIds[i] = media.getMediaId();
                        }

                        statusUpdate.setMediaIds(mediaIds);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (currentLocation != null)
                                    statusUpdate.setLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
                                asyncTwitter.updateStatus(statusUpdate);
                            }
                        });
                    }
                }).start();
            } else {
                if (currentLocation != null) {
                    statusUpdate.setLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
                }

                asyncTwitter.updateStatus(statusUpdate);
                finish();
            }

            Utilities.hideKeyboard(this);
            if (AppData.appConfiguration.isSounds()) {
                mediaPlayer.start();
            }
        }
    }

    /**
     * Setup compose listener to check @mentions and #hashtags
     */
    private void setupComposeListener() {
        binding.txtComposeText.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Timer timer = new Timer();

            @Override
            public void afterTextChanged(final Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if (!isSelection) {
                    final String[] strings = s.toString().split("\\s+");
                    final String lastWord = strings.length == 0 ? "" : strings[strings.length - 1];
                    final long delay = lastWord.equals("@") || lastWord.equals("#") ? 0 : 500;

                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (strings.length > 0 && !Utilities.checkLastCharIsSpace(s.toString())) {
                                if (lastWord.contains("@")) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            viewModel.filterMentions(lastWord, true);
                                        }
                                    });
                                } else if (lastWord.contains("#")) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            viewModel.filterHashtags(lastWord, true);
                                        }
                                    });
                                } else {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            viewModel.setShowHashtags(false);
                                            viewModel.setShowMentions(false);
                                        }
                                    });
                                }
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewModel.setShowHashtags(false);
                                        viewModel.setShowMentions(false);
                                    }
                                });
                            }
                        }
                    }, delay);
                }
            }
        });
    }

    /**
     * Performs check for quote visibility
     */
    private void checkQuote() {
        if (Flags.CURRENT_COMPOSE == Flags.COMPOSE_QUOTE) {
            viewModel.setShowQuote(true);

            binding.txtComposeQuoteTitle.setText(AppData.CURRENT_TWEET_MODEL == null ?
                    AppData.CURRENT_STATUS_MODEL.getUser().getName() : AppData.CURRENT_TWEET_MODEL.getUsername());
            binding.txtComposeQuoteSubtitle.setText(" @" + AppData.CURRENT_STATUS_MODEL.getUser().getScreenName());
            binding.txtComposeQuoteText.setText(AppData.CURRENT_TWEET_MODEL == null ?
                    AppData.CURRENT_STATUS_MODEL.getText() : AppData.CURRENT_TWEET_MODEL.getText());

            for (JsonElement jsonElement : AppData.CURRENT_STATUS_MODEL.getUrlEntities()) {
                binding.txtComposeQuoteText.setText(binding.txtComposeQuoteText.getText().toString()
                        .replace(jsonElement.getAsJsonObject().get("expandedURL").getAsString(),
                                jsonElement.getAsJsonObject().get("displayURL").getAsString()));
            }

            for (JsonElement jsonElement : AppData.CURRENT_STATUS_MODEL.getMediaEntities()) {
                binding.txtComposeQuoteText.setText(binding.txtComposeQuoteText.getText().toString()
                        + " " + jsonElement.getAsJsonObject().get("mediaURL").getAsString());
            }

            binding.imgComposeQuoteImage.setVisibility(View.GONE);
        }
    }

    /**
     * Performs link add to compose
     */
    private void checkLink() {
        if (Flags.CURRENT_COMPOSE == Flags.COMPOSE_LINK) {
            viewModel.setTweetText(AppData.COMPOSE_LINK + " ");
        }
    }

    private void checkTag() {
        if (Flags.CURRENT_COMPOSE == Flags.COMPOSE_HASHTAG) {
            viewModel.setTweetText(AppData.COMPOSE_HASHTAG + " ");
            binding.txtComposeText.setSelection(0);
        }
    }

    /**
     * Performs check for reply visibility
     */
    @SuppressLint("SetTextI18n")
    private void checkReply() {
        if (Flags.CURRENT_COMPOSE == Flags.COMPOSE_REPLY) {
            viewModel.setShowReply(true);
            viewModel.setTweetText("@" + AppData.CURRENT_STATUS_MODEL.getUser().getScreenName() + " ");

            binding.txtComposeReplyTitle.setText(AppData.CURRENT_STATUS_MODEL.getUser().getName());
            binding.txtComposeReplyText.setText(AppData.CURRENT_TWEET_MODEL == null ?
                    AppData.CURRENT_STATUS_MODEL.getText() : AppData.CURRENT_TWEET_MODEL .getText());
        } else if (Flags.CURRENT_COMPOSE == Flags.COMPOSE_MENTION) {
            viewModel.setTweetText(AppData.COMPOSE_MENTION + " ");
        }
    }

    /**
     * Setup upload images and gallery, camera picker
     *
     * @param v - View to show popup
     */
    private void setupUpload(View v) {
        if (viewModel.isHasGif() || viewModel.isHasVideo() || viewModel.getImagesSource().size() == 4) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_multiple_image), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!viewModel.isLibrary()) {
            PopupMenu popupMenu = new PopupMenu(mActivity, v, 0, 0, R.style.popup_menu);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.menu_compose_camera, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.compose_camera:
                            EasyImage.openCamera(mActivity, EasyImageConfig.REQ_TAKE_PICTURE);
                            break;

                        case R.id.compose_library:
                            Matisse.from(ComposeActivity.this)
                                    .choose(MimeType.ofAll())
                                    .countable(true)
                                    .maxSelectable(4)
                                    .imageEngine(new PicassoEngine())
                                    .forResult(REQUEST_LIBRARY_CODE);
                            break;
                    }
                    return false;
                }
            });

            popupMenu.show();
        } else {
            if (Permission.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                viewModel.setLibrary(false);
                viewModel.setShowPhoto(true);
            } else {
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Permission.PHOTO_REQUEST);
            }
        }
    }

    /**
     * Close camera
     */
    public void resetCamera() {
        viewModel.setLibrary(true);
        viewModel.setShowPhoto(false);
    }

    private void checkDrafts() {
        final boolean isNight = App.getInstance().isNightEnabled();
        Drafts drafts = new Drafts(getApplicationContext(), AppData.ME.getId());
        drafts.loadAll(new ReservoirGetCallback<List<DraftModel>>() {
            @Override
            public void onFailure(Exception e) {
                binding.btnComposeDrafts.setImageDrawable(getResources().getDrawable(R.drawable.ic_icons_compose_drafts_empty));
            }

            @Override
            public void onSuccess(List<DraftModel> drafts) {
                binding.btnComposeDrafts.setImageDrawable(getResources().getDrawable(
                        drafts.size() > 0 ? R.drawable.ic_icons_compose_drafts :
                                R.drawable.ic_icons_compose_drafts_empty));
            }
        });
    }

    public void setupDraft(String message) {
        viewModel.setFragment(false);
        viewModel.setFromDraft(true);
        viewModel.setTweetText(message);
    }

    // MARK: - View implementation
    @Override
    public void setupMentions(@NonNull List<UserModel> data) {
        viewModel.setMentions(data);
    }
}
