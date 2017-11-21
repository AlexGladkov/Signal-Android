package solonsky.signal.twitter.helpers;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by neura on 21.06.17.
 */

public class OAuthInterceptor implements Interceptor {

   /*THIS CLASS CONTAIN ERROR ITS BECAUSE THIS APP DOES NOT IMPORTED THE RETROFIT LIBRARY*/


   /*IMPORT below dependency to gradel to fix error
   *
   *    compile 'com.squareup.retrofit2:retrofit:2.1.0'
        compile 'com.squareup.retrofit2:converter-gson:2.1.0'
        compile 'com.squareup.okhttp3:okhttp:3.3.1'
        compile 'com.squareup.okhttp3:logging-interceptor:3.3.1'
   * */

    private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String OAUTH_NONCE = "oauth_nonce";
    private static final String OAUTH_SIGNATURE = "oauth_signature";
    private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1";
    private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String OAUTH_VERSION = "oauth_version";
    private static final String OAUTH_VERSION_VALUE = "1.0";
    private static final String OAUTH_TOKEN = "oauth_token";
    private static final String TAG = "OAUTHINTERCEPTOR";

    private final String consumerKey;
    private final String consumerSecret;


    private OAuthInterceptor(String consumerKey, String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request original = chain.request();
        HttpUrl originalHttpUrl = original.url();

//        Log.d("URL", original.url().toString());
//        Log.d("URL", original.url().scheme());
//        Log.d("encodedpath", original.url().encodedPath());
//        Log.d("query", ""+original.url().query());
//        Log.d("path", ""+original.url().host());
//        Log.d("encodedQuery", ""+original.url().encodedQuery());
//        Log.d("method", ""+original.method());

        ////////////////////////////////////////////////////////////

        final String nonce = Utilities.generateNonce();
        final String timestamp = String.valueOf(new DateTime(DateTimeZone.UTC).getMillis() / 1000);
//        Log.d("nonce", nonce);
//        Log.d("time", timestamp);

        String dynamicStructureUrl = original.url().scheme() + "://" + original.url().host() + original.url().encodedPath();

//        Log.d("ENCODED PATH", ""+dynamicStructureUrl);
        String firstBaseString = original.method() + "&" + urlEncoded(dynamicStructureUrl);
//        Log.d("firstBaseString", firstBaseString);
        String generatedBaseString = "";


        if (original.url().encodedQuery() != null) {
            generatedBaseString = original.url().encodedQuery() + "&include_entities=true&oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonce + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + timestamp + "&oauth_version=1.0";
        } else {
            generatedBaseString = "include_entities=true&oauth_consumer_key=" + consumerKey + "&oauth_nonce=" +
                    nonce + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + timestamp + "&oauth_token=" +
                    AppData.CLIENT_TOKEN + "&oauth_version=1.0&status=hello";
        }


//        ParameterList result = new ParameterList();
//        result.addQuerystring(generatedBaseString);
//        generatedBaseString=result.sort().asOauthBaseString();
//        Log.d("Sorted","00--"+result.sort().asOauthBaseString());

        String secoundBaseString = "&" + generatedBaseString;

        if (firstBaseString.contains("%3F")) {
            secoundBaseString = "%26" + urlEncoded(generatedBaseString);
        }

        String baseString = firstBaseString + secoundBaseString;

        Log.e(TAG, "baseString - " + baseString);

//        baseString = "POST&https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3D" + AppData.CONSUMER_KEY + "%26oauth_nonce%3D" + nonce + "%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D" + timestamp + "%26oauth_token%3D155474496-Q3b5cehEeD2wMPCqVwOYiaVaFQFp2H9akHu3ZQWV%26oauth_version%3D1.0%26status%3Dhello";

//        String signature = new HMACSha1SignatureService().getSignature(baseString,
//                consumerSecret, AppData.CLIENT_SECRET);
//        Log.d("Signature", signature);

        HttpUrl url = originalHttpUrl.newBuilder()
                .addQueryParameter("include_entities", "true")
                .addQueryParameter(OAUTH_CONSUMER_KEY, consumerKey)
                .addQueryParameter(OAUTH_NONCE, nonce)
                .addQueryParameter(OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE)
                .addQueryParameter(OAUTH_TIMESTAMP, timestamp)
                .addQueryParameter(OAUTH_TOKEN, AppData.CLIENT_TOKEN)
                .addQueryParameter(OAUTH_VERSION, OAUTH_VERSION_VALUE)
                .addQueryParameter("status", "hello")
                .build();


        String authHeader = "OAuth ";

//        authHeader = addHead(authHeader, OAUTH_CONSUMER_KEY, consumerKey);
//        authHeader = addHead(authHeader, OAUTH_NONCE, nonce);
//        authHeader = addHead(authHeader, OAUTH_SIGNATURE, signature);
//        authHeader = addHead(authHeader, OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE);
//        authHeader = addHead(authHeader, OAUTH_TIMESTAMP, timestamp);
//        authHeader = addHead(authHeader, OAUTH_TOKEN, AppData.CLIENT_TOKEN);
//        authHeader = addHead(authHeader, OAUTH_VERSION, OAUTH_VERSION_VALUE);
//        authHeader = authHeader.substring(0, authHeader.length() - 1);

        Log.e(TAG, "Client token - " + AppData.CLIENT_TOKEN);
        Log.e(TAG, "Client secret - " + AppData.CLIENT_SECRET);

        // Request customization: add request headers
        Request.Builder requestBuilder = original.newBuilder()
                .addHeader("Authorization", authHeader)
                .url(url);

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }


    public static final class Builder {

        private String consumerKey;
        private String consumerSecret;
        private int type;

        public Builder consumerKey(String consumerKey) {
            if (consumerKey == null) throw new NullPointerException("consumerKey = null");
            this.consumerKey = consumerKey;
            return this;
        }

        public Builder consumerSecret(String consumerSecret) {
            if (consumerSecret == null) throw new NullPointerException("consumerSecret = null");
            this.consumerSecret = consumerSecret;
            return this;
        }



        public OAuthInterceptor build() {

            if (consumerKey == null) throw new IllegalStateException("consumerKey not set");
            if (consumerSecret == null) throw new IllegalStateException("consumerSecret not set");

            return new OAuthInterceptor(consumerKey, consumerSecret);
        }
    }

    public String addHead(String input, String key, String value) {
        input = input + key + "=" + value + ",";
        return input;
    }

    public String urlEncoded(String url) {
        String encodedurl = "";
        try {

            encodedurl = URLEncoder.encode(url, "UTF-8");
//            Log.d("TEST", encodedurl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encodedurl;
    }
}
