package solonsky.signal.twitter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.MuteUserAdapter;
import solonsky.signal.twitter.adapters.UserHorizontalAdapter;
import solonsky.signal.twitter.data.MuteData;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.databinding.ActivityMuteAddBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.RemoveModel;
import solonsky.signal.twitter.models.UserModel;
import solonsky.signal.twitter.viewmodels.MuteAddViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.User;

/**
 * Created by neura on 01.06.17.
 */

public class MuteAddActivity extends AppCompatActivity {
    private final String TAG = MuteAddActivity.class.getSimpleName();
    private MuteAddViewModel viewModel;
    private ActivityMuteAddBinding binding;
    private MuteAddActivity mActivity;

    /* Mentions block */
    private ArrayList<UserModel> userModels;
    private ArrayList<UserModel> filteredUserModels;
    private UserHorizontalAdapter userAdapter;

    private boolean isSelection = false;
    private boolean isTimer = false;
    private AsyncTwitter asyncTwitter;
    private ArrayList<UserModel> userModelsAdd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean isNight = App.getInstance().isNightEnabled();

        if (isNight) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_mute_add);
        viewModel = new MuteAddViewModel(getString(R.string.logged_mute_simple), false);
        mActivity = this;

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color));

        setupBottom();
        asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(twitterListener);

        binding.imgMuteAddBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.txtMuteAddKeyword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    viewModel.setList(true);
                    viewModel.setBottom(false);

                    binding.txtMuteAddKeyword.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddHashtag.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddUser.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddClient.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));

                    binding.txtMuteAddKeyword.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddHashtag.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddUser.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddClient.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                }
            }
        });

        binding.txtMuteAddHashtag.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    viewModel.setList(true);
                    viewModel.setBottom(false);

                    binding.txtMuteAddKeyword.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddHashtag.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddUser.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddClient.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));

                    binding.txtMuteAddKeyword.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddHashtag.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddUser.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddClient.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                }
            }
        });

        binding.txtMuteAddUser.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.txtMuteAddKeyword.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddHashtag.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddUser.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddClient.setTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));

                    binding.txtMuteAddKeyword.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddHashtag.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));
                    binding.txtMuteAddUser.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddClient.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_hint_text_color : R.color.light_hint_text_color
                    ));

                    filterMentions("", true);
                } else {
                    binding.txtMuteAddUser.clearFocus();
                }
            }
        });

        binding.txtMuteAddClient.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.txtMuteAddKeyword.setAlpha(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddHashtag.setAlpha(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddUser.setAlpha(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddClient.setAlpha(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));


                    binding.txtMuteAddKeyword.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddHashtag.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddUser.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));
                    binding.txtMuteAddClient.setHintTextColor(getResources().getColor(
                            isNight ? R.color.dark_primary_text_color : R.color.light_primary_text_color
                    ));

                    startActivity(new Intent(getApplicationContext(), MuteClientsActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    Utilities.hideKeyboard(mActivity);
                    finish();
                }
            }
        });

        binding.txtMuteAddKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAction();
            }
        });

        binding.txtMuteAddClient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAction();
            }
        });

        binding.txtMuteAddHashtag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAction();
            }
        });

        binding.txtMuteAddUser.addTextChangedListener(new TextWatcher() {
            Handler handler = new Handler();
            Timer timer = new Timer();
            final long DELAY = 500; // in ms

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMentions(s.toString().replace(" ", ""), true);
                checkAction();
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (isSelection) {
                    isSelection = false;
                } else {
                    isTimer = false;
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                                       @Override
                                       public void run() {
                                           AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                                           asyncTwitter.addListener(new TwitterAdapter() {
                                               @Override
                                               public void searchedUser(ResponseList<User> users) {
                                                   super.searchedUser(users);
                                                   if (!isTimer) {
                                                       for (User user : users) {
                                                           boolean hasUser = false;
                                                           for (UserModel userModel : userModels) {
                                                               if (user.getId() == userModel.getId()) {
                                                                   hasUser = true;
                                                                   break;
                                                               }
                                                           }

                                                           if (!hasUser)
                                                               userModels.add(new UserModel(user.getId(),
                                                                       user.getBiggerProfileImageURL(), user.getName(),
                                                                       "@" + user.getScreenName(), false, false, false));
                                                       }

                                                       handler.post(new Runnable() {
                                                           @Override
                                                           public void run() {
                                                               filterMentions(s.toString(), true);
                                                           }
                                                       });
                                                   }
                                               }

                                               @Override
                                               public void onException(TwitterException te, TwitterMethod method) {
                                                   super.onException(te, method);
                                                   Log.e(TAG, "Error - " + te.getLocalizedMessage());
                                               }
                                           });
                                           asyncTwitter.searchUsers(s.toString(), 0);
                                       }
                                   },
                            DELAY
                    );
                }
            }
        });

        if (!MuteData.getInstance().isCacheLoaded())
            MuteData.getInstance().loadCache();

        binding.setModel(viewModel);
        binding.setClick(new MuteAddViewModel.MuteAddClickHandler() {
            @Override
            public void onActionClick(View v) {
                if (viewModel.isAction()) {
                    if (viewModel.isList()) {
                        if (!TextUtils.isEmpty(binding.txtMuteAddKeyword.getText().toString())) {
                            RemoveModel removeModel = new RemoveModel(0, binding.txtMuteAddKeyword.getText().toString());
                            if (!MuteData.getInstance().getmKeywordsList().contains(removeModel)) {
                                MuteData.getInstance().getmKeywordsList().add(0, removeModel);
                                MuteData.getInstance().saveCache();
                                Toast.makeText(getApplicationContext(), getString(R.string.success_mute), Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (!TextUtils.isEmpty(binding.txtMuteAddHashtag.getText().toString())) {
                            RemoveModel removeModel = new RemoveModel(0, "#" + binding.txtMuteAddHashtag.getText().toString());
                            if (!MuteData.getInstance().getmHashtagsList().contains(removeModel)) {
                                MuteData.getInstance().getmHashtagsList().add(0, removeModel);
                                MuteData.getInstance().saveCache();
                                Toast.makeText(getApplicationContext(), getString(R.string.success_mute_tag)
                                        .replace("[tag]", binding.txtMuteAddHashtag.getText().toString()), Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (!TextUtils.isEmpty(binding.txtMuteAddUser.getText().toString())) {
                            RemoveModel removeModel = new RemoveModel(0, binding.txtMuteAddUser.getText().toString());
                            if (!MuteData.getInstance().getmUsersList().contains(removeModel)) {
                                MuteData.getInstance().getmHashtagsList().add(0, removeModel);
                                MuteData.getInstance().saveCache();
                                Toast.makeText(getApplicationContext(), getString(R.string.success_mute_user)
                                        .replace("[username]", binding.txtMuteAddUser.getText().toString()), Toast.LENGTH_SHORT).show();
                                asyncTwitter.createMute(binding.txtMuteAddUser.getText().toString().replace(" ", ""));
                            }
                        }
                    } else {
                        if (userModelsAdd != null && userModelsAdd.size() > 0) {
                            asyncTwitter.createMute(userModelsAdd.get(0).getId());
                            Toast.makeText(getApplicationContext(), getString(R.string.success_mute), Toast.LENGTH_SHORT).show();
                        }
                    }

                    Utilities.hideKeyboard(mActivity);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Utilities.hideKeyboard(mActivity);
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }

    TwitterAdapter twitterListener = new TwitterAdapter() {
        @Override
        public void onException(TwitterException te, TwitterMethod method) {
            super.onException(te, method);
            Log.e(TAG, "error creating mute - " + te.getLocalizedMessage());
        }

        @Override
        public void createdMute(User user) {
            super.createdMute(user);
            Log.e(TAG, getString(R.string.success_mute));
        }
    };

    private void checkAction() {
        boolean isAction = !binding.txtMuteAddClient.getText().toString().equals("") ||
                !binding.txtMuteAddHashtag.getText().toString().equals("") ||
                !binding.txtMuteAddUser.getText().toString().equals("") ||
                !binding.txtMuteAddKeyword.getText().toString().equals("");

        Log.e(TAG, "isAction - " + isAction);
        viewModel.setAction(isAction);
    }

    /**
     * Sort mentions to setup it
     *
     * @param lastWord - filter string
     */
    private void filterMentions(String lastWord, boolean isShow) {
        filteredUserModels.clear();
        for (UserModel userModel : userModels) {
            if (userModel.getTwitterName().toLowerCase().contains(lastWord.toLowerCase().replace("@", ""))) {
                filteredUserModels.add(userModel);
            }
        }

        if (filteredUserModels.size() != 0 && isShow) {
            userAdapter.notifyDataSetChanged();
            viewModel.setBottom(true);
        } else if (isShow) {
            filteredUserModels.add(new UserModel(0, "", lastWord, "@" + lastWord, true, false, false));
            userAdapter.notifyDataSetChanged();
            viewModel.setBottom(true);
        }
    }

    private void setupBottom() {
        userModels = new ArrayList<>();
        filteredUserModels = new ArrayList<>();

        for (solonsky.signal.twitter.models.User user : UsersData.getInstance().getUsersList()) {
            UserModel userModel = new UserModel(user.getId(), user.getBiggerProfileImageURL(), user.getName(),
                    "@" + user.getScreenName(), user.isFollowRequestSent(), false, false);
            userModels.add(userModel);
            filteredUserModels.add(userModel);
        }

        userAdapter = new UserHorizontalAdapter(filteredUserModels, getApplicationContext(), mActivity,
                new UserHorizontalAdapter.UserClickHandler() {
                    @Override
                    public void onItemClick(UserModel model, View v) {
                        userModelsAdd = new ArrayList<>();
                        userModelsAdd.add(model);
                        binding.txtMuteAddUser.clearFocus();
                        viewModel.setAction(true);
                        viewModel.setBottom(false);

                        isSelection = true;
                        isTimer = true;

                        MuteUserAdapter adapter = new MuteUserAdapter(userModelsAdd, getApplicationContext(),
                                mActivity, new MuteUserAdapter.MuteUserClickHandler() {
                            @Override
                            public void onItemClick(UserModel model, View v) {
                                Toast.makeText(getApplicationContext(), getString(R.string.success_mute), Toast.LENGTH_SHORT).show();
                                asyncTwitter.createMute(model.getId());
                                Utilities.hideKeyboard(mActivity);
                                finish();
                            }
                        });

                        Utilities.hideKeyboard(mActivity);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(),
                                LinearLayoutManager.VERTICAL, true);
                        binding.recyclerMuteSelected.setLayoutManager(linearLayoutManager);
                        binding.recyclerMuteSelected.setHasFixedSize(true);
                        binding.recyclerMuteSelected.setAdapter(adapter);
                        viewModel.setList(false);
                    }

                    @Override
                    public void onSearchClick(UserModel model, View v) {

                    }
                });

        binding.recyclerMute.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerMute.setAdapter(userAdapter);
        binding.recyclerMute.scrollToPosition(0);
    }
}
