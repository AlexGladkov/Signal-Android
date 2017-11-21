package solonsky.signal.twitter.helpers;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by neura on 03.08.17.
 */

public class HTML {
    private final String TAG = HTML.class.getSimpleName();
    private Context mContext;
    private HtmlRequestHandler htmlRequestHandler;

    public interface HtmlRequestHandler {
        void onUserIds(ArrayList<Long> ids);
        void onFailure();
    }

    public HTML(Context mContext, HtmlRequestHandler htmlRequestHandler) {
        this.mContext = mContext;
        this.htmlRequestHandler = htmlRequestHandler;
    }

    public void extractFavoriteUsers(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                htmlRequestHandler.onFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String aFinalString = response.body().string();
                    String[] strings = aFinalString.split("\\s+");
                    ArrayList<Long> ids = new ArrayList<>();

                    for (String string : strings) {
                        if (string.contains("data-user-id")) {
                            Log.e(TAG, "string " + string);
                            string = string.replace("u003", "").replaceAll("[^0-9]", "");
                            if (!ids.contains(Long.valueOf(string))) ids.add(Long.valueOf(string));
                        }
                    }

                    htmlRequestHandler.onUserIds(ids);
                }
            }
        });
    }
}
