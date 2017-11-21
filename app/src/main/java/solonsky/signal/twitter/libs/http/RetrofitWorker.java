package solonsky.signal.twitter.libs.http;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import solonsky.signal.twitter.helpers.Utilities;

/**
 * Created by neura on 20.06.17.
 */

public abstract class RetrofitWorker<V> implements Callback<V> {
    private final String TAG = "RETROFITWORKER";
    private RetrofitListener callback;

    public interface RetrofitListener<V> {
        void onSuccess(V data);
        void onError(Throwable t);
    }

    protected RetrofitWorker(RetrofitListener<V> callback) {
        this.callback = callback;
    }

    @Override
    public void onResponse(Call<V> call, Response<V> response) {
        if (Utilities.validateCode(response.code())) {
            if (response.body() == null) {
                callback.onError(new Throwable("Ошибка сервера"));
            } else {
                callback.onSuccess(response.body());
            }
        } else {
            callback.onError(new Throwable("Bad code - " + response.code()));
        }
    }

    @Override
    public void onFailure(Call<V> call, Throwable t) {
        callback.onError(t);
    }
}

