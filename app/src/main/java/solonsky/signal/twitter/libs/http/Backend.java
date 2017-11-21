package solonsky.signal.twitter.libs.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.OAuthInterceptor;
import solonsky.signal.twitter.helpers.Utilities;

/**
 * Created by neura on 20.06.17.
 */

public class Backend {
    private static volatile Backend instance;
    private final Gson gson;
    private final Retrofit retrofit;

    public static Backend getInstance() {
        Backend localInstance = instance;
        if (localInstance == null) {
            synchronized (Backend.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Backend();
                }
            }
        }

        return localInstance;
    }

    private Backend() {
//        OkHttpOAuthConsumer consumer = new OkHttpOAuthConsumer(AppData.CONSUMER_KEY, AppData.CONSUMER_SECRET);
//        consumer.setTokenWithSecret(AppData.CLIENT_TOKEN, AppData.CLIENT_SECRET);

        OAuthInterceptor oAuthInterceptor = new OAuthInterceptor.Builder()
                .consumerSecret(AppData.CONSUMER_SECRET)
                .consumerKey(AppData.CONSUMER_KEY)
                .build();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .addInterceptor(interceptor)
                .build();

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        String mainUrl = "http://getsignal.co/";
        retrofit = new Retrofit.Builder()
                .baseUrl(mainUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void call(String path, Object params, RetrofitWorker.RetrofitListener<JsonObject> callback) {
        String token = "OAuth oauth_nonce=\"" + Utilities.generateNonce() + "\", oauth_timestamp=\"" +
        System.currentTimeMillis() + "\", oauth_version=\"1.0\", oauth_signature_method=\"HMAC-SHA1\", " +
                "oauth_consumer_key=\"" + AppData.CONSUMER_KEY + "\"," +
                " oauth_token=\"" + AppData.CLIENT_TOKEN + "\", oauth_signature=\"gBzukmvv8ivdGYPc53FbEpAvwZs%3D\"";
        retrofit.create(PostApi.class).callApi(path, (JsonObject) new Gson().toJsonTree(params))
                .enqueue(new RetrofitWorker<JsonObject>(callback) {});
    }

    public void get(String path, RetrofitWorker.RetrofitListener<String> callback) {
        retrofit.create(GetApi.class).callApi(path).enqueue(new RetrofitWorker<String>(callback) {});
    }

    interface PostApi {
        @POST("{path_to_api}")
        Call<JsonObject> callApi(@Path("path_to_api") String path, @Body JsonObject send);
    }

    interface GetApi {
        @GET("{path_to_api}")
        Call<String> callApi(@Path("path_to_api") String Path);
    }
}
