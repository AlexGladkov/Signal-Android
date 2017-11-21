package solonsky.signal.twitter.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.ClientsAdapter;
import solonsky.signal.twitter.data.MuteData;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.RemoveModel;
import solonsky.signal.twitter.models.SimpleModel;

/**
 * Created by neura on 21.07.17.
 */

public class MuteClientsActivity extends AppCompatActivity {

    private final String TAG = MuteClientsActivity.class.getSimpleName();
    private CircularProgressView mCpvWait;
    private RecyclerView mRvClients;
    private ArrayList<SimpleModel> simpleModels;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        setContentView(R.layout.activity_mute_clients);

        mRvClients = (RecyclerView) findViewById(R.id.recycler_mute_client);
        mCpvWait = (CircularProgressView) findViewById(R.id.cpv_mute_client);
        ImageView mBtnBack = (ImageView) findViewById(R.id.img_mute_add_back);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color));

        simpleModels = new ArrayList<>();
        ClientsAdapter clientsAdapter = new ClientsAdapter(simpleModels, getApplicationContext(),
                new ClientsAdapter.ClientsClickListener() {
                    @Override
                    public void onItemClick(SimpleModel model, View v) {
                        RemoveModel removeModel = new RemoveModel(0, model.getTitle());
                        if (!MuteData.getInstance().getmClientsList().contains(removeModel)) {
                            MuteData.getInstance().getmClientsList().add(0, removeModel);
                            MuteData.getInstance().saveCache();
                            Toast.makeText(getApplicationContext(), getString(R.string.success_mute), Toast.LENGTH_SHORT).show();
                        }

                        onBackPressed();
                    }
                });

        mRvClients.setAdapter(clientsAdapter);
        mRvClients.setHasFixedSize(true);
        mRvClients.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, true));

        updateViews(true);
        loadAllClients();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }

    private void updateViews(boolean isWait) {
        mCpvWait.setVisibility(isWait ? View.VISIBLE : View.GONE);
        mRvClients.setVisibility(isWait ? View.GONE : View.VISIBLE);
    }

    private void loadAllClients() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Build and set timeout values for the request.
                    URLConnection connection = (new URL("http://getsignal.co/links/mute-clients.txt")).openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.connect();
                    // Read and store the result line by line then return the entire string.
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder html = new StringBuilder();
                    int i = 0;
                    for (String line; (line = reader.readLine()) != null; ) {
                        html.append(line);
                        simpleModels.add(0, new SimpleModel(i, line));
                        i = i + 1;
                    }
                    in.close();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateViews(false);
                            mRvClients.scrollToPosition(0);
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "Error - " + e.getLocalizedMessage());
                }
            }
        }).start();
    }
}
