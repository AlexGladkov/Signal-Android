package solonsky.signal.twitter.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import it.sephiroth.android.library.easing.Back;
import oauth.signpost.http.HttpResponse;
import retrofit2.Retrofit;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.ActivitySupportBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.http.Backend;
import solonsky.signal.twitter.libs.http.RetrofitWorker;
import solonsky.signal.twitter.viewmodels.SupportViewModel;

/**
 * Created by neura on 23.05.17.
 */

public class SupportActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private final String TAG = "SUPPORTACTIVITY";
    private SupportViewModel viewModel;
    private ActivitySupportBinding binding;
    private BillingProcessor bp;
    private final String TEST_IN_APP = "android.test.purchased";
    private final String TIER_1 = "tier_1";
    private final String TIER_2 = "tier_2";
    private final String TIER_3 = "tier_3";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        final SupportActivity mActivity = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_support);
        viewModel = new SupportViewModel(getStringCount(0), getStringCount(0),
                getStringCount(0));

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.parseColor("#33000000"));

        binding.imgSupportBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        bp = new BillingProcessor(this, getString(R.string.billing_key),
                getString(R.string.merchant_id),this);
        bp.initialize();

        for (String productId : bp.listOwnedProducts()) {
            switch (productId) {
                case TIER_1:
                    viewModel.setTip1(true);
                    viewModel.setStar1(true);
                    binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                    break;

                case TIER_2:
                    viewModel.setTip1(true);
                    viewModel.setTip2(true);
                    viewModel.setStar1(false);
                    viewModel.setStar2(true);
                    binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                    break;

                case TIER_3:
                    viewModel.setTip1(true);
                    viewModel.setTip2(true);
                    viewModel.setTip3(true);
                    viewModel.setStar1(false);
                    viewModel.setStar2(false);
                    viewModel.setStar3(true);
                    binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                    break;

                case TEST_IN_APP:
                    viewModel.setTip1(true);
                    viewModel.setTip2(true);
                    viewModel.setTip3(true);
                    viewModel.setStar1(false);
                    viewModel.setStar2(false);
                    viewModel.setStar3(true);
                    binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                    break;
            }
        }

        binding.setModel(viewModel);
        binding.setClick(new SupportViewModel.SupportClickHandler() {
            @Override
            public void onFirstTipClick(View v) {
                if (viewModel.isTip1()) {
                    Utilities.openLink("https://goo.gl/forms/MlpzvB9onY7V14on2", mActivity);
                } else {
                    onCoffeeClick(v);
                }
            }

            @Override
            public void onSecondTipClick(View v) {
                if (viewModel.isTip2()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "support@getsignal.co", null));

                    int codeVersion = 0;
                    String version = "";
                    try {
                        PackageInfo pInfo = mActivity.getPackageManager().getPackageInfo(getPackageName(), 0);
                        version = pInfo.versionName;
                        codeVersion = pInfo.versionCode;
                    } catch (PackageManager.NameNotFoundException e) {
                        //Do nothing
                    }

                    Locale current = getResources().getConfiguration().locale;
                    String deviceName = getDeviceName() + " (" + Build.VERSION.RELEASE + ")";
                    String appVersion = version + " (" + codeVersion + ")";
                    String locale = current.getDisplayLanguage();
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Signal · Priority Support");
                    intent.putExtra(Intent.EXTRA_TEXT, "\n\n\n\n\n---\n\nDeveloper Support Info\n\n • Device: "
                            + deviceName + "\n • App Version: " + appVersion + "\n • Locale: " + locale);
                    mActivity.startActivity(intent);
                } else {
                    onBeerClick(v);
                }
            }

            @Override
            public void onThirdTipClick(View v) {
                if (viewModel.isTip3()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "support@getsignal.co", null));

                    int codeVersion = 0;
                    String version = "";
                    try {
                        PackageInfo pInfo = mActivity.getPackageManager().getPackageInfo(getPackageName(), 0);
                        version = pInfo.versionName;
                        codeVersion = pInfo.versionCode;
                    } catch (PackageManager.NameNotFoundException e) {
                        //Do nothing
                    }

                    Locale current = getResources().getConfiguration().locale;
                    String deviceName = getDeviceName() + " (" + Build.VERSION.RELEASE + ")";
                    String appVersion = version + " (" + codeVersion + ")";
                    String locale = current.getDisplayLanguage();
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Signal · Join Beta");
                    intent.putExtra(Intent.EXTRA_TEXT, "\n\n\n\n\n---\n\nDeveloper Support Info\n\n • Device: "
                            + deviceName + "\n • App Version: " + appVersion + "\n • Locale: " + locale);
                    mActivity.startActivity(intent);
                } else {
                    onCakeClick(v);
                }
            }

            @Override
            public void onRestoreClick(View v) {
                if (BillingProcessor.isIabServiceAvailable(getApplicationContext())) {
                    bp.loadOwnedPurchasesFromGoogle();
                    for (String productId : bp.listOwnedProducts()) {
                        switch (productId) {
                            case TIER_1:
                                viewModel.setTip1(true);
                                viewModel.setStar1(true);
                                binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                                loadButtonsStyle();
                                break;

                            case TIER_2:
                                viewModel.setTip1(true);
                                viewModel.setTip2(true);
                                viewModel.setStar1(false);
                                viewModel.setStar2(true);
                                binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                                loadButtonsStyle();
                                break;

                            case TIER_3:
                                viewModel.setTip1(true);
                                viewModel.setTip2(true);
                                viewModel.setTip3(true);
                                viewModel.setStar1(false);
                                viewModel.setStar2(false);
                                viewModel.setStar3(true);
                                binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                                loadButtonsStyle();
                                break;

                            case TEST_IN_APP:
                                viewModel.setTip1(true);
                                viewModel.setTip2(true);
                                viewModel.setTip3(true);
                                viewModel.setStar1(false);
                                viewModel.setStar2(false);
                                viewModel.setStar3(true);
                                binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                                loadButtonsStyle();
                                break;
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error_billing_support, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onReviewClick(View v) {
                Utilities.openLink("https://play.google.com/store/apps/details?id=solonsky.signal.twitter", mActivity);
            }

            @Override
            public void onCoffeeClick(View v) {
                if (BillingProcessor.isIabServiceAvailable(getApplicationContext())) {
                    bp.purchase(mActivity, TIER_1);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error_billing_support, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBeerClick(View v) {
                if (BillingProcessor.isIabServiceAvailable(getApplicationContext())) {
                    bp.purchase(mActivity, TIER_2);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error_billing_support, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCakeClick(View v) {
                if (BillingProcessor.isIabServiceAvailable(getApplicationContext())) {
                    bp.purchase(mActivity, TIER_3);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error_billing_support, Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadTipsCount();
        loadButtonsStyle();
    }

    @Override
    protected void onDestroy() {
        if (bp != null) {
            bp.release();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }

    private String getStringCount(int count) {
        return String.valueOf(count) + " " + getString(R.string.support_users_tipped);
    }

    private void loadTipsCount() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Build and set timeout values for the request.
                    URLConnection connection = (new URL("http://getsignal.co/links/tips.txt")).openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.connect();
                    // Read and store the result line by line then return the entire string.
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder html = new StringBuilder();
                    int i = 0;
                    for (String line; (line = reader.readLine()) != null; ) {
                        line = line.replace(" tipped", "\ntipped");
                        Log.e(TAG, "line - " + line);
                        switch (i) {
                            case 0:
                                final String finalLine2 = line;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewModel.setTips_1(finalLine2);
                                    }
                                });
                                break;

                            case 1:
                                final String finalLine = line;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewModel.setTips_2(finalLine);
                                    }
                                });
                                break;

                            case 2:
                                final String finalLine1 = line;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewModel.setTips_3(finalLine1);
                                    }
                                });
                                break;
                        }

                        i = i + 1;
                        html.append(line);
                    }
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error - " + e.getLocalizedMessage());
                }
            }
        }).start();
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void loadButtonsStyle() {
        boolean isNight = App.getInstance().isNightEnabled();
        binding.btnSupportTip3.setBackgroundResource(viewModel.isTip3() ? isNight ?
                R.drawable.dark_active_support_button : R.drawable.light_active_support_button :
                isNight ? R.drawable.dark_inactive_support_button : R.drawable.light_inactive_support_button);

        binding.btnSupportTip2.setBackgroundResource(viewModel.isTip2() ? isNight ?
                R.drawable.dark_active_support_button : R.drawable.light_active_support_button :
                isNight ? R.drawable.dark_inactive_support_button : R.drawable.light_inactive_support_button);

        binding.btnSupportTip1.setBackgroundResource(viewModel.isTip1() ? isNight ?
                R.drawable.dark_active_support_button : R.drawable.light_active_support_button :
                isNight ? R.drawable.dark_inactive_support_button : R.drawable.light_inactive_support_button);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Billing section starts here
     */

    private boolean hasProductId(String productId) {
        return bp.listOwnedProducts().contains(productId);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        startActivity(new Intent(getApplicationContext(), ThanksActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        switch (productId) {
            case TIER_1:
                viewModel.setTip1(true);
                viewModel.setStar1(true);
                loadButtonsStyle();
                break;

            case TIER_2:
                viewModel.setTip1(true);
                viewModel.setTip2(true);
                viewModel.setStar1(false);
                viewModel.setStar2(true);
                loadButtonsStyle();
                break;

            case TIER_3:
                viewModel.setTip1(true);
                viewModel.setTip2(true);
                viewModel.setTip3(true);
                viewModel.setStar1(false);
                viewModel.setStar2(false);
                viewModel.setStar3(true);
                loadButtonsStyle();
                break;

            case TEST_IN_APP:
                viewModel.setTip1(true);
                viewModel.setTip2(true);
                viewModel.setTip3(true);
                viewModel.setStar1(false);
                viewModel.setStar2(false);
                viewModel.setStar3(true);
                loadButtonsStyle();
                break;
        }

        binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
    }

    @Override
    public void onPurchaseHistoryRestored() {
        for (String productId : bp.listOwnedProducts()) {
            switch (productId) {
                case TIER_1:
                    viewModel.setTip1(true);
                    viewModel.setStar1(true);
                    binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                    loadButtonsStyle();
                    break;

                case TIER_2:
                    viewModel.setTip1(true);
                    viewModel.setTip2(true);
                    viewModel.setStar1(false);
                    viewModel.setStar2(true);
                    binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                    loadButtonsStyle();
                    break;

                case TIER_3:
                    viewModel.setTip1(true);
                    viewModel.setTip2(true);
                    viewModel.setTip3(true);
                    viewModel.setStar1(false);
                    viewModel.setStar2(false);
                    viewModel.setStar3(true);
                    binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                    loadButtonsStyle();
                    break;

                case TEST_IN_APP:
                    viewModel.setTip1(true);
                    viewModel.setTip2(true);
                    viewModel.setTip3(true);
                    viewModel.setStar1(false);
                    viewModel.setStar2(false);
                    viewModel.setStar3(true);
                    binding.txtSupportMainText.setText(getString(R.string.support_main_subtitle_after));
                    loadButtonsStyle();
                    break;
            }
        }
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
//        Toast.makeText(getApplicationContext(), "Billing error - " + errorCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBillingInitialized() {
        Log.e(TAG, "billing init");
    }
}
