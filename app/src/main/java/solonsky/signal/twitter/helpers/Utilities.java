package solonsky.signal.twitter.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Random;

import solonsky.signal.twitter.data.MuteData;
import solonsky.signal.twitter.models.RemoveModel;
import solonsky.signal.twitter.models.StatusModel;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by neura on 21.05.17.
 * Helper class for different template operations through project
 */
public class Utilities {
    private static final String TAG = Utilities.class.getSimpleName();

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        try {
            Bitmap output;
            Rect srcRect, dstRect;
            float r;
            final int width = bitmap.getWidth();
            final int height = bitmap.getHeight();

            if (width > height){
                output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
                int left = (width - height) / 2;
                int right = left + height;
                srcRect = new Rect(left, 0, right, height);
                dstRect = new Rect(0, 0, height, height);
                r = height / 2;
            }else{
                output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
                int top = (height - width)/2;
                int bottom = top + width;
                srcRect = new Rect(0, top, width, bottom);
                dstRect = new Rect(0, 0, width, width);
                r = width / 2;
            }

            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(r, r, r, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

            bitmap.recycle();

            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }


    /**
     * This method hide soft keyboard
     *
     * @param activity - calling activity
     */
    public static void hideKeyboard(Activity activity) {
        try {
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void showKeyboard(final EditText editText) {
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(editText, 0);
            }
        }, 50);
    }

    /**
     * Parse followers count to K for thousands or M for millions
     *
     * @param followersCount - input followersCount
     * @param text           - added text
     * @return parsed string
     */
    public static String parseFollowers(long followersCount, String text) {
        if (followersCount < 1000) {
            return String.valueOf(followersCount) + " " + text;
        } else if (followersCount >= 1000 && followersCount < 1000000) {
            return String.valueOf(new DecimalFormat("#.#").format((float) followersCount / 1000f)) + "K " + text;
        } else {
            return String.valueOf(new DecimalFormat("#.#").format((float) followersCount / 1000000f)) + "M " + text;
        }
    }

    /**
     * A method to find height of the status bar
     *
     * @param appCompatActivity - context
     * @return height
     */
    public static int getStatusBarHeight(AppCompatActivity appCompatActivity) {
        int result = 0;
        int resourceId = appCompatActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = appCompatActivity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static boolean validateUrls(String text) {
        return text.contains("instagram.com") || text.contains("https://youtube");
    }

    public static JsonObject createMediaFromUrl(JsonObject urlEntity) {
        JsonObject mediaEntity = new JsonObject();
        mediaEntity.addProperty("mediaURL", urlEntity.get("expandedURL").getAsString());
        mediaEntity.addProperty("mediaURLHttps", urlEntity.get("expandedURL").getAsString());
        mediaEntity.addProperty("type", "youtube");
        return mediaEntity;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static String getRtText(boolean isRetweet, boolean isRetweetedByMe, long retweetCount, String username) {
        if (isRetweet && isRetweetedByMe) {
            if (retweetCount > 2) {
                return username + " " + Localization.AND_YOU;
            } else {
                return username + " " + Localization.AND_YOU;
            }
        } else if (isRetweet) {
            if (retweetCount > 1) {
                return username;
            } else {
                return username;
            }
        } else if (isRetweetedByMe) {
            if (retweetCount > 1) {
                return Localization.YOU;
            } else {
                return Localization.YOU;
            }
        } else {
            return "";
        }
    }

    public static void openLink(String link, Activity mActivity) {
        if (!link.startsWith("http://") && !link.startsWith("https://"))
            link = "http://" + link;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        mActivity.startActivity(browserIntent);
    }

    public static int getAverageColor(Bitmap bitmap) {
        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;
        int pixelCount = 0;

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int c = bitmap.getPixel(x, y);

                pixelCount++;
                redBucket += Color.red(c);
                greenBucket += Color.green(c);
                blueBucket += Color.blue(c);
                // does alpha matter?
            }
        }

        return Color.rgb(redBucket / pixelCount,
                greenBucket / pixelCount,
                blueBucket / pixelCount);
    }

    public static int getScreenWidth(Activity activity) {
        if (activity != null && activity.getWindowManager() != null) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size.x;
        } else {
            return 0;
        }
    }

    public static int getScreenHeight(AppCompatActivity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getAttributeColor(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes);
        } catch (Resources.NotFoundException e) {
            Log.w("Utilities", "Not found color resource by id: " + colorRes);
        }
        return color;
    }

    public static boolean validateCode(int statusCode) {
        return statusCode == 200 || statusCode == 201;
    }

    public static String generateNonce() {
        String nonce = "";

        Random r = new Random();

        for (int i = 0; i < 32; i++) {
            nonce += String.valueOf(r.nextInt(10));
        }

        return nonce;
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes("UTF-8");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static void expand(final View v, int duration) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
//        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(duration);
        v.startAnimation(a);
    }

    public static boolean checkLastCharIsSpace(String text) {
        return text.length() <= 0 || Character.isWhitespace(text.charAt(text.length() - 1));
    }

    public static void collapse(final View v, int duration) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
//        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(duration);
        v.startAnimation(a);
    }

    public static TwitterStream getTwitterStream() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(AppData.CONSUMER_KEY)
                .setOAuthConsumerSecret(AppData.CONSUMER_SECRET)
                .setOAuthAccessToken(AppData.CLIENT_TOKEN)
                .setOAuthAccessTokenSecret(AppData.CLIENT_SECRET);

        return new TwitterStreamFactory(configurationBuilder.build()).getInstance();
    }

    public static AsyncTwitter getAsyncTwitter() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(AppData.CONSUMER_KEY)
                .setOAuthConsumerSecret(AppData.CONSUMER_SECRET)
                .setOAuthAccessToken(AppData.CLIENT_TOKEN)
                .setOAuthAccessTokenSecret(AppData.CLIENT_SECRET);

        configurationBuilder.setTweetModeExtended(true);
        AsyncTwitterFactory asyncTwitterFactory = new AsyncTwitterFactory(configurationBuilder.build());
        return asyncTwitterFactory.getInstance();
    }

    public static AsyncTwitter getAsyncTwitterMediaOnly() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(AppData.CONSUMER_KEY)
                .setOAuthConsumerSecret(AppData.CONSUMER_SECRET)
                .setOAuthAccessToken(AppData.CLIENT_TOKEN)
                .setOAuthAccessTokenSecret(AppData.CLIENT_SECRET);
        configurationBuilder.setIncludeEntitiesEnabled(true);

        AsyncTwitterFactory asyncTwitterFactory = new AsyncTwitterFactory(configurationBuilder.build());
        return asyncTwitterFactory.getInstance();
    }

    public static Twitter getTwitterInstance() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(AppData.CONSUMER_KEY)
                .setOAuthConsumerSecret(AppData.CONSUMER_SECRET)
                .setOAuthAccessToken(AppData.CLIENT_TOKEN)
                .setOAuthAccessTokenSecret(AppData.CLIENT_SECRET);

        return new TwitterFactory(configurationBuilder.build()).getInstance();
    }

    public static boolean validateTweet(StatusModel statusModel) {
        if (statusModel.isRetweet() && !AppData.appConfiguration.isShowRetweets()) return false;
        if (statusModel.getText().contains("@") && !AppData.appConfiguration.isShowMentions())
            return false;

        if (!MuteData.getInstance().isCacheLoaded())
            MuteData.getInstance().loadCache();

        if (statusModel.isRetweet() && MuteData.getInstance().getmRetweetsIds()
                .contains(statusModel.getUser().getId())) return false;

        for (RemoveModel removeModel : MuteData.getInstance().getmClientsList()) {
            if (statusModel.getSource().toLowerCase().contains(removeModel.getTitle().toLowerCase())) {
                return false;
            }
        }

        for (RemoveModel removeModel : MuteData.getInstance().getmKeywordsList()) {
            if (statusModel.getText().toLowerCase().contains(removeModel.getTitle().toLowerCase())) {
                return false;
            }
        }

        for (RemoveModel removeModel : MuteData.getInstance().getmHashtagsList()) {
            if (statusModel.getText().toLowerCase().contains(removeModel.getTitle().toLowerCase())) {
                return false;
            }
        }

        return true;
    }
}
